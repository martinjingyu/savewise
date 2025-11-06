package com.cs407.savewise.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    @Query("SELECT * FROM user WHERE userUID = :uid")
    suspend fun getByUID(uid: String): User?
    @Query("SELECT * FROM user WHERE userId = :id")
    suspend fun getById(id: Int): User
    @Insert(entity = User::class)
    suspend fun insert(user: User)
}