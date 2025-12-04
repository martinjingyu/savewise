package com.cs407.savewise.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cs407.savewise.model.ExpenseRecord

/**
 * Local Room entity used as the single source of truth for expenses.
 * Additional fields track sync status with Firestore.
 */
@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val category: String,
    val amount: Double,
    val date: String,
    val ownerUid: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val syncState: SyncState = SyncState.CLEAN
)

enum class SyncState {
    CLEAN,
    DIRTY_INSERT,
    DIRTY_UPDATE,
    DIRTY_DELETE,
    SYNC_ERROR
}

fun ExpenseEntity.toRecord(): ExpenseRecord = ExpenseRecord(
    id = id,
    title = title,
    category = category,
    amount = amount,
    date = date,
    ownerUid = ownerUid,
    createdAt = createdAt
)

fun ExpenseRecord.toEntity(
    ownerUid: String,
    syncState: SyncState,
    updatedAt: Long = System.currentTimeMillis(),
    isDeleted: Boolean = false
): ExpenseEntity {
    val now = System.currentTimeMillis()
    val resolvedId = if (id > 0) id else now
    val created = if (createdAt > 0) createdAt else now
    return ExpenseEntity(
        id = resolvedId,
        title = title,
        category = category,
        amount = amount,
        date = date,
        ownerUid = ownerUid,
        createdAt = created,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        syncState = syncState
    )
}

