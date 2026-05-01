package com.spendexpenses.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spendexpenses.app.categorize.Category

@Entity(tableName = "merchant_categories")
data class MerchantCategory(
    @PrimaryKey val merchant: String,   // normalized lowercase
    val category: Category
)
