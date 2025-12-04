package com.cs407.savewise.data

import android.content.Context
import com.cs407.savewise.model.ExpenseRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Repository that keeps Room as the source of truth and syncs to Firestore.
 */
class ExpenseRepository(context: Context) {
    private val dao = ExpenseDatabase.getInstance(context).expenseDao()
    private val remote = ExpenseStorage(context)
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val remoteJobs = ConcurrentHashMap<String, Job>()
    companion object {
        @Volatile
        private var globalAutoSyncEnabled: Boolean = true
    }

    fun observeExpenses(ownerUid: String): Flow<List<ExpenseRecord>> =
        dao.observeByOwner(ownerUid).map { list ->
            list.filter { !it.isDeleted }.sortedByDescending { it.date }.map { it.toRecord() }
        }

    suspend fun addExpense(ownerUid: String, record: ExpenseRecord) {
        val entity = record.toEntity(
            ownerUid = ownerUid,
            syncState = SyncState.DIRTY_INSERT,
            updatedAt = System.currentTimeMillis(),
            isDeleted = false
        )
        dao.upsert(entity)
        pushDirty(ownerUid)
    }

    suspend fun updateExpense(ownerUid: String, record: ExpenseRecord) {
        val entity = record.toEntity(
            ownerUid = ownerUid,
            syncState = SyncState.DIRTY_UPDATE,
            updatedAt = System.currentTimeMillis(),
            isDeleted = false
        )
        dao.upsert(entity)
        pushDirty(ownerUid)
    }

    suspend fun deleteExpense(ownerUid: String, id: Long) {
        val now = System.currentTimeMillis()
        dao.markDeleted(id = id, updatedAt = now, state = SyncState.DIRTY_DELETE)
        pushDirty(ownerUid)
    }

    suspend fun syncNow(ownerUid: String, force: Boolean = false) {
        pushDirty(ownerUid, force = force)
    }

    /**
        * Starts listening to Firestore and writing remote changes into Room (clean state).
        * Safe to call multiple times; keeps one listener per user.
        */
    fun startRemoteSync(ownerUid: String) {
        if (remoteJobs.containsKey(ownerUid)) return
        val job = scope.launch {
            remote.expensesForUser(ownerUid).collect { remoteList ->
                val incoming = remoteList.map {
                    // Treat remote as source of truth; mark clean
                    it.toEntity(
                        ownerUid = ownerUid,
                        syncState = SyncState.CLEAN,
                        updatedAt = it.createdAt,
                        isDeleted = false
                    )
                }
                withContext(Dispatchers.IO) {
                    dao.replaceCleanWith(ownerUid, incoming)
                }
            }
        }
        remoteJobs[ownerUid] = job
    }

    fun stopRemoteSync(ownerUid: String) {
        remoteJobs.remove(ownerUid)?.cancel()
    }

    fun setAutoSyncEnabled(enabled: Boolean) {
        globalAutoSyncEnabled = enabled
    }

    private fun pushDirty(ownerUid: String, force: Boolean = false) {
        if (!force && !globalAutoSyncEnabled) return
        scope.launch {
            val dirty = dao.getDirty(ownerUid)
            if (dirty.isEmpty()) return@launch

            dirty.forEach { entity ->
                try {
                    when (entity.syncState) {
                        SyncState.DIRTY_INSERT -> {
                            remote.addExpense(ownerUid, entity.toRecord())
                            dao.upsert(entity.copy(syncState = SyncState.CLEAN))
                        }
                        SyncState.DIRTY_UPDATE -> {
                            remote.updateExpense(ownerUid, entity.toRecord())
                            dao.upsert(entity.copy(syncState = SyncState.CLEAN))
                        }
                        SyncState.DIRTY_DELETE -> {
                            remote.deleteExpense(ownerUid, entity.id)
                            dao.hardDelete(entity.id)
                        }
                        SyncState.SYNC_ERROR -> {
                            // Retry based on deleted flag
                            if (entity.isDeleted) {
                                remote.deleteExpense(ownerUid, entity.id)
                                dao.hardDelete(entity.id)
                            } else {
                                remote.updateExpense(ownerUid, entity.toRecord())
                                dao.upsert(entity.copy(syncState = SyncState.CLEAN))
                            }
                        }
                        SyncState.CLEAN -> Unit
                    }
                } catch (e: Exception) {
                    dao.updateSyncState(entity.id, SyncState.SYNC_ERROR)
                }
            }
        }
    }
}
