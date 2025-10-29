package com.cs407.savewise.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cs407.savewise.ui.theme.SavewiseTheme

private data class Expense(
    val id: Int,
    val title: String,
    val category: String,
    val amount: Double,
    val date: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen() {
    val sampleExpenses = rememberSampleExpenses()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Expenses") }
            )
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            if (sampleExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No expenses yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sampleExpenses, key = { it.id }) { expense ->
                        ExpenseRow(expense)
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseRow(expense: Expense) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
                text = "${expense.category} • ${expense.date}",
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
private fun rememberSampleExpenses(): List<Expense> {
    // UI-only placeholder data for preview/runtime until wired to data layer
    return listOf(
        Expense(1, "Groceries at Market", "Shopping", 54.23, "2025-10-25"),
        Expense(2, "Lunch with friends", "Dining", 18.90, "2025-10-25"),
        Expense(3, "Gas Refill", "Transport", 42.10, "2025-10-24"),
        Expense(4, "Movie Night", "Entertainment", 12.50, "2025-10-22"),
        Expense(5, "Weekly Groceries", "Shopping", 76.45, "2025-10-20"),
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
