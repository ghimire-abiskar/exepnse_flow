package com.example.mcptestapp.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mcptestapp.model.Expense
import com.example.mcptestapp.model.ExpenseCategory
import java.time.format.DateTimeFormatter

@Composable
fun RecentExpensesSection(
    expenses: List<Expense>,
    onDeleteExpense: (Expense) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "Recent expenses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            if (expenses.isEmpty()) {
                Text(text = "No expenses match the current filter.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                expenses.forEachIndexed { index, expense ->
                    ExpenseRow(expense = expense, onDelete = { onDeleteExpense(expense) })
                    if (index != expenses.lastIndex) {
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseRow(
    expense: Expense,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape).background(expense.category.color().copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = expense.category.icon(), contentDescription = null, tint = expense.category.color(), modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = expense.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = expense.date.format(DateTimeFormatter.ofPattern("MMM d, yyyy")), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(horizontalAlignment = Alignment.End) {
                Text(text = formatCurrency(expense.amount), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = expense.category.displayName(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete expense", tint = Color(0xFFEA6A6A))
            }
        }
    }
}

internal fun ExpenseCategory.color(): Color = when (this) {
    ExpenseCategory.FOOD -> Color(0xFF5B67F1)
    ExpenseCategory.TRANSPORT -> Color(0xFF2FCB9A)
    ExpenseCategory.SHOPPING -> Color(0xFFEA6A6A)
    ExpenseCategory.BILLS -> Color(0xFFFFB84D)
    ExpenseCategory.HEALTH -> Color(0xFF7B61FF)
    ExpenseCategory.LEISURE -> Color(0xFF22B1E5)
}

internal fun ExpenseCategory.icon(): ImageVector = when (this) {
    ExpenseCategory.FOOD -> Icons.Default.Fastfood
    ExpenseCategory.TRANSPORT -> Icons.Default.Train
    ExpenseCategory.SHOPPING -> Icons.Default.ShoppingBag
    ExpenseCategory.BILLS -> Icons.Default.Home
    ExpenseCategory.HEALTH -> Icons.Default.HealthAndSafety
    ExpenseCategory.LEISURE -> Icons.Default.AttachMoney
}
