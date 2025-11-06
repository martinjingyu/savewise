package com.cs407.savewise.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cs407.savewise.model.ExpenseRecord

@Composable
fun AddExpenseDialog(
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
        title = { Text("Add New Expense") },
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