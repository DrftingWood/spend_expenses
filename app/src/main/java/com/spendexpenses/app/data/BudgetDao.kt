package com.spendexpenses.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spendexpenses.app.categorize.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: Budget)

    @Query("DELETE FROM budgets WHERE category = :category")
    suspend fun delete(category: Category)

    @Query("SELECT * FROM budgets")
    fun observeAll(): Flow<List<Budget>>
}
