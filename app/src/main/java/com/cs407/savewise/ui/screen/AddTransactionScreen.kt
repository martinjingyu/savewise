package com.cs407.savewise.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cs407.savewise.ui.theme.SavewiseTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.text.format
import java.util.Date
import java.util.Locale
import androidx.compose.material3.OutlinedTextFieldDefaults
import android.app.TimePickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransaction(
    onBackClicked: () -> Unit = {},
    onProfileClicked: () -> Unit = {}
) {
    Surface(
        color = Color.Transparent
    ) {
        var subject by remember { mutableStateOf("") }
        var detail by remember { mutableStateOf("") }
        var spend by remember { mutableStateOf("") }
        var date by remember { mutableStateOf("") }
        var time by remember { mutableStateOf("") }
        var showDatePicker by remember { mutableStateOf(false) }
        var showTimePicker by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val datePickerState = rememberDatePickerState()



        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Add Transaction") },
                    navigationIcon = {
                        IconButton(onClick = onBackClicked) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {onProfileClicked}) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFFF3EFFF) // Light purple background
                    )
                )
            },
            containerColor = Color(0xFFF3EFFF) // Matching background color
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Subject Field
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    placeholder = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Detail Field
                OutlinedTextField(
                    value = detail,
                    onValueChange = { detail = it },
                    label = { Text("Detail") },
                    placeholder = { Text("Add description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Spend Field
                OutlinedTextField(
                    value = spend,
                    onValueChange = { spend = it },
                    label = { Text("Spend") },
                    placeholder = { Text("Add spend") },
                    trailingIcon = { Text("$", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Date Field
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

                Spacer(modifier = Modifier.height(16.dp))

                // Time Field
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

                Spacer(modifier = Modifier.weight(1f)) // Pushes buttons to the bottom

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { onBackClicked() }, // Or a dedicated cancel action
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEADDFF)), // Lighter purple
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color.Black)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = { /* TODO: Add transaction logic */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEADDFF)), // Lighter purple
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add", color = Color.Black)
                    }
                }
            }
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

        // --- Time Picker Dialog ---
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
    }
}

@Preview(showBackground = true)
@Composable
fun AddTransactionPreview() {
    SavewiseTheme {
        AddTransaction()
    }
}