package com.spendexpenses.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.spendexpenses.app.categorize.Category

enum class Direction { DEBIT, CREDIT }

@Entity(
    tableName = "expenses",
    indices = [Index(value = ["smsId"], unique = true)]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,                 // signed: debit positive, credit negative
    val direction: Direction,
    val merchant: String,
    val category: Category,
    val timestamp: Long,                // epoch millis
    val rawSms: String,
    val sender: String,
    val smsId: Long?                    // SMS provider _id when imported; null for manual
)
