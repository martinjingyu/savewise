package com.cs407.savewise.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.savewise.data.ExpenseStorage
import com.cs407.savewise.model.ExpenseRecord
import com.cs407.savewise.service.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class HomeViewModel(application: Application) : AndroidViewModel(application) {


    private val _shouldOpenAddDialog = MutableStateFlow(false)

    private val storage = ExpenseStorage(application.applicationContext)
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var expensesJob: Job? = null
    private val chatRepository = ChatRepository()
    private val _recentExpenses = MutableStateFlow<List<ExpenseRecord>>(emptyList())
    private val _name = MutableStateFlow("Martin")
    private val _aiTip = MutableStateFlow("You're spending 20% more on dining this week.")

    private val _expenseTitle = MutableStateFlow("")
    val expenseTitle: StateFlow<String> = _expenseTitle

    private val _expenseCategory = MutableStateFlow("")
    val expenseCategory: StateFlow<String> = _expenseCategory

    private val _expenseAmount = MutableStateFlow(0.0)
    val expenseAmount: StateFlow<Double> = _expenseAmount

    private val _speechText = MutableStateFlow("")
    val speechText: StateFlow<String> = _speechText
    val recentExpenses: StateFlow<List<ExpenseRecord>> = _recentExpenses
    val userName: StateFlow<String> = _name
    val aiTip: StateFlow<String> = _aiTip


    val shouldOpenAddDialog: StateFlow<Boolean> = _shouldOpenAddDialog
    init {
        observeAuthChanges()
    }

    private fun observeAuthChanges() {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _name.value = user?.displayName ?: ""
            subscribeToExpenses(user?.uid)
        }
    }

    private fun subscribeToExpenses(ownerUid: String?) {
        expensesJob?.cancel()
        if (ownerUid.isNullOrBlank()) {
            _recentExpenses.value = emptyList()
            return
        }
        expensesJob = viewModelScope.launch {
            storage.seedDefaultsIfEmpty()
            storage.expensesForUser(ownerUid).collect { stored ->
                _recentExpenses.value = stored.sortedByDescending { it.date }
            }
        }
    }

    fun addExpense(record: ExpenseRecord) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            storage.addExpense(uid, record)
        }
    }

    fun clearExpenses() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            storage.replaceAll(uid, emptyList())
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
    }

    fun onSpeechStart() {
        println("🎙️【开始录音】")
        _aiTip.value = "Listening..."
    }

    fun onSpeechStop() {
        println("🛑【停止录音】")
    }

}
