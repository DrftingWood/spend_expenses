package com.spendexpenses.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spendexpenses.app.categorize.Category

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey val category: Category,
    val monthlyLimit: Double
)
