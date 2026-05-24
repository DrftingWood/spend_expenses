package com.spendexpenses.app.ui.home

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spendexpenses.app.SpendApp
import com.spendexpenses.app.categorize.Category
import com.spendexpenses.app.data.Direction
import com.spendexpenses.app.data.Expense
import com.spendexpenses.app.export.CsvExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(private val app: Application) : AndroidViewModel(app) {

    private val repo = (app as SpendApp).repository

    val expenses: StateFlow<List<Expense>> =
        repo.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uncategorized: StateFlow<List<Expense>> =
        repo.observeUncategorized().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun assign(expense: Expense, category: Category, remember: Boolean = true) {
        viewModelScope.launch { repo.assignCategory(expense, category, remember) }
    }

    fun delete(expense: Expense) {
        viewModelScope.launch { repo.delete(expense) }
    }

    fun update(
        id: Long,
        amount: Double,
        direction: Direction,
        merchant: String,
        category: Category,
        timestamp: Long
    ) {
        viewModelScope.launch {
            repo.updateExpense(id, amount, direction, merchant, category, timestamp)
        }
    }

    fun addManual(
        amount: Double,
        direction: Direction,
        merchant: String,
        category: Category,
        timestamp: Long,
        note: String
    ) {
        viewModelScope.launch {
            repo.addManual(amount, direction, merchant, category, timestamp, note)
        }
    }

    fun exportCsv(target: Uri, onDone: (Int) -> Unit) {
        viewModelScope.launch {
            val rows = repo.snapshot()
            withContext(Dispatchers.IO) {
                app.contentResolver.openOutputStream(target)?.use { out ->
                    CsvExporter.write(out, rows)
                }
            }
            onDone(rows.size)
        }
    }
}
