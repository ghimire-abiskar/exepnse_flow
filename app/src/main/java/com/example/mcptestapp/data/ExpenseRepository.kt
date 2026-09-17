package com.example.mcptestapp.data

import com.example.mcptestapp.model.Expense
import com.example.mcptestapp.model.ExpenseCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth

class ExpenseRepository(private val dao: ExpenseDao,private val budgetDao: BudgetDao) {
    val expenses: Flow<List<Expense>> = dao.observeAll().map { rows -> rows.map(ExpenseEntity::toDomain) }
    suspend fun add(expense: Expense) = dao.insert(expense.toEntity())
    suspend fun delete(expense: Expense) = dao.delete(expense.toEntity())
    suspend fun allExpenses(): List<Expense> = dao.getAll().map(ExpenseEntity::toDomain)
    suspend fun import(expenses: List<Expense>) = dao.insertAll(expenses.map(Expense::toEntity))

    fun getBudgetLimit(yearMonth: YearMonth): Flow<Double> {
        return budgetDao.getBudgetLimit(yearMonth.toString())
    }
    suspend fun updateBudgetLimit(budgetEntity: BudgetEntity) = budgetDao.insertOrUpdateBudgetEntity(budgetEntity)
}

private fun ExpenseEntity.toDomain() = Expense(id, title, amount, ExpenseCategory.valueOf(category), LocalDate.ofEpochDay(dateEpochDay))
private fun Expense.toEntity() = ExpenseEntity(id, title, amount, category.name, date.toEpochDay())
