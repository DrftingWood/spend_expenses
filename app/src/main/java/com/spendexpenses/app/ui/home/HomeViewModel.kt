package com.spendexpenses.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spendexpenses.app.SpendApp
import com.spendexpenses.app.categorize.Category
import com.spendexpenses.app.data.Expense
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as SpendApp).repository

    val expenses: StateFlow<List<Expense>> =
        repo.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uncategorized: StateFlow<List<Expense>> =
        repo.observeUncategorized().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun assign(expense: Expense, category: Category, remember: Boolean = true) {
        viewModelScope.launch { repo.assignCategory(expense, category, remember) }
    }
}
