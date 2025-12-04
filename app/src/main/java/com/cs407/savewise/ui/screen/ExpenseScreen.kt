package com.cs407.savewise.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs407.savewise.model.ExpenseRecord
import com.cs407.savewise.ui.theme.SavewiseTheme
import com.cs407.savewise.viewModel.ExpensesViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.style.TextAlign
import com.cs407.savewise.viewModel.ExpenseFilter


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ExpenseScreen() {
    val vm: ExpensesViewModel = viewModel()
    val expenses by vm.filteredExpenses.collectAsState(emptyList())
    val filter by vm.filter.collectAsState()
    val categories by vm.categories.collectAsState(emptySet())

    var editing by remember { mutableStateOf<ExpenseRecord?>(null) }
    var pendingDelete by remember { mutableStateOf<ExpenseRecord?>(null) }
    var showFilters by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Expenses") },
                actions = {
                    IconButton(onClick = { showFilters = true }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filter")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            // 1) Summary card
            item {
                ExpensesSummaryCard(expenses = expenses)
            }

            // 2) Quick category chips
            if (categories.isNotEmpty()) {
                item {
                    CategoryFilterRow(
                        categories = categories,
                        selected = filter.categories,
                        onToggle = { cat -> vm.toggleCategory(cat) }
                    )
                }
            }

            // 3) List or empty state
            if (expenses.isEmpty()) {
                val hasActiveFilter = filter != ExpenseFilter()
                item {
                    ExpensesEmptyStateCard(
                        hasFilter = hasActiveFilter,
                        onClearFilters = {
                            vm.clearFilters()
                        }
                    )
                }
            } else {
                item {
                    Text(
                        text = "All expenses",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                items(expenses, key = { it.id }) { expense ->
                    ExpenseRowCard(
                        expense = expense,
                        onClick = { editing = expense },
                        onLongPress = { pendingDelete = expense }
                    )
                }
            }
        }
    }

    // keep your dialogs exactly as before

    if (editing != null) {
        EditExpenseDialog(
            expense = editing!!,
            onDismiss = { editing = null },
            onSave = { updated ->
                vm.updateExpense(updated)
                editing = null
            }
        )
    }

    pendingDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete expense?") },
            text = { Text("This will permanently remove \"${expense.title}\".") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteExpense(expense.id)
                    pendingDelete = null
                    if (editing?.id == expense.id) {
                        editing = null
                    }
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showFilters) {
        FilterDialog(
            currentFilter = filter,
            categories = categories,
            onApply = { q, min, max, start, end, selectedCats ->
                vm.setFilterQuery(q)
                vm.setAmountRange(min, max)
                vm.setDateRange(start, end)
                vm.setSelectedCategories(selectedCats)
                showFilters = false
            },
            onClear = {
                vm.clearFilters()
                showFilters = false
            },
            onDismiss = { showFilters = false }
        )
    }
}


@Composable
private fun ExpenseRow(
    expense: ExpenseRecord,
    onClick: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick?.invoke() },
                onLongClick = { onLongPress?.invoke() }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = iconForCategory(expense.category)
        Icon(
            imageVector = icon,
            contentDescription = expense.category,
            tint = MaterialTheme.colorScheme.primary
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = expense.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${expense.category} - ${expense.date}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = formatAmount(expense.amount),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun ExpensesSummaryCard(expenses: List<ExpenseRecord>) {
    val total = expenses.sumOf { it.amount }
    val count = expenses.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Total spent",
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = String.format("$%.2f", total),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = if (count == 0) "No expenses recorded"
                else "$count expense${if (count > 1) "s" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CategoryFilterRow(
    categories: Set<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.sorted().forEach { cat ->
            val isSelected = cat in selected
            FilterChip(
                selected = isSelected,
                onClick = { onToggle(cat) },
                label = { Text(cat) },
                modifier = Modifier
            )
        }
    }
}

@Composable
private fun ExpenseRowCard(
    expense: ExpenseRecord,
    onClick: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onClick?.invoke() },
                    onLongClick = { onLongPress?.invoke() }
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = iconForCategory(expense.category)
            Icon(
                imageVector = icon,
                contentDescription = expense.category,
                tint = MaterialTheme.colorScheme.primary
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${expense.category} - ${expense.date}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = formatAmount(expense.amount),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ExpensesEmptyStateCard(
    hasFilter: Boolean,
    onClearFilters: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (hasFilter) "No expenses match your filter"
                else "No expenses yet",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = if (hasFilter)
                    "Try adjusting or clearing your filters."
                else
                    "Add an expense from the Home screen or with the + button.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (hasFilter) {
                TextButton(onClick = onClearFilters) {
                    Text("Clear filters")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditExpenseDialog(
    expense: ExpenseRecord,
    onDismiss: () -> Unit,
    onSave: (ExpenseRecord) -> Unit
) {
    var title by remember { mutableStateOf(expense.title) }
    var category by remember { mutableStateOf(expense.category) }
    val isoFormatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }
    val displayFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault()) }
    var isoDate by remember {
        mutableStateOf(expense.date.takeIf { it.isNotBlank() } ?: LocalDate.now().format(isoFormatter))
    }
    val displayDate = remember(isoDate) {
        runCatching { LocalDate.parse(isoDate, isoFormatter).format(displayFormatter) }.getOrElse { isoDate }
    }
    var showDatePicker by remember { mutableStateOf(false) }
    val initialDateMillis = remember(isoDate) { parseIsoDateToMillis(isoDate) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
    var amountText by remember { mutableStateOf(expense.amount.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Expense") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                DateSelectorField(
                    label = "Date",
                    displayValue = displayDate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    onClick = { showDatePicker = true }
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amount = amountText.toDoubleOrNull()
                if (amount != null) {
                    onSave(
                        expense.copy(
                            title = title,
                            category = category,
                            date = isoDate,
                            amount = amount
                        )
                    )
                }
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            isoDate = millisToIsoDate(millis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        isoDate = ""
                        showDatePicker = false
                    }) { Text("Clear") }
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDialog(
    currentFilter: com.cs407.savewise.viewModel.ExpenseFilter,
    categories: Set<String>,
    onApply: (query: String, min: Double?, max: Double?, start: String?, end: String?, selected: Set<String>) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf(currentFilter.query) }
    var minText by remember { mutableStateOf(currentFilter.minAmount?.toString() ?: "") }
    var maxText by remember { mutableStateOf(currentFilter.maxAmount?.toString() ?: "") }
    val isoFormatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }
    val displayFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault()) }
    var startDateIso by remember { mutableStateOf(currentFilter.startDate.orEmpty()) }
    var endDateIso by remember { mutableStateOf(currentFilter.endDate.orEmpty()) }
    val startDisplay = remember(startDateIso) { isoDateToDisplay(startDateIso, isoFormatter, displayFormatter) }
    val endDisplay = remember(endDateIso) { isoDateToDisplay(endDateIso, isoFormatter, displayFormatter) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val startInitialMillis = remember(startDateIso) { parseIsoDateToMillis(startDateIso) }
    val startPickerState = rememberDatePickerState(initialSelectedDateMillis = startInitialMillis)
    val endInitialMillis = remember(endDateIso) { parseIsoDateToMillis(endDateIso) }
    val endPickerState = rememberDatePickerState(initialSelectedDateMillis = endInitialMillis)
    var selectedCats by remember { mutableStateOf(currentFilter.categories.toMutableSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Expenses") },
        text = {
            Column {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search query") },
                    singleLine = true
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        value = minText,
                        onValueChange = { minText = it },
                        label = { Text("Min $") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        value = maxText,
                        onValueChange = { maxText = it },
                        label = { Text("Max $") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    DateSelectorField(
                        label = "Start Date",
                        displayValue = startDisplay,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        onClick = { showStartPicker = true }
                    )
                    DateSelectorField(
                        label = "End Date",
                        displayValue = endDisplay,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        onClick = { showEndPicker = true }
                    )
                }
                if (categories.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 8.dp)
                    ) {
                        categories.sorted().forEach { cat ->
                            val selected = cat in selectedCats
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    if (!selectedCats.add(cat)) selectedCats.remove(cat)
                                },
                                label = { Text(cat) },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onApply(
                    query,
                    minText.toDoubleOrNull(),
                    maxText.toDoubleOrNull(),
                    startDateIso.ifBlank { null },
                    endDateIso.ifBlank { null },
                    selectedCats
                )
            }) { Text("Apply") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onClear) { Text("Clear") }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )

    if (showStartPicker) {
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startPickerState.selectedDateMillis?.let { millis ->
                        startDateIso = millisToIsoDate(millis)
                    }
                    showStartPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        startDateIso = ""
                        showStartPicker = false
                    }) { Text("Clear") }
                    TextButton(onClick = { showStartPicker = false }) { Text("Cancel") }
                }
            }
        ) {
            DatePicker(state = startPickerState)
        }
    }

    if (showEndPicker) {
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endPickerState.selectedDateMillis?.let { millis ->
                        endDateIso = millisToIsoDate(millis)
                    }
                    showEndPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        endDateIso = ""
                        showEndPicker = false
                    }) { Text("Clear") }
                    TextButton(onClick = { showEndPicker = false }) { Text("Cancel") }
                }
            }
        ) {
            DatePicker(state = endPickerState)
        }
    }
}

private fun iconForCategory(category: String): ImageVector = when (category) {
    "Dining" -> Icons.Filled.LocalDining
    "Transport" -> Icons.Filled.DirectionsCar
    "Entertainment" -> Icons.Filled.Movie
    else -> Icons.Filled.ShoppingCart
}

private fun formatAmount(amount: Double): String = "-$" + String.format("%.2f", amount)

@Composable
private fun DateSelectorField(
    label: String,
    displayValue: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = displayValue,
            onValueChange = { },
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select $label"
                )
            },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .clickable { onClick() }
        )
    }
}

private fun parseIsoDateToMillis(value: String): Long? {
    if (value.isBlank()) return null
    return runCatching {
        LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    }.getOrNull()
}

private fun millisToIsoDate(millis: Long): String =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .format(DateTimeFormatter.ISO_LOCAL_DATE)

private fun isoDateToDisplay(
    value: String,
    isoFormatter: DateTimeFormatter,
    displayFormatter: DateTimeFormatter
): String {
    if (value.isBlank()) return "Any"
    return runCatching { LocalDate.parse(value, isoFormatter).format(displayFormatter) }.getOrElse { value }
}

@Preview(showBackground = true)
@Composable
private fun ExpenseScreenPreview() {
    SavewiseTheme {
        ExpenseScreen()
    }
}
