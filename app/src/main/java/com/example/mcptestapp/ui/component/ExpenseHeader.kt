package com.example.mcptestapp.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTopBar(onOpenDrawer: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val headerColor = if (isDark) Color(0xFF4F46E5) else Color(0xFFDDEEFF)
    val headerTextColor = if (isDark) Color.White else Color(0xFF0F172A)

    TopAppBar(
        modifier = Modifier.background(headerColor),
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, contentDescription = "Open navigation", tint = headerTextColor) }
        },
        title = {
            Text(
                text = "ExpenseFlow",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = headerTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = headerColor,
            navigationIconContentColor = headerTextColor,
            titleContentColor = headerTextColor
        )
    )
}

@Composable
fun FiltersButton(
    selectedMonth: YearMonth?,
    selectedDate: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    val activeLabel = when {
        selectedDate != null -> selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
        selectedMonth != null -> selectedMonth.format(DateTimeFormatter.ofPattern("MMM yyyy"))
        else -> "All expenses"
    }
    val isFilterActive = selectedMonth != null || selectedDate != null

    Row(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "Showing", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = activeLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }

        OutlinedButton(onClick = onClick, shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null,
                tint = if (isFilterActive) Color(0xFF5B67F1) else Color(0xFF6C7A93),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = "Filters", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}