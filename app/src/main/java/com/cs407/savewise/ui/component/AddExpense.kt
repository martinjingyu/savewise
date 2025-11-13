package com.cs407.savewise.ui.component

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cs407.savewise.model.ExpenseRecord
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
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
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    var time by remember { mutableStateOf("") }
    var showTimePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current


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
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") },
                    placeholder = { Text("mm/dd/yyyy") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Date"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    readOnly = true, // Typically opened by a dialog
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = Color.Gray,
                        disabledPlaceholderColor = Color.Gray,
                        disabledLabelColor = Color.Gray,
                        disabledTrailingIconColor = Color.Gray
                    )
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time") },
                    placeholder = { Text("--:--") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Time"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = true },
                    readOnly = true, // Typically opened by a dialog
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = Color.Gray,
                        disabledPlaceholderColor = Color.Gray,
                        disabledLabelColor = Color.Gray,
                        disabledTrailingIconColor = Color.Gray
                    )
                )
            }
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val selectedDate = datePickerState.selectedDateMillis
                                if (selectedDate != null) {
                                    val formatter = SimpleDateFormat(
                                        "MM/dd/yyyy",
                                        Locale.getDefault()
                                    )
                                    date = formatter.format(Date(selectedDate))
                                }
                                showDatePicker = false
                            }
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
            if (showTimePicker) {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)

                TimePickerDialog(
                    context,
                    { _, selectedHour: Int, selectedMinute: Int ->
                        time = String.format("%02d:%02d", selectedHour, selectedMinute)
                        showTimePicker = false
                    },
                    hour,
                    minute,
                    true // Use false for 12-hour format with AM/PM
                ).show()
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