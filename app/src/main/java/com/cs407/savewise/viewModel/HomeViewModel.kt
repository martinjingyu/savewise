package com.cs407.savewise.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.savewise.model.ExpenseRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {


    private val _recentExpenses = MutableStateFlow<List<ExpenseRecord>>(emptyList())
    private val _name = MutableStateFlow<String>("Martin")
    val recentExpenses: StateFlow<List<ExpenseRecord>> = _recentExpenses
    val userName: StateFlow<String> = _name

    private val _aiTip = MutableStateFlow("You're spending 20% more on dining this week.")
    val aiTip: StateFlow<String> = _aiTip

    init {
        viewModelScope.launch {
            _recentExpenses.value = listOf(
                ExpenseRecord(1, "Groceries at Market", "Shopping", 54.23, "2025-10-25"),
                ExpenseRecord(2, "Lunch with friends", "Dining", 18.90, "2025-10-25"),
                ExpenseRecord(3, "Gas Refill", "Transport", 42.10, "2025-10-24"),
                ExpenseRecord(4, "Movie Night", "Entertainment", 12.50, "2025-10-22"),
                ExpenseRecord(5, "Weekly Groceries", "Shopping", 76.45, "2025-10-20"),
            )
        }
    }


    fun addExpense(record: ExpenseRecord) {
        viewModelScope.launch {
            _recentExpenses.value = _recentExpenses.value + record
        }
    }


    fun clearExpenses() {
        viewModelScope.launch {
            _recentExpenses.value = emptyList()
        }
    }
}