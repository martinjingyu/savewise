package com.cs407.savewise.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.filled.Settings

import androidx.compose.material.icons.filled.PlayArrow
import com.cs407.savewise.model.ExpenseItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
//    viewModel: HomeViewModel = /* TODO: your DI logic */,
    onMicClick: () -> Unit = {}
) {
//    val expenses = remember { viewModel.recentExpenses.collectAsState() }
    val expenses = remember {
        mutableStateOf(
            listOf(
                ExpenseItem("Coffee", "$4.50", "Food"),
                ExpenseItem("Lunch", "$12.00", "Dining"),
                ExpenseItem("Bus Ticket", "$2.00", "Transport"),
                ExpenseItem("Groceries", "$30.00", "Shopping"),
                ExpenseItem("Movie", "$10.00", "Entertainment"),
            )
        )
    }
    val aiTip = "You're spending 20% more on dining this week."

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            TopAppBar(
                title = { Text("SaveWise") },
                actions = {
                    IconButton(onClick = { /* TODO: open settings */ }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onMicClick) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Voice Input")
            }
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

            // 1. AI 分析语句
            item {
                Text(
                    text = aiTip,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }

            // 2. 麦克风输入（大图标按钮 + 提示）
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onMicClick,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.size(200.dp)
                    ) {
                        Text("🎤", fontSize = 100.sp)
                    }
                }
            }


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


            item {
                Text(
                    text = "Recent Expenses",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                )
            }


            items(expenses.value) { expense ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = expense.description)
                        Text(text = "${expense.amount} on ${expense.category}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }


            if (expenses.value.isEmpty()) {
                item {
                    Text(
                        text = "No recent expenses recorded.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}