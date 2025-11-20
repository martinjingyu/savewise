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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Context.expensesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "expenses_data_store"
)

class ExpenseStorage(private val context: Context) {

    private val allExpenses: Flow<List<ExpenseRecord>> =
        context.expensesDataStore.data.map { prefs -> decodeExpenses(prefs[EXPENSES_KEY]) }

    fun expensesForUser(ownerUid: String): Flow<List<ExpenseRecord>> =
        allExpenses.map { list -> list.filter { it.ownerUid == ownerUid } }

    suspend fun seedDefaultsIfEmpty() {
        context.expensesDataStore.edit { prefs ->
            if (!prefs.contains(EXPENSES_KEY)) {
                prefs[EXPENSES_KEY] = json.encodeToString(emptyList<ExpenseRecord>())
            }
        }
    }

    suspend fun addExpense(ownerUid: String, expense: ExpenseRecord) {
        updateList(ownerUid) { owned, all ->
            val record = ensureId(expense, all)
            owned + record
        }
    }

    suspend fun updateExpense(ownerUid: String, expense: ExpenseRecord) {
        updateList(ownerUid) { owned, _ ->
            owned.map { if (it.id == expense.id) expense else it }
        }
    }

    suspend fun deleteExpense(ownerUid: String, id: Long) {
        updateList(ownerUid) { owned, _ -> owned.filterNot { it.id == id } }
    }

    suspend fun replaceAll(ownerUid: String, expenses: List<ExpenseRecord>) {
        updateList(ownerUid) { _, _ -> expenses }
    }

    private suspend fun updateList(
        ownerUid: String,
        transform: (owned: List<ExpenseRecord>, all: List<ExpenseRecord>) -> List<ExpenseRecord>
    ) {
        context.expensesDataStore.edit { prefs ->
            val current = decodeExpenses(prefs[EXPENSES_KEY])
            val (owned, others) = current.partition { it.ownerUid == ownerUid }
            val updatedOwned = transform(owned, current)
                .map { ensureOwnership(ensureDateFormat(it), ownerUid) }
            prefs[EXPENSES_KEY] = json.encodeToString((others + updatedOwned).map(::ensureDateFormat))
        }
    }

    private fun decodeExpenses(raw: String?): List<ExpenseRecord> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching { json.decodeFromString<List<ExpenseRecord>>(raw) }
            .getOrElse { emptyList() }
            .map(::ensureDateFormat)
    }

    private fun ensureId(expense: ExpenseRecord, current: List<ExpenseRecord>): ExpenseRecord {
        if (expense.id > 0) return expense
        val nextId = (current.maxOfOrNull { it.id } ?: 0L) + 1L
        return expense.copy(id = nextId)
    }

    private fun ensureOwnership(expense: ExpenseRecord, ownerUid: String): ExpenseRecord {
        if (expense.ownerUid == ownerUid) return expense
        return expense.copy(ownerUid = ownerUid)
    }

    companion object {
        private val EXPENSES_KEY = stringPreferencesKey("expenses_json")
        private val json = Json { ignoreUnknownKeys = true }
        private val isoFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
        private val legacyFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.getDefault())

        private fun ensureDateFormat(expense: ExpenseRecord): ExpenseRecord {
            val normalized = normalizeDate(expense.date)
            if (normalized == expense.date) return expense
            return expense.copy(date = normalized)
        }

        private fun normalizeDate(raw: String): String {
            val trimmed = raw.trim()
            if (trimmed.isBlank()) return trimmed
            parseDate(trimmed, isoFormatter)?.let { return it.format(isoFormatter) }
            parseDate(trimmed, legacyFormatter)?.let { return it.format(isoFormatter) }
            return trimmed
        }

        private fun parseDate(value: String, formatter: DateTimeFormatter): LocalDate? =
            runCatching { LocalDate.parse(value, formatter) }.getOrNull()
    }
}
