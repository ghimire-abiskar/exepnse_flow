package com.example.mcptestapp.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mcptestapp.model.Expense
import com.example.mcptestapp.model.ExpenseCategory
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * Builds a flat list of calendar cells for [yearMonth], padded with nulls
 * so the grid always starts on Sunday and completes full weeks.
 */
private fun generateCalendarDays(yearMonth: YearMonth): List<LocalDate?> {
    val firstDay = yearMonth.atDay(1)
    // DayOfWeek.SUNDAY.value == 7, MONDAY == 1 ... convert so Sunday -> 0
    val leadingBlanks = firstDay.dayOfWeek.value % 7
    val daysInMonth = yearMonth.lengthOfMonth()

    val cells = mutableListOf<LocalDate?>()
    repeat(leadingBlanks) { cells.add(null) }
    for (day in 1..daysInMonth) cells.add(yearMonth.atDay(day))
    while (cells.size % 7 != 0) cells.add(null)
    return cells
}

private val CALENDAR_CELL_SIZE = 32.dp

/**
 * Compact, self-sized calendar picker. Unlike Material3's DatePicker/DatePickerDialog
 * (which force a fixed ~360dp container width regardless of content), this sizes
 * itself naturally to its fixed-size day cells, so it stays small and uniform.
 */
@Composable
private fun CompactCalendarPicker(
    initialDate: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    var displayedMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }
    var selectedDate by remember { mutableStateOf(initialDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("d MMM yyyy")),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row {
                        IconButton(onClick = { displayedMonth = displayedMonth.minusMonths(1) }) {
                            Text("<", style = MaterialTheme.typography.titleMedium)
                        }
                        IconButton(onClick = { displayedMonth = displayedMonth.plusMonths(1) }) {
                            Text(">", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        },
        text = {
            Column {
                val weekDayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    weekDayLabels.forEach { label ->
                        Box(
                            modifier = Modifier.size(CALENDAR_CELL_SIZE),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                val days = remember(displayedMonth) { generateCalendarDays(displayedMonth) }
                days.chunked(7).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        week.forEach { date ->
                            val isSelected = date != null && date == selectedDate
                            Box(
                                modifier = Modifier
                                    .size(CALENDAR_CELL_SIZE)
                                    .then(
                                        if (isSelected) {
                                            Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .then(
                                        if (date != null) Modifier.clickable { selectedDate = date } else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (date != null) {
                                    Text(
                                        text = date.dayOfMonth.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedDate) }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    initialMonth: YearMonth?,
    initialDate: LocalDate?,
    availableMonths: List<YearMonth>,
    onApply: (YearMonth?, LocalDate?) -> Unit,
    onDismiss: () -> Unit,
) {
    var tempMonth by remember { mutableStateOf(initialMonth) }
    var tempDate by remember { mutableStateOf(initialDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        CompactCalendarPicker(
            initialDate = tempDate ?: LocalDate.now(),
            onConfirm = { picked ->
                tempDate = picked
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter expenses") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Month", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (availableMonths.isNotEmpty()) {
                            availableMonths.take(12).forEach { month ->
                                FilterChip(
                                    selected = tempMonth == month,
                                    onClick = { tempMonth = if (tempMonth == month) null else month },
                                    label = { Text(month.format(DateTimeFormatter.ofPattern("MMM yyyy"))) }
                                )
                            }
                        } else {
                            Text("No months available", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Specific date", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = tempDate?.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) ?: "Pick a date",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (tempDate != null) {
                            TextButton(onClick = { tempDate = null }) {
                                Text("Clear")
                            }
                        }
                    }
                }

                if (tempMonth != null || tempDate != null) {
                    TextButton(
                        onClick = { tempMonth = null; tempDate = null },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Reset all")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onApply(tempMonth, tempDate) }) { Text("Apply") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onSave: (Expense) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.FOOD) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        CompactCalendarPicker(
            initialDate = selectedDate,
            onConfirm = { picked ->
                selectedDate = picked
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("Amount") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text(text = "Category", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExpenseCategory.entries.forEach { category ->
                        FilterChip(selected = selectedCategory == category, onClick = { selectedCategory = category }, label = { Text(category.displayName()) })
                    }
                }
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val parsedAmount = amountText.toDoubleOrNull() ?: return@Button
                if (title.isNotBlank()) {
                    onSave(Expense(title = title.trim(), amount = parsedAmount, category = selectedCategory, date = selectedDate))
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun BudgetLimitDialog(
    month: YearMonth,
    currentLimit: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit,
) {
    var budgetText by remember(currentLimit, month) { mutableStateOf(currentLimit.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update budget for ${month.format(DateTimeFormatter.ofPattern("MMM yyyy"))}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Enter a new monthly budget limit.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                OutlinedTextField(value = budgetText, onValueChange = { budgetText = it }, label = { Text("Budget limit") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val parsed = budgetText.toDoubleOrNull() ?: return@TextButton
                onSave(parsed)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}