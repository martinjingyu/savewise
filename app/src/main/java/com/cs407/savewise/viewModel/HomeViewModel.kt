package com.cs407.savewise.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.savewise.data.ExpenseStorage
import com.cs407.savewise.model.ExpenseRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val storage = ExpenseStorage(application.applicationContext)

    private val _recentExpenses = MutableStateFlow<List<ExpenseRecord>>(emptyList())
    private val _name = MutableStateFlow("Martin")
    val recentExpenses: StateFlow<List<ExpenseRecord>> = _recentExpenses
    val userName: StateFlow<String> = _name

    private val _aiTip = MutableStateFlow("You're spending 20% more on dining this week.")
    val aiTip: StateFlow<String> = _aiTip

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
}
