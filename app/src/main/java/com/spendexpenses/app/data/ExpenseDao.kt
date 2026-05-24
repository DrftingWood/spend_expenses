package com.spendexpenses.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spendexpenses.app.categorize.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(expense: Expense): Long

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE timestamp >= :since ORDER BY timestamp DESC")
    fun observeSince(since: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE category = :uncat ORDER BY timestamp DESC")
    fun observeUncategorized(uncat: Category = Category.UNCATEGORIZED): Flow<List<Expense>>

    @Query("UPDATE expenses SET category = :category WHERE id = :id")
    suspend fun setCategory(id: Long, category: Category)

    @Query("UPDATE expenses SET category = :category WHERE LOWER(merchant) = LOWER(:merchant) AND category = :uncat")
    suspend fun applyCategoryToMerchant(
        merchant: String,
        category: Category,
        uncat: Category = Category.UNCATEGORIZED
    )

    @Query("SELECT EXISTS(SELECT 1 FROM expenses WHERE smsId = :smsId)")
    suspend fun existsBySmsId(smsId: Long): Boolean

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("""UPDATE expenses
              SET amount = :amount, direction = :direction, merchant = :merchant,
                  category = :category, timestamp = :timestamp
              WHERE id = :id""")
    suspend fun update(
        id: Long,
        amount: Double,
        direction: Direction,
        merchant: String,
        category: Category,
        timestamp: Long
    )

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    suspend fun snapshot(): List<Expense>
}
