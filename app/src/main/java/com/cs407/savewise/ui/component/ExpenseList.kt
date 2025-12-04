package com.cs407.savewise.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.savewise.model.ExpenseRecord


@Composable
fun ExpenseList(
    expenses: List<ExpenseRecord>,
    modifier: Modifier = Modifier,
    onExpenseClick: (ExpenseRecord) -> Unit
) {
    Surface(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No expenses match your filter")
                }
            } else {
                expenses.forEach { expense ->
                    ExpenseRow(
                        expense = expense,
                        onClick = { onExpenseClick(expense) }
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}


@Composable
private fun ExpenseRow(expense: ExpenseRecord, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            val icon = iconForCategory(expense.category)
            Icon(
                imageVector = icon,
                contentDescription = expense.category,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = expense.title.ifBlank { "Unknown" },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${expense.category} • ${expense.date}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = formatAmount(expense.amount),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 17.sp
            ),
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD32F2F)
        )
    }
}

fun formatAmount(amount: Double): String = "-$" + String.format("%.2f", amount)


private fun iconForCategory(category: String): ImageVector = when (category) {
    "Dining" -> Icons.Filled.LocalDining
    "Transport" -> Icons.Filled.DirectionsCar
    "Entertainment" -> Icons.Filled.Movie
    else -> Icons.Filled.ShoppingCart
}
