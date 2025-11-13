package com.cs407.savewise.model

import kotlinx.serialization.Serializable

@Serializable
data class ExpenseRecord(
    val id: Long,
    val title: String,
    val category: String,
    val amount: Double,
    val date: String // ISO-like yyyy-MM-dd for simple sorting
)

