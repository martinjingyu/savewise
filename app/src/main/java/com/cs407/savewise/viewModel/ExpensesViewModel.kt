package com.cs407.savewise.viewModel

import androidx.lifecycle.ViewModel
import com.cs407.savewise.model.ExpenseRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ExpenseFilter(
    val query: String = "",
    val category: String? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val startDate: String? = null, // yyyy-MM-dd inclusive
    val endDate: String? = null,   // yyyy-MM-dd inclusive
    val categories: Set<String> = emptySet() // multi-select; empty means all
)

data class ExpensesUiState(
    val expenses: List<ExpenseRecord> = emptyList(),
    val filter: ExpenseFilter = ExpenseFilter()
)

class ExpensesViewModel : ViewModel() {
    private val _allExpenses = MutableStateFlow(sampleExpenses())
    private val _filter = MutableStateFlow(ExpenseFilter())

    val filter: StateFlow<ExpenseFilter> = _filter

    private val _expenseHistory = MutableStateFlow(_allExpenses.value.sortedByDescending { it.date })
    val expenseHistory: StateFlow<List<ExpenseRecord>> = _expenseHistory

    private val _filteredExpenses = MutableStateFlow(computeFiltered(_allExpenses.value, _filter.value))
    val filteredExpenses: StateFlow<List<ExpenseRecord>> = _filteredExpenses

    private val _categories = MutableStateFlow(extractCategories(_allExpenses.value))
    val categories: StateFlow<Set<String>> = _categories

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
        _allExpenses.value = _allExpenses.value + record
        recomputeDerived()
    }

    fun deleteExpense(id: Long) {
        _allExpenses.value = _allExpenses.value.filterNot { it.id == id }
        recomputeDerived()
    }

    fun updateExpense(updated: ExpenseRecord) {
        _allExpenses.value = _allExpenses.value.map { if (it.id == updated.id) updated else it }
        recomputeDerived()
    }

    fun adjustExpenseAmount(id: Long, newAmount: Double) {
        _allExpenses.value = _allExpenses.value.map { rec ->
            if (rec.id == id) rec.copy(amount = newAmount) else rec
        }
        recomputeDerived()
    }

    fun adjustExpenseMeta(
        id: Long,
        newTitle: String? = null,
        newCategory: String? = null,
        newDate: String? = null
    ) {
        _allExpenses.value = _allExpenses.value.map { rec ->
            if (rec.id == id) rec.copy(
                title = newTitle ?: rec.title,
                category = newCategory ?: rec.category,
                date = newDate ?: rec.date
            ) else rec
        }
        recomputeDerived()
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

    private fun sampleExpenses(): List<ExpenseRecord> = listOf(
        ExpenseRecord(1, "Groceries at Market", "Shopping", 54.23, "2025-10-25"),
        ExpenseRecord(2, "Lunch with friends", "Dining", 18.90, "2025-10-25"),
        ExpenseRecord(3, "Gas Refill", "Transport", 42.10, "2025-10-24"),
        ExpenseRecord(4, "Movie Night", "Entertainment", 12.50, "2025-10-22"),
        ExpenseRecord(5, "Weekly Groceries", "Shopping", 76.45, "2025-10-20"),
    )
}

// no-op
