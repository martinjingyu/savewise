package com.cs407.savewise.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction


@Dao
interface DeleteDao {
    @Query("DELETE FROM user WHERE userId = :userId")
    suspend fun deleteUser(userId: Int)
    @Transaction
    suspend fun delete(userId: Int) {
        deleteUser(userId)
    }
}