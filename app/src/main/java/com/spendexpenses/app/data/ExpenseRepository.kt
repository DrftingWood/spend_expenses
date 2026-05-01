package com.spendexpenses.app.data

import com.spendexpenses.app.categorize.Categorizer
import com.spendexpenses.app.categorize.Category
import com.spendexpenses.app.sms.ParsedSms
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val merchantDao: MerchantCategoryDao,
    private val categorizer: Categorizer
) {
    fun observeAll(): Flow<List<Expense>> = expenseDao.observeAll()
    fun observeSince(since: Long): Flow<List<Expense>> = expenseDao.observeSince(since)
    fun observeUncategorized(): Flow<List<Expense>> = expenseDao.observeUncategorized()

    data class IngestResult(val id: Long, val category: Category, val merchant: String, val amount: Double)

    suspend fun ingest(parsed: ParsedSms): IngestResult? {
        if (parsed.smsId != null && expenseDao.existsBySmsId(parsed.smsId)) return null
        val cat = categorizer.categorize(parsed.merchant, merchantDao)
        val signed = if (parsed.direction == Direction.DEBIT) parsed.amount else -parsed.amount
        val id = expenseDao.insert(
            Expense(
                amount = signed,
                direction = parsed.direction,
                merchant = parsed.merchant,
                category = cat,
                timestamp = parsed.timestamp,
                rawSms = parsed.rawBody,
                sender = parsed.sender,
                smsId = parsed.smsId
            )
        )
        return if (id > 0) IngestResult(id, cat, parsed.merchant, parsed.amount) else null
    }

    suspend fun assignCategory(expense: Expense, category: Category, remember: Boolean) {
        expenseDao.setCategory(expense.id, category)
        if (remember) {
            merchantDao.upsert(MerchantCategory(expense.merchant.lowercase(), category))
            expenseDao.applyCategoryToMerchant(expense.merchant, category)
        }
    }
}
