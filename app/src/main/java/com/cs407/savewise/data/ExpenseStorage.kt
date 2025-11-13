package com.cs407.savewise.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cs407.savewise.model.ExpenseRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private val Context.expensesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "expenses_data_store"
)

class ExpenseStorage(private val context: Context) {

    val expenses: Flow<List<ExpenseRecord>> =
        context.expensesDataStore.data.map { prefs -> decodeExpenses(prefs[EXPENSES_KEY]) }

    suspend fun seedDefaultsIfEmpty() {
        context.expensesDataStore.edit { prefs ->
            if (!prefs.contains(EXPENSES_KEY)) {
                prefs[EXPENSES_KEY] = json.encodeToString(DEFAULT_EXPENSES)
            }
        }
    }

    suspend fun addExpense(expense: ExpenseRecord) {
        updateList { current ->
            val record = ensureId(expense, current)
            current + record
        }
    }

    suspend fun updateExpense(expense: ExpenseRecord) {
        updateList { current ->
            current.map { if (it.id == expense.id) expense else it }
        }
    }

    suspend fun deleteExpense(id: Long) {
        updateList { current -> current.filterNot { it.id == id } }
    }

    suspend fun replaceAll(expenses: List<ExpenseRecord>) {
        context.expensesDataStore.edit { prefs ->
            prefs[EXPENSES_KEY] = json.encodeToString(expenses)
        }
    }

    private suspend fun updateList(transform: (List<ExpenseRecord>) -> List<ExpenseRecord>) {
        context.expensesDataStore.edit { prefs ->
            val current = decodeExpenses(prefs[EXPENSES_KEY])
            val updated = transform(current)
            prefs[EXPENSES_KEY] = json.encodeToString(updated)
        }
    }

    private fun decodeExpenses(raw: String?): List<ExpenseRecord> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching { json.decodeFromString<List<ExpenseRecord>>(raw) }.getOrElse { emptyList() }
    }

    private fun ensureId(expense: ExpenseRecord, current: List<ExpenseRecord>): ExpenseRecord {
        if (expense.id > 0) return expense
        val nextId = (current.maxOfOrNull { it.id } ?: 0L) + 1L
        return expense.copy(id = nextId)
    }

    companion object {
        private val EXPENSES_KEY = stringPreferencesKey("expenses_json")
        private val json = Json { ignoreUnknownKeys = true }
        private val DEFAULT_EXPENSES = listOf(
            ExpenseRecord(1, "Groceries at Market", "Shopping", 54.23, "2025-10-25"),
            ExpenseRecord(2, "Lunch with friends", "Dining", 18.90, "2025-10-25"),
            ExpenseRecord(3, "Gas Refill", "Transport", 42.10, "2025-10-24"),
            ExpenseRecord(4, "Movie Night", "Entertainment", 12.50, "2025-10-22"),
            ExpenseRecord(5, "Weekly Groceries", "Shopping", 76.45, "2025-10-20")
        )
    }
}
