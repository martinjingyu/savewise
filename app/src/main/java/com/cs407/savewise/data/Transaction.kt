package com.cs407.savewise.data

data class Transaction(
    val subject: String,
    val detail: String,
    val spend: Double,
    val date: String,
    val time: String
)