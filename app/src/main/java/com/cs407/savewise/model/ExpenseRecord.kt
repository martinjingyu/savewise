package com.cs407.savewise.model

//import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.serialization.Serializable

//@IgnoreExtraProperties
@Serializable
data class ExpenseRecord(
    val id: Long = 0L,
    val title: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val date: String = "", // ISO-like yyyy-MM-dd for simple sorting
    val ownerUid: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
