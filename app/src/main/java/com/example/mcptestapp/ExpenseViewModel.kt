package com.example.mcptestapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.mcptestapp.data.BudgetEntity
import com.example.mcptestapp.data.ExpenseDatabase
import com.example.mcptestapp.data.ExpenseRepository
import com.example.mcptestapp.model.Expense
import com.example.mcptestapp.model.ExpenseCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {
    val expenses: StateFlow<List<Expense>> = repository.expenses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val _selectedMonth: MutableStateFlow<YearMonth> = MutableStateFlow(YearMonth.now())

    fun setSelectedMonth(month: YearMonth) {
        _selectedMonth.value = month
    }

    fun add(expense: Expense) = viewModelScope.launch { repository.add(expense) }
    fun delete(expense: Expense) = viewModelScope.launch { repository.delete(expense) }
    fun exportCsv(onReady: (String) -> Unit) = viewModelScope.launch { onReady(CsvCodec.encode(repository.allExpenses())) }
    fun importCsv(csv: String, onResult: (Result<Int>) -> Unit) = viewModelScope.launch {
        runCatching { CsvCodec.decode(csv) }.onSuccess { repository.import(it); onResult(Result.success(it.size)) }
            .onFailure { onResult(Result.failure(it)) }
    }
    fun updateBudgetLimit(month: YearMonth, newBudget: Double){
        viewModelScope.launch {
            repository.updateBudgetLimit(BudgetEntity(month,newBudget))
        }
    }
    val budgetLimit : StateFlow<Double> = _selectedMonth.flatMapLatest { yearMonth ->
        repository.getBudgetLimit(yearMonth)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0.0
    )
    companion object {
        fun factory(context: Context) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = Room.databaseBuilder(context.applicationContext, ExpenseDatabase::class.java, "expenses.db").build()
                return ExpenseViewModel(ExpenseRepository(database.expenseDao(),database.budgetDao())) as T
            }
        }
    }
}

object CsvCodec {
    private const val HEADER = "title,amount,category,date"
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    fun encode(expenses: List<Expense>): String = buildString {
        appendLine(HEADER)
        expenses.forEach { appendLine(listOf(escape(it.title), it.amount, it.category.name, it.date.format(formatter)).joinToString(",")) }
    }
    fun decode(csv: String): List<Expense> {
        val lines = csv.lineSequence().filter { it.isNotBlank() }.toList()
        require(lines.firstOrNull()?.trim() == HEADER) { "CSV header must be: $HEADER" }
        return lines.drop(1).mapIndexed { index, line ->
            val fields = parseLine(line)
            require(fields.size == 4) { "Row ${index + 2} must contain four columns" }
            val amount = fields[1].toDoubleOrNull() ?: error("Row ${index + 2} has an invalid amount")
            require(amount > 0) { "Row ${index + 2} amount must be positive" }
            Expense(0, fields[0].trim().also { require(it.isNotEmpty()) { "Row ${index + 2} title is required" } }, amount,
                runCatching { ExpenseCategory.valueOf(fields[2].trim().uppercase()) }.getOrElse { error("Row ${index + 2} has an invalid category") },
                runCatching { LocalDate.parse(fields[3].trim(), formatter) }.getOrElse { error("Row ${index + 2} has an invalid date") })
        }
    }
    private fun escape(value: String) = "\"" + value.replace("\"", "\"\"") + "\""
    private fun parseLine(line: String): List<String> {
        val result = mutableListOf<String>(); val field = StringBuilder(); var quoted = false; var i = 0
        while (i < line.length) { val c = line[i]; when { c == '\"' && quoted && i + 1 < line.length && line[i + 1] == '\"' -> { field.append(c); i++ }; c == '\"' -> quoted = !quoted; c == ',' && !quoted -> { result += field.toString(); field.clear() }; else -> field.append(c) }; i++ }
        require(!quoted) { "Unclosed quote in CSV" }; result += field.toString(); return result
    }
}
