package com.cs407.savewise.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.savewise.data.ExpenseRepository
import com.cs407.savewise.model.ExpenseRecord
import com.cs407.savewise.service.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import com.cs407.savewise.data.UserPreferencesRepository
class HomeViewModel(application: Application) : AndroidViewModel(application) {


    private val _shouldOpenAddDialog = MutableStateFlow(false)

    private val repository = ExpenseRepository(application.applicationContext)
    private var currentOwnerUid: String? = null
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var expensesJob: Job? = null
    private val chatRepository = ChatRepository()
    private val _recentExpenses = MutableStateFlow<List<ExpenseRecord>>(emptyList())
    private val _name = MutableStateFlow("")
    private val _aiTip = MutableStateFlow("You're spending 20% more on dining this week.")

    private val _expenseTitle = MutableStateFlow("")
    val expenseTitle: StateFlow<String> = _expenseTitle

    private val _expenseCategory = MutableStateFlow("")
    val expenseCategory: StateFlow<String> = _expenseCategory

    private val _expenseAmount = MutableStateFlow(0.0)
    val expenseAmount: StateFlow<Double> = _expenseAmount

    private val _speechText = MutableStateFlow("")
    val speechText: StateFlow<String> = _speechText
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording
    val recentExpenses: StateFlow<List<ExpenseRecord>> = _recentExpenses
    val userName: StateFlow<String> = _name
    val aiTip: StateFlow<String> = _aiTip
    private val userPreferencesRepository = UserPreferencesRepository(application)

    private val _autoPauseEnabled = MutableStateFlow(false)
    val autoPauseEnabled: StateFlow<Boolean> = _autoPauseEnabled

    val shouldOpenAddDialog: StateFlow<Boolean> = _shouldOpenAddDialog
    init {
        observeAuthChanges()

        viewModelScope.launch {
            userPreferencesRepository.preferencesFlow.collect { prefs ->
                _autoPauseEnabled.value = prefs.autoRecording
            }
        }
    }

    private fun observeAuthChanges() {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _name.value = user?.displayName ?: ""
            subscribeToExpenses(user?.uid)
        }
    }

    fun refreshUserNameFromFirebase() {
        val user = auth.currentUser
        _name.value = user?.displayName ?: ""
    }


    private fun subscribeToExpenses(ownerUid: String?) {
        expensesJob?.cancel()
        currentOwnerUid?.let { repository.stopRemoteSync(it) }
        currentOwnerUid = ownerUid

        if (ownerUid.isNullOrBlank()) {
            _recentExpenses.value = emptyList()
            return
        }
        repository.startRemoteSync(ownerUid)
        expensesJob = viewModelScope.launch {
            repository.observeExpenses(ownerUid).collect { stored ->
                _recentExpenses.value = stored.sortedByDescending { it.date }
            }
        }
        viewModelScope.launch { repository.syncNow(ownerUid) }
    }

    fun addExpense(record: ExpenseRecord) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.addExpense(uid, record)
        }
    }

    fun clearExpenses() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _recentExpenses.value.forEach { exp ->
                repository.deleteExpense(uid, exp.id)
            }
        }
    }

    fun resetAddDialogFlag() {
        _shouldOpenAddDialog.value = false
    }


    // ------------------------------------------------
    // 🔊 语音识别回调 —— 从 SpeechHelper 传进来
    // ------------------------------------------------


    fun onSpeechResult(text: String) {
        println("🎤【识别成功】：$text")

        _aiTip.value = "AI is analysing…"

        // ⭐ 先把原始识别文字显示
        _speechText.value = text

        // ⭐ 调 GPT（需要启动协程）
        viewModelScope.launch {
            chatRepository.sendPromptToGpt(text).collect { result ->
                result
                    .onSuccess { processed ->
                        val obj = JSONObject(processed)

                        val title = obj.optString("title")
                        val category = obj.optString("category")
                        val amount = obj.optDouble("amount", 0.0)
                        println("🤖 GPT 回复：$processed")
                        println("🧾 Parsed: title=$title, category=$category, amount=$amount")

                        _expenseTitle.value = title
                        _expenseCategory.value = category
                        _expenseAmount.value = amount

                        // ✔ 用 GPT 处理后的内容更新
                        _speechText.value = processed
                        _aiTip.value = "Understand，trying to create expense…"

                        // ✔ 通知 UI 可以打开 dialog
                        _shouldOpenAddDialog.value = true
                    }
                    .onFailure { e ->
                        println(e)
                        _aiTip.value = "GPT Error：${e.message}"
                    }
            }
        }
    }

    fun onSpeechError(error: String) {
        println("❌【语音错误】：$error")
        _aiTip.value = "Speech error: $error"
        _isRecording.value = false
    }

    fun onSpeechStart() {
        println("🎙️【开始录音】")
        _aiTip.value = "Listening..."
        _isRecording.value = true
    }

    fun onSpeechStop() {
        println("🛑【停止录音】")
        _isRecording.value = false
    }
    fun handelUserInput(string: String) {
        viewModelScope.launch {
            chatRepository.sendTextToGpt(string, _recentExpenses.value).collect { result ->
                result
                    .onSuccess { processed ->

                        println("🤖 GPT 回复：$processed")

                        // ✔ 用 GPT 处理后的内容更新
                        _aiTip.value = processed

                    }
                    .onFailure { e ->
                        println(e)
                        _aiTip.value = "GPT Error：${e.message}"
                    }
            }
        }
        _aiTip.value = "Responding..."
    }
}
