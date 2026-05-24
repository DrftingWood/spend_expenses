package com.spendexpenses.app.ui.budgets

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spendexpenses.app.SpendApp
import com.spendexpenses.app.categorize.Category
import com.spendexpenses.app.data.Direction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class BudgetRow(
    val category: Category,
    val limit: Double,
    val spent: Double
) {
    val progress: Float
        get() = if (limit <= 0.0) 0f else (spent / limit).toFloat().coerceIn(0f, 1f)
    val over: Boolean get() = limit > 0.0 && spent > limit
}

class BudgetsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as SpendApp).repository

    val rows: StateFlow<List<BudgetRow>> = combine(
        repo.observeBudgets(),
        repo.observeAll()
    ) { budgets, expenses ->
        val startOfMonth = startOfMonth()
        val spendByCat = expenses
            .filter { it.timestamp >= startOfMonth && it.direction == Direction.DEBIT }
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

        val cats = (budgets.map { it.category } + spendByCat.keys + Category.selectable()).toSet()
        cats.map { c ->
            BudgetRow(
                category = c,
                limit = budgets.firstOrNull { it.category == c }?.monthlyLimit ?: 0.0,
                spent = spendByCat[c] ?: 0.0
            )
        }.sortedWith(
            compareByDescending<BudgetRow> { it.limit > 0.0 }
                .thenByDescending { it.spent }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setBudget(category: Category, limit: Double) {
        viewModelScope.launch { repo.setBudget(category, limit) }
    }

    private fun startOfMonth(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
