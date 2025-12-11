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
import androidx.compose.foundation.clickable
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import androidx.compose.runtime.DisposableEffect
import com.cs407.savewise.service.SpeechRecognizerHelper
import androidx.compose.runtime.rememberCoroutineScope
import com.cs407.savewise.service.WavAudioRecorder
import com.cs407.savewise.service.WhisperApi
import com.cs407.savewise.service.RecordingStorageManager
import kotlinx.coroutines.launch
import java.io.File



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
    val recordingStorageDays by viewModel.recordingStorageDays.collectAsState()

    val isRecording by viewModel.isRecording.collectAsState()

    val activity = LocalActivity.current
    var showPermissionDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current


    val autoPauseEnabled by viewModel.autoPauseEnabled.collectAsState()

    val scope = rememberCoroutineScope()

    // Manual recording mode (no auto-pause) uses raw audio + Whisper
    val audioFile = remember { File(context.cacheDir, "expense_voice.wav") }

    val whisperRecorder = remember {
        WavAudioRecorder(
            context = context,
            file = audioFile,
            onError = { msg -> viewModel.onSpeechError(msg) }
        )
    }
    val recordingStorage = remember { RecordingStorageManager(context) }


    var stopRecording: () -> Unit = {}

    val speechHelper = remember(autoPauseEnabled) {
        SpeechRecognizerHelper(
            context = context,
            onResult = { text ->
                viewModel.onSpeechResult(text)
                // Only auto-stop the button when Auto pause is ON
                if (autoPauseEnabled) {
                    stopRecording()
                }
            },
            onError = { msg ->
                viewModel.onSpeechError(msg)
                // On error we *always* stop to avoid a stuck recording state
                stopRecording()
            },
            onAutoStop = {
                if (autoPauseEnabled) {
                    // Only auto-pause on 2s silence when setting is ON
                    stopRecording()
                }
                // When Auto pause is OFF, ignore silence and leave the button running
            }
        )
    }


    // Now that speechHelper exists, define what stopRecording actually does.
    stopRecording = {
        if (viewModel.isRecording.value) {
            speechHelper.stop()
            viewModel.onSpeechStop()
        }
    }
    DisposableEffect(autoPauseEnabled) {
        onDispose {
            if (autoPauseEnabled) {
                // SpeechRecognizer mode
                stopRecording()
                speechHelper.destroy()
            } else {
                // Manual Whisper mode – just ensure we’re not still recording
                whisperRecorder.stop()
                viewModel.onSpeechStop()
            }
        }
    }




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
                title = {
                    Column {
                        // Dynamic Greeting Logic
                        val currentHour = java.time.LocalTime.now().hour
                        val greeting = when (currentHour) {
                            in 5..11 -> "Good Morning"
                            in 12..17 -> "Good Afternoon"
                            else -> "Good Evening"
                        }

                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                    }
                },
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
                AiTipCard(aiTip = aiTip, viewModel = viewModel)
            }

            // Big record button + label
            item {
                RecordSection(
                    isRecording = isRecording,
                    onToggleRecording = {
                        if (autoPauseEnabled) {
                            // ✅ Auto-pause mode: use SpeechRecognizer
                            if (isRecording) {
                                stopRecording()
                            } else {
                                viewModel.onSpeechStart()
                                speechHelper.start()
                            }
                        } else {
                            // ✅ Manual mode: use WavAudioRecorder + Whisper, NO auto-pause
                            if (isRecording) {
                            // User taps to STOP
                            viewModel.onSpeechStop()
                            whisperRecorder.stop()

                            // Transcribe with Whisper and feed it into ViewModel
                            scope.launch {
                                try {
                                    // save a local copy and prune old recordings based on settings
                                    recordingStorage.saveCopy(audioFile)
                                    recordingStorage.cleanupOlderThan(recordingStorageDays)

                                    val text = WhisperApi.transcribe(audioFile)
                                    viewModel.onSpeechResult(text)
                                } catch (e: Exception) {
                                    viewModel.onSpeechError("Transcription failed: ${e.message}")
                                }
                                }
                            } else {
                                // User taps to START
                                viewModel.onSpeechStart()
                                whisperRecorder.start()
                            }
                        }
                    }
                )

            }




            // Monthly chart
            if (expenses.isNotEmpty()) {
                // Monthly chart
                item {
                    MonthlyExpenseChart(expenses = expenses)
                }

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
            } else {
                // Replaces the old empty chart and empty card with a single illustration
                item {
                    EmptyStateIllustration()
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
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF4F8CF9), // Brand Blue
                            Color(0xFF84B2FF)  // Lighter Blue
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "This Month's Spending",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )

                // Amount Display
                Text(
                    text = String.format("$%.2f", total),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        // Monospace to prevent jumping
                        fontFeatureSettings = "tnum"
                    ),
                    color = Color.White,
                    fontSize = 36.sp
                )

                if (total == 0.0) {
                    Text(
                        text = "Ready to track",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AiTipCard(aiTip: String, viewModel: HomeViewModel) {
    if (aiTip.isBlank()) return
    val containerColor = Color(0xFFFFECB3) // Light amber/orange
    val contentColor = Color(0xFF6D4C00)   // Darker amber/brown
    val iconColor = Color(0xFFEF6C00)      // Deep Orange
    var showDialog by remember { mutableStateOf(false) }
    var userInput by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top, // Align to top in case text wraps
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = "Insight",
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier
                    .clickable { showDialog = true }
                    .padding(8.dp)
            ) {
                Text(
                    text = "Spending Insight",
                    style = MaterialTheme.typography.labelMedium,
                    color = iconColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = aiTip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor
                )
            }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Ask AI") },
                    text = {
                        Column {
                            Text("Your Input:")
                            Spacer(modifier = Modifier.height(4.dp))
                            TextField(
                                value = userInput,
                                onValueChange = {
                                    userInput = it
                                                },
                                placeholder = { Text("Ask me anything...") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("AI Suggestion:")
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = aiTip,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {viewModel.handelUserInput(userInput)}) {
                            Text("Send")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Close")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun RecordSection(
    isRecording: Boolean,
    onToggleRecording: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(0.6f),
            contentAlignment = Alignment.Center
        ) {
            AnimatedRecordButton(
                isRecording = isRecording,
                onToggle = onToggleRecording
            )
        }
        Text(
            text = "Tap to record a new expense",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
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

@Composable
private fun EmptyStateIllustration() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Placeholder Illustration (Piggy Bank Icon)
        // Ideally, replace this Icon with an Image(painter = painterResource(id = R.drawable.illustration_empty_wallet)...)
        Icon(
            imageVector = Icons.Default.Savings, // A sleeping piggy bank metaphor
            contentDescription = "No expenses",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No expenses yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap + or use the microphone to start tracking.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {

}
