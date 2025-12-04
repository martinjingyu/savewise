package com.cs407.savewise.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.savewise.data.ExpenseRepository
import com.cs407.savewise.model.ExpenseRecord
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ExpenseFilter(
    val query: String = "",
    val category: String? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val startDate: String? = null, // yyyy-MM-dd inclusive
    val endDate: String? = null,   // yyyy-MM-dd inclusive
    val categories: Set<String> = emptySet()
)

data class ExpensesUiState(
    val expenses: List<ExpenseRecord> = emptyList(),
    val filter: ExpenseFilter = ExpenseFilter()
)

class ExpensesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ExpenseRepository(application.applicationContext)
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var expensesJob: Job? = null
    private var currentOwnerUid: String? = null

    private val _allExpenses = MutableStateFlow<List<ExpenseRecord>>(emptyList())
    private val _filter = MutableStateFlow(ExpenseFilter())

    val filter: StateFlow<ExpenseFilter> = _filter

    private val _expenseHistory = MutableStateFlow<List<ExpenseRecord>>(emptyList())
    val expenseHistory: StateFlow<List<ExpenseRecord>> = _expenseHistory

    private val _filteredExpenses = MutableStateFlow<List<ExpenseRecord>>(emptyList())
    val filteredExpenses: StateFlow<List<ExpenseRecord>> = _filteredExpenses

    private val _categories = MutableStateFlow<Set<String>>(emptySet())
    val categories: StateFlow<Set<String>> = _categories

    init {
        observeAuthChanges()
    }

    fun setFilterQuery(query: String) {
        _filter.value = _filter.value.copy(query = query)
        recomputeDerived()
    }

    fun setFilterCategory(category: String?) {
        _filter.value = _filter.value.copy(category = category)
        recomputeDerived()
    }

    fun setSelectedCategories(categories: Set<String>) {
        _filter.value = _filter.value.copy(categories = categories)
        recomputeDerived()
    }

    fun toggleCategory(category: String) {
        val current = _filter.value.categories.toMutableSet()
        if (!current.add(category)) current.remove(category)
        _filter.value = _filter.value.copy(categories = current)
        recomputeDerived()
    }

    fun setAmountRange(min: Double?, max: Double?) {
        _filter.value = _filter.value.copy(minAmount = min, maxAmount = max)
        recomputeDerived()
    }

    fun setDateRange(start: String?, end: String?) {
        _filter.value = _filter.value.copy(startDate = start, endDate = end)
        recomputeDerived()
    }

    fun clearFilters() {
        _filter.value = ExpenseFilter()
        recomputeDerived()
    }

    fun addExpense(record: ExpenseRecord) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.addExpense(uid, record)
        }
    }

    fun deleteExpense(id: Long) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.deleteExpense(uid, id)
        }
    }

    fun updateExpense(updated: ExpenseRecord) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.updateExpense(uid, updated)
        }
    }

    fun adjustExpenseAmount(id: Long, newAmount: Double) {
        val target = getExpenseById(id) ?: return
        updateExpense(target.copy(amount = newAmount))
    }

    fun adjustExpenseMeta(
        id: Long,
        newTitle: String? = null,
        newCategory: String? = null,
        newDate: String? = null
    ) {
        val target = getExpenseById(id) ?: return
        updateExpense(
            target.copy(
                title = newTitle ?: target.title,
                category = newCategory ?: target.category,
                date = newDate ?: target.date
            )
        )
    }

    fun getExpenseById(id: Long): ExpenseRecord? = _allExpenses.value.firstOrNull { it.id == id }

    private fun recomputeDerived() {
        _expenseHistory.value = _allExpenses.value.sortedByDescending { it.date }
        _filteredExpenses.value = computeFiltered(_allExpenses.value, _filter.value)
        _categories.value = extractCategories(_allExpenses.value)
    }

    private fun computeFiltered(list: List<ExpenseRecord>, f: ExpenseFilter): List<ExpenseRecord> {
        return list.filter { rec ->
            val matchesQuery = f.query.isBlank() ||
                    rec.title.contains(f.query, ignoreCase = true) ||
                    rec.category.contains(f.query, ignoreCase = true)
            val matchesCategory = f.category?.let { rec.category.equals(it, true) } ?: true
            val matchesMultiCategory = if (f.categories.isEmpty()) true else rec.category in f.categories
            val matchesMin = f.minAmount?.let { rec.amount >= it } ?: true
            val matchesMax = f.maxAmount?.let { rec.amount <= it } ?: true
            val matchesStart = f.startDate?.let { rec.date >= it } ?: true
            val matchesEnd = f.endDate?.let { rec.date <= it } ?: true
            matchesQuery && matchesCategory && matchesMultiCategory && matchesMin && matchesMax && matchesStart && matchesEnd
        }.sortedByDescending { it.date }
    }

    private fun extractCategories(list: List<ExpenseRecord>): Set<String> =
        list.map { it.category }.toSet()

    private fun observeAuthChanges() {
        auth.addAuthStateListener { firebaseAuth ->
            subscribeToExpenses(firebaseAuth.currentUser?.uid)
        }
    }

    private fun subscribeToExpenses(ownerUid: String?) {
        expensesJob?.cancel()
        currentOwnerUid?.let { repository.stopRemoteSync(it) }
        currentOwnerUid = ownerUid

        if (ownerUid.isNullOrBlank()) {
            _allExpenses.value = emptyList()
            recomputeDerived()
            return
        }
        expensesJob = viewModelScope.launch {
            repository.startRemoteSync(ownerUid)
            repository.observeExpenses(ownerUid).collect { stored ->
                _allExpenses.value = stored
                recomputeDerived()
            }
        }
        viewModelScope.launch { repository.syncNow(ownerUid) }
    }
}

// no-op
