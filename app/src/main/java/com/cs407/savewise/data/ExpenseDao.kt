package com.cs407.savewise.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query(
        """
        SELECT * FROM expenses
        WHERE ownerUid = :ownerUid AND isDeleted = 0
        ORDER BY date DESC, createdAt DESC
        """
    )
    fun observeByOwner(ownerUid: String): Flow<List<ExpenseEntity>>

    @Query(
        """
        SELECT * FROM expenses
        WHERE ownerUid = :ownerUid AND syncState != :cleanState
        """
    )
    suspend fun getDirty(ownerUid: String, cleanState: SyncState = SyncState.CLEAN): List<ExpenseEntity>

    @Upsert
    suspend fun upsert(entity: ExpenseEntity)

    @Upsert
    suspend fun upsertAll(entities: List<ExpenseEntity>)

    @Query(
        """
        UPDATE expenses
        SET syncState = :state
        WHERE id = :id
        """
    )
    suspend fun updateSyncState(id: Long, state: SyncState)

    @Query(
        """
        UPDATE expenses
        SET isDeleted = 1, syncState = :state, updatedAt = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun markDeleted(id: Long, updatedAt: Long, state: SyncState)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun hardDelete(id: Long)

    @Transaction
    suspend fun replaceCleanWith(ownerUid: String, incoming: List<ExpenseEntity>) {
        upsertAll(incoming)
        val ids = incoming.map { it.id }
        deleteCleanNotIn(ownerUid, ids)
    }

    @Query(
        """
        DELETE FROM expenses
        WHERE ownerUid = :ownerUid
          AND syncState = :cleanState
          AND (:idsSize = 0 OR id NOT IN (:ids))
        """
    )
    suspend fun deleteCleanNotIn(
        ownerUid: String,
        ids: List<Long>,
        cleanState: SyncState = SyncState.CLEAN,
        idsSize: Int = ids.size
    )
}

