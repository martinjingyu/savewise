package com.cs407.savewise.ui.screen



import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cs407.savewise.model.ExpenseRecord
import com.cs407.savewise.ui.component.AddExpenseDialog
import com.cs407.savewise.ui.component.AnimatedRecordButton
import com.cs407.savewise.ui.component.ExpenseList
import com.cs407.savewise.ui.component.MonthlyExpenseChart
import com.cs407.savewise.viewModel.HomeViewModel
import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Alignment


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStartSpeech: () -> Unit = {},
    onStopSpeech: () -> Unit = {},
    onSettingClick: () -> Unit = {},
) {
    val shouldOpenAddDialog by viewModel.shouldOpenAddDialog.collectAsState()
    val expenses by viewModel.recentExpenses.collectAsState()
    val aiTip by viewModel.aiTip.collectAsState()
    val speechText by viewModel.speechText.collectAsState()
    val name by viewModel.userName.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val expenseTitle by viewModel.expenseTitle.collectAsState()
    val expenseCategory by viewModel.expenseCategory.collectAsState()
    val expenseAmount by viewModel.expenseAmount.collectAsState()

    val activity = LocalActivity.current
    var showPermissionDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) {
                Toast.makeText(activity, "麦克风权限被拒绝", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            showPermissionDialog = true
        }
    }

    LaunchedEffect(shouldOpenAddDialog) {
        if (shouldOpenAddDialog) {
            showAddDialog = true
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("权限请求") },
            text = { Text("我们需要麦克风权限来识别语音添加账单项。") },
            confirmButton = {
                TextButton(onClick = {
                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    showPermissionDialog = false
                }) {
                    Text("允许")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            TopAppBar(
                title = { Text("Welcome $name") },
                actions = {
                    IconButton(onClick = onSettingClick) {
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
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {

            // Hero summary card
            item {
                SummaryCard(expenses = expenses)
            }

            // AI tip card
            item {
                AiTipCard(aiTip = aiTip)
            }

            // Big record button + label
            item {
                RecordSection(
                    onStartSpeech = onStartSpeech,
                    onStopSpeech = onStopSpeech
                )
            }

            // Monthly chart
            item {
                MonthlyExpenseChart(expenses = expenses)
            }

            if (expenses.isEmpty()) {
                // Empty state card instead of blank area
                item {
                    EmptyStateCard(
                        onAddClick = { showAddDialog = true }
                    )
                }
            } else {
                item {
                    Text(
                        text = "Recent expenses",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                item {
                    ExpenseList(
                        expenses = expenses,
                        modifier = Modifier.fillMaxWidth(),
                        onExpenseClick = { expense ->
                            println("Clicked on ${expense.title}")
                        }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddExpenseDialog(
                expense = ExpenseRecord(
                    id = -1,
                    title = expenseTitle,
                    category = expenseCategory,
                    amount = expenseAmount,
                    date = ""
                ),
                onDismiss = {
                    showAddDialog = false
                    viewModel.resetAddDialogFlag()
                },
                onSave = { newExpense ->
                    viewModel.addExpense(newExpense)
                    showAddDialog = false
                    viewModel.resetAddDialogFlag()
                }
            )
        }
    }
}

@Composable
private fun SummaryCard(expenses: List<ExpenseRecord>) {
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
                text = "This session",
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = String.format("$%.2f", total),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = if (count == 0) "No expenses yet"
                else "$count expense${if (count > 1) "s" else ""} recorded",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun AiTipCard(aiTip: String) {
    if (aiTip.isBlank()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = aiTip,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun RecordSection(
    onStartSpeech: () -> Unit,
    onStopSpeech: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedRecordButton(
            onStart = onStartSpeech,
            onStop = onStopSpeech,
            onFinished = {}
        )
        Text(
            text = "Tap to record a new expense",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun EmptyStateCard(onAddClick: () -> Unit) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No expenses yet",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Tap the + button to add your first expense.",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(onClick = onAddClick) {
                Text("Add expense")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {

}