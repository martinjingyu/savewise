package com.cs407.savewise.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
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
        Surface(modifier = Modifier.padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (expenses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No expenses match your filter",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(expenses, key = { it.id }) { expense ->
                            ExpenseRow(
                                expense = expense,
                                onClick = { editing = expense },
                                onLongPress = { pendingDelete = expense }
                            )
                            Divider()
                        }
                    }
                }
            }
        }
    }

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
private fun EditExpenseDialog(
    expense: ExpenseRecord,
    onDismiss: () -> Unit,
    onSave: (ExpenseRecord) -> Unit
) {
    var title by remember { mutableStateOf(expense.title) }
    var category by remember { mutableStateOf(expense.category) }
    var date by remember { mutableStateOf(expense.date) }
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
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date YYYY-MM-DD") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
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
                            date = date,
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
}

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
    var startDate by remember { mutableStateOf(currentFilter.startDate.orEmpty()) }
    var endDate by remember { mutableStateOf(currentFilter.endDate.orEmpty()) }
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
                    OutlinedTextField(
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start YYYY-MM-DD") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End YYYY-MM-DD") },
                        singleLine = true
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
                    startDate.ifBlank { null },
                    endDate.ifBlank { null },
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
}

private fun iconForCategory(category: String): ImageVector = when (category) {
    "Dining" -> Icons.Filled.LocalDining
    "Transport" -> Icons.Filled.DirectionsCar
    "Entertainment" -> Icons.Filled.Movie
    else -> Icons.Filled.ShoppingCart
}

private fun formatAmount(amount: Double): String = "-$" + String.format("%.2f", amount)

@Preview(showBackground = true)
@Composable
private fun ExpenseScreenPreview() {
    SavewiseTheme {
        ExpenseScreen()
    }
}
