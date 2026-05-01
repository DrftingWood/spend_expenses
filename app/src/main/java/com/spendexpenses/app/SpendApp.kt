package com.spendexpenses.app

import android.app.Application
import com.spendexpenses.app.categorize.Categorizer
import com.spendexpenses.app.data.AppDatabase
import com.spendexpenses.app.data.ExpenseRepository
import com.spendexpenses.app.sms.SmsImporter

class SpendApp : Application() {

    lateinit var repository: ExpenseRepository
        private set

    lateinit var importer: SmsImporter
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.get(this)
        val categorizer = Categorizer()
        repository = ExpenseRepository(db.expenseDao(), db.merchantCategoryDao(), categorizer)
        importer = SmsImporter(this, repository)
    }
}
