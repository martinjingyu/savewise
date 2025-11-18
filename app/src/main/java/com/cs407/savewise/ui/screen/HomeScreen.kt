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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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

        // 如果没有权限，弹出对话框
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
                TextButton(onClick = {
                    showPermissionDialog = false
                }) {
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


            item {
                AnimatedRecordButton(
                    onStart = { onStartSpeech() },
                    onStop = { onStopSpeech()},
                    onFinished = {

                    }
                )
            }

            item {
                MonthlyExpenseChart(expenses = expenses)
            }


            item {
                Text(
                    text = "Recent Expenses",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                )
            }


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
                    title = expenseTitle,
                    category = expenseCategory,
                    amount = expenseAmount,
                    date = ""
                ),
                onDismiss = { showAddDialog = false
                    viewModel.resetAddDialogFlag() },
                onSave = { newExpense ->
                    viewModel.addExpense(newExpense)
                    showAddDialog = false
                    viewModel.resetAddDialogFlag()
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {

}