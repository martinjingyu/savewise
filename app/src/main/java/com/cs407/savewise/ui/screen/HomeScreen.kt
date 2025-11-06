package com.cs407.savewise.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs407.savewise.model.ExpenseRecord
import com.cs407.savewise.ui.component.AddExpenseDialog
import com.cs407.savewise.ui.component.AnimatedRecordButton
import com.cs407.savewise.ui.component.ExpenseList
import com.cs407.savewise.viewModel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onMicClick: () -> Unit = {},
    onSettingClick: ()  -> Unit = {},
) {
    val expenses by viewModel.recentExpenses.collectAsState()
    val aiTip by viewModel.aiTip.collectAsState()
    val name by viewModel.userName.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            TopAppBar(
                title = { Text("Welcome $name") },
                actions = {
                    IconButton(onClick = { onSettingClick() }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(
                            color = Color(0x3348A9E6),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = aiTip,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // 麦克风输入按钮
            item {
                AnimatedRecordButton()
            }

            // 月度图表占位
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp)
                        .background(Color(0xAA11AF11)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Monthly Pie Chart Placeholder")
                }
            }

            // 最近支出
            item {
                Text(
                    text = "Recent Expenses",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                )
            }

            // 支出列表
            item {
                ExpenseList(
                    expenses = expenses,
                    modifier = Modifier,
                    onExpenseClick = { expense ->
                        println("Clicked on ${expense.title}")
                    }
                )
            }
        }
        if (showAddDialog) {
            AddExpenseDialog(
                expense = ExpenseRecord(
                    id = -1,
                    title = "",
                    category = "",
                    amount = 0.0,
                    date = ""
                ),
                onDismiss = { showAddDialog = false },
                onSave = { newExpense ->
                    viewModel.addExpense(newExpense)
                    showAddDialog = false
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}