package com.spendexpenses.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spendexpenses.app.categorize.Category

@Dao
interface MerchantCategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(mc: MerchantCategory)

    @Query("SELECT category FROM merchant_categories WHERE merchant = LOWER(:merchant) LIMIT 1")
    suspend fun find(merchant: String): Category?
}
