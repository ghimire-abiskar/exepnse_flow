package com.example.mcptestapp.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mcptestapp.model.Expense
import com.example.mcptestapp.model.ExpenseCategory
import java.text.NumberFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

data class CategorySpend(
    val category: ExpenseCategory,
    val amount: Double,
    val color: Color,
)

@Composable
fun SummaryRow(
    totalSpent: Double,
    budgetLeft: Double,
    budgetLimit: Double,
    selectedMonth: YearMonth,
    largestExpense: Expense?,
    onBudgetClick: () -> Unit,
    categoryLabel: String? = null,
) {
    val internalCardColor = internalCardBackgroundColor()
    val totalSubtitle = if (categoryLabel != null) "$categoryLabel this month" else "This month"
    val budgetSubtitle = if (categoryLabel != null) "Of ${formatCurrency(budgetLimit)} for $categoryLabel" else "Of ${formatCurrency(budgetLimit)} for ${selectedMonth.format(DateTimeFormatter.ofPattern("MMM yyyy"))}"

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(
                title = "Total spent",
                value = formatCurrency(totalSpent),
                subtitle = totalSubtitle,
                accent = Color(0xFF5B67F1),
                icon = Icons.Default.AttachMoney,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Budget spent",
                value = formatCurrency(totalSpent),
                subtitle = budgetSubtitle,
                accent = Color(0xFF2FCB9A),
                icon = Icons.Default.Home,
                modifier = Modifier.weight(1f),
                configureIcon = Icons.Default.Settings,
                onConfigureClick = onBudgetClick
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = internalCardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Largest spend", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = largestExpense?.title ?: "No data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = largestExpense?.let { formatCurrency(it.amount) } ?: "₹0", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = largestExpense?.category?.displayName() ?: "—", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    subtitle: String,
    accent: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    configureIcon: ImageVector? = null,
    onConfigureClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = internalCardBackgroundColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                }
                Text(text = title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                configureIcon?.let {
                    if (onConfigureClick != null) {
                        IconButton(onClick = onConfigureClick, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = it, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                        }
                    } else {
                        Icon(imageVector = it, contentDescription = null, tint = accent, modifier = Modifier.size(25.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsSection(categoryTotals: List<CategorySpend>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = internalCardBackgroundColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Spending analytics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Icon(imageVector = Icons.Default.BarChart, contentDescription = null, tint = Color(0xFF5B67F1), modifier = Modifier.size(22.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CategoryDonutChart(categoryTotals)
                Column(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    categoryTotals.forEach { item ->
                        CategoryLegendRow(title = item.category.displayName(), amount = formatCurrency(item.amount), color = item.color)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryDonutChart(categoryTotals: List<CategorySpend>) {
    val chartCenterColor = internalCardBackgroundColor()

    if (categoryTotals.isEmpty()) {
        Box(
            modifier = Modifier.size(170.dp).background(chartCenterColor, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("No data", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val total = categoryTotals.sumOf { it.amount }
    val strokeWidth = 22.dp
    Canvas(modifier = Modifier.size(170.dp).padding(8.dp)) {
        var startAngle = -90f
        val donutSize = Size(size.width, size.height)
        val availableAngle = 360f

        categoryTotals.forEach { item ->
            val segment = ((item.amount / total) * availableAngle).toFloat()
            drawArc(
                color = item.color,
                startAngle = startAngle,
                sweepAngle = segment,
                useCenter = false,
                size = donutSize,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
            )
            startAngle += segment
        }

        drawCircle(
            color = chartCenterColor,
            radius = (size.minDimension / 2f) - (strokeWidth.toPx() * 1.15f),
            center = Offset(size.width / 2f, size.height / 2f)
        )
    }
}

@Composable
fun CategoryLegendRow(title: String, amount: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.size(12.dp).background(color, shape = CircleShape))
        Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Text(text = amount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
internal fun internalCardBackgroundColor(): Color {
    return if (isSystemInDarkTheme()) {
        Color(0xFF1D2333)
    } else {
        Color(0xFFF4F7FF)
    }
}

internal fun formatCurrency(amount: Double): String {
    val roundedAmount = amount.roundToInt()
    return "₹${NumberFormat.getNumberInstance(Locale("en", "in")).format(roundedAmount)}"
}

internal fun ExpenseCategory.displayName(): String = when (this) {
    ExpenseCategory.FOOD -> "Food"
    ExpenseCategory.TRANSPORT -> "Transport"
    ExpenseCategory.SHOPPING -> "Shopping"
    ExpenseCategory.BILLS -> "Bills"
    ExpenseCategory.HEALTH -> "Health"
    ExpenseCategory.LEISURE -> "Leisure"
}
