package com.example.mcptestapp.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.LazyColumn
import com.example.mcptestapp.ExpenseViewModel
import com.example.mcptestapp.model.ExpenseCategory
import com.example.mcptestapp.ui.component.AddExpenseDialog
import com.example.mcptestapp.ui.component.AnalyticsSection
import com.example.mcptestapp.ui.component.BudgetLimitDialog
import com.example.mcptestapp.ui.component.CategorySpend
import com.example.mcptestapp.ui.component.ExpenseTopBar
import com.example.mcptestapp.ui.component.FilterDialog
import com.example.mcptestapp.ui.component.FiltersButton
import com.example.mcptestapp.ui.component.RecentExpensesSection
import com.example.mcptestapp.ui.component.SummaryRow
import com.example.mcptestapp.ui.component.color
import com.example.mcptestapp.ui.component.displayName
import kotlinx.coroutines.launch
import java.time.YearMonth

private enum class BottomTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    ACTIVITY("Activity", Icons.Default.List),
}

/**
 * Dropdown replacement for the old horizontally-scrolling FilterChip row.
 * Same selection semantics as before: null category + showOtherCategories=false
 * means "All", showOtherCategories=true means the HEALTH/LEISURE bucket, otherwise
 * a specific ExpenseCategory is selected.
 */
@Composable
private fun CategoryFilterDropdown(
    selectedCategory: ExpenseCategory?,
    showOtherCategories: Boolean,
    onSelectAll: () -> Unit,
    onSelectCategory: (ExpenseCategory) -> Unit,
    onSelectOtherCategories: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val currentLabel = when {
        selectedCategory != null -> selectedCategory.displayName()
        showOtherCategories -> "Other categories"
        else -> "All categories"
    }

    Column(modifier = Modifier.wrapContentWidth()) {
        OutlinedButton(onClick = { expanded = true }) {
            Text(currentLabel)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("All categories") },
                onClick = {
                    onSelectAll()
                    expanded = false
                }
            )
            ExpenseCategory.entries.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.displayName()) },
                    onClick = {
                        onSelectCategory(category)
                        expanded = false
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("Other categories") },
                onClick = {
                    onSelectOtherCategories()
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun ExpenseTrackerScreen() {
    val context = LocalContext.current
    val viewModel: ExpenseViewModel = viewModel(factory = ExpenseViewModel.factory(context.applicationContext))
    val expenses by viewModel.expenses.collectAsState()
    var selectedMonth by remember { mutableStateOf<YearMonth?>(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<java.time.LocalDate?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }
    var selectedCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
    var showOtherCategories by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        viewModel.exportCsv { csv ->
            runCatching { context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(csv) } ?: error("Unable to write file") }
                .onSuccess { message = "Exported ${expenses.size} expenses." }
                .onFailure { message = "Export failed: ${it.message}" }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching { context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: error("Unable to read file") }
            .onSuccess { csv -> viewModel.importCsv(csv) { result -> message = result.fold({ "Imported $it expenses." }, { "Import failed: ${it.message}" }) } }
            .onFailure { message = "Import failed: ${it.message}" }
    }

    val otherCategorySet = setOf(ExpenseCategory.HEALTH, ExpenseCategory.LEISURE)
    val filteredExpenses = remember(expenses, selectedMonth, selectedDate, selectedCategory, showOtherCategories) {
        expenses.filter { expense ->
            val monthMatches = selectedMonth == null || YearMonth.from(expense.date) == selectedMonth
            val dateMatches = selectedDate == null || expense.date == selectedDate
            val categoryMatches = when {
                selectedCategory != null -> expense.category == selectedCategory
                showOtherCategories -> expense.category in otherCategorySet
                else -> true
            }
            monthMatches && dateMatches && categoryMatches
        }.sortedByDescending { it.date }
    }

    val activeMonth = selectedMonth ?: YearMonth.now()
    LaunchedEffect(activeMonth) {
        viewModel.setSelectedMonth(activeMonth)
    }

    val totalSpent = filteredExpenses.sumOf { it.amount }
    val largestExpense = filteredExpenses.maxByOrNull { it.amount }
    val categoryTotals = ExpenseCategory.entries.map { category ->
        val amount = filteredExpenses.filter { it.category == category }.sumOf { it.amount }
        CategorySpend(category, amount, category.color())
    }.filter { it.amount > 0.0 }
    val budgetLimit by viewModel.budgetLimit.collectAsState()
    val budgetLeft = budgetLimit - totalSpent
    val activeCategoryLabel = when {
        selectedCategory != null -> selectedCategory!!.displayName()
        showOtherCategories -> "Other categories"
        else -> null
    }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.65f)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = "ExpenseFlow", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp))
                    NavigationDrawerItem(
                        label = { Text("Import CSV") },
                        selected = false,
                        onClick = {
                            importLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/csv"))
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Export CSV") },
                        selected = false,
                        onClick = {
                            exportLauncher.launch("expenses.csv")
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = { ExpenseTopBar(onOpenDrawer = { scope.launch { drawerState.open() } }) },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    BottomTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { androidx.compose.material3.Icon(imageVector = tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddDialog = true }, containerColor = Color(0xFF5B67F1), contentColor = Color.White) {
                    Icon(Icons.Default.Add, contentDescription = "Add expense")
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }
                item { FiltersButton(selectedMonth = selectedMonth, selectedDate = selectedDate, onClick = { showFilterDialog = true }) }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        CategoryFilterDropdown(
                            selectedCategory = selectedCategory,
                            showOtherCategories = showOtherCategories,
                            onSelectAll = {
                                selectedCategory = null
                                showOtherCategories = false
                            },
                            onSelectCategory = { category ->
                                selectedCategory = category
                                showOtherCategories = false
                            },
                            onSelectOtherCategories = {
                                showOtherCategories = true
                                selectedCategory = null
                            }
                        )
                    }
                }
                when (selectedTab) {
                    BottomTab.HOME -> {
                        item {
                            SummaryRow(
                                totalSpent = totalSpent,
                                budgetLeft = budgetLeft,
                                budgetLimit = budgetLimit,
                                selectedMonth = activeMonth,
                                largestExpense = largestExpense,
                                onBudgetClick = { showBudgetDialog = true },
                                categoryLabel = activeCategoryLabel
                            )
                        }
                        if (selectedCategory == null && !showOtherCategories) {
                            item { AnalyticsSection(categoryTotals = categoryTotals) }
                        }
                    }
                    BottomTab.ACTIVITY -> {
                        item { RecentExpensesSection(expenses = filteredExpenses, onDeleteExpense = viewModel::delete) }
                    }
                }
            }
        }
    }

    if (showFilterDialog) {
        FilterDialog(
            initialMonth = selectedMonth,
            initialDate = selectedDate,
            availableMonths = expenses.map { YearMonth.from(it.date) }.distinct().sortedDescending(),
            onApply = { m, d -> selectedMonth = m; selectedDate = d; showFilterDialog = false },
            onDismiss = { showFilterDialog = false }
        )
    }

    if (showBudgetDialog) {
        BudgetLimitDialog(
            month = activeMonth,
            currentLimit = budgetLimit,
            onDismiss = { showBudgetDialog = false },
            onSave = { newBudget ->
                viewModel.updateBudgetLimit(activeMonth, newBudget)
                showBudgetDialog = false
            }
        )
    }

    if (showAddDialog) {
        AddExpenseDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newExpense ->
                viewModel.add(newExpense)
                showAddDialog = false
            }
        )
    }
}