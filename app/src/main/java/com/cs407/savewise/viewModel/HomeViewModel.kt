package com.cs407.savewise.viewModel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.savewise.data.ExpenseStorage
import com.cs407.savewise.model.ExpenseRecord
import com.cs407.savewise.service.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class HomeViewModel(application: Application) : AndroidViewModel(application) {


    private val _shouldOpenAddDialog = MutableStateFlow(false)

    private val storage = ExpenseStorage(application.applicationContext)
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
        viewModelScope.launch {
            storage.seedDefaultsIfEmpty()
            storage.expenses.collect { stored ->
                _recentExpenses.value = stored.sortedByDescending { it.date }
            }
        }
    }

    fun addExpense(record: ExpenseRecord) {
        viewModelScope.launch {
            storage.addExpense(record)
        }
    }

    fun clearExpenses() {
        viewModelScope.launch {
            storage.replaceAll(emptyList())
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

        _aiTip.value = "识别成功，正在理解内容…"

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
                        _aiTip.value = "语义理解完成，准备添加账单…"

                        // ✔ 通知 UI 可以打开 dialog
                        _shouldOpenAddDialog.value = true
                    }
                    .onFailure { e ->
                        println(e)
                        _aiTip.value = "GPT 错误：${e.message}"
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