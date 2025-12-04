package com.cs407.savewise.data

import com.cs407.savewise.model.ExpenseRecord
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class ExpenseStorage(
    // Context no longer required for persistence, retained to avoid breaking call sites
    @Suppress("UnusedParameter") private val context: android.content.Context
) {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun expensesForUser(ownerUid: String, pageSize: Long? = null): Flow<List<ExpenseRecord>> {
        val baseQuery = collection(ownerUid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
        val query = pageSize?.let { baseQuery.limit(it) } ?: baseQuery

        return callbackFlow {
            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList()).isFailure
                    return@addSnapshotListener
                }
                val mapped = snapshot?.documents.orEmpty()
                    .mapNotNull { doc -> doc.toObject(ExpenseRecord::class.java) }
                    .map { ensureOwnership(ensureDateFormat(it), ownerUid) }
                trySend(mapped).isFailure
            }
            awaitClose { listener.remove() }
        }.map { list -> list.sortedByDescending { it.createdAt } }
    }

    suspend fun seedDefaultsIfEmpty() {
        // No-op; Firestore starts empty and is user-scoped
    }

    suspend fun addExpense(ownerUid: String, expense: ExpenseRecord) {
        val record = ensureOwnership(ensureMeta(expense), ownerUid)
        collection(ownerUid)
            .document(record.id.toString())
            .set(record)
            .await()
    }

    suspend fun updateExpense(ownerUid: String, expense: ExpenseRecord) {
        val record = ensureOwnership(ensureMeta(expense), ownerUid)
        collection(ownerUid)
            .document(record.id.toString())
            .set(record)
            .await()
    }

    suspend fun deleteExpense(ownerUid: String, id: Long) {
        collection(ownerUid)
            .document(id.toString())
            .delete()
            .await()
    }

    suspend fun replaceAll(ownerUid: String, expenses: List<ExpenseRecord>) {
        clearExpenses(ownerUid)
        if (expenses.isEmpty()) return
        val chunks = expenses.map { ensureOwnership(ensureMeta(it), ownerUid) }.chunked(400)
        for (chunk in chunks) {
            val batch = firestore.batch()
            val col = collection(ownerUid)
            chunk.forEach { exp ->
                batch.set(col.document(exp.id.toString()), exp)
            }
            batch.commit().await()
        }
    }

    private suspend fun clearExpenses(ownerUid: String) {
        val col = collection(ownerUid)
        while (true) {
            val snapshot = col.limit(400).get().await()
            if (snapshot.isEmpty) break
            val batch = firestore.batch()
            snapshot.documents.forEach { doc -> batch.delete(doc.reference) }
            batch.commit().await()
        }
    }

    private fun collection(ownerUid: String) =
        firestore.collection("users").document(ownerUid).collection("expenses")

    private fun ensureMeta(expense: ExpenseRecord): ExpenseRecord {
        val now = System.currentTimeMillis()
        val id = if (expense.id > 0) expense.id else now
        val createdAt = if (expense.createdAt > 0) expense.createdAt else now
        return expense.copy(id = id, createdAt = createdAt)
    }

    private fun ensureOwnership(expense: ExpenseRecord, ownerUid: String): ExpenseRecord {
        if (expense.ownerUid == ownerUid) return expense
        return expense.copy(ownerUid = ownerUid)
    }

    companion object {
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
