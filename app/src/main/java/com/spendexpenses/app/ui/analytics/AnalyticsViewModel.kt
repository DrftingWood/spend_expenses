package com.spendexpenses.app.ui.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spendexpenses.app.SpendApp
import com.spendexpenses.app.categorize.Category
import com.spendexpenses.app.data.Direction
import com.spendexpenses.app.data.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

enum class Range(val days: Int?, val label: String) {
    THIS_MONTH(null, "This month"),
    LAST_3_MONTHS(90, "Last 3 months"),
    ALL(null, "All time")
}

data class AnalyticsState(
    val range: Range = Range.THIS_MONTH,
    val includeRefunds: Boolean = true,
    val total: Double = 0.0,
    val byCategory: List<Pair<Category, Double>> = emptyList(),
    val byMonth: List<Pair<String, Double>> = emptyList(),
    val topMerchants: List<Pair<String, Double>> = emptyList()
)

class AnalyticsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as SpendApp).repository
    private val rangeFlow = MutableStateFlow(Range.THIS_MONTH)
    private val refundsFlow = MutableStateFlow(true)

    val state: StateFlow<AnalyticsState> = combine(
        repo.observeAll(),
        rangeFlow,
        refundsFlow
    ) { all, range, includeRefunds ->
        val filtered = filterByRange(all, range)
            .let { if (includeRefunds) it else it.filter { e -> e.direction == Direction.DEBIT } }

        val total = filtered.sumOf { it.amount }
        val byCat = filtered.groupBy { it.category }
            .map { (c, list) -> c to list.sumOf { it.amount } }
            .sortedByDescending { it.second }
        val byMonth = filtered.groupBy { monthKey(it.timestamp) }
            .toSortedMap()
            .map { (k, list) -> k to list.sumOf { it.amount } }
        val topMerchants = filtered
            .filter { it.direction == Direction.DEBIT }
            .groupBy { it.merchant }
            .map { (m, list) -> m to list.sumOf { it.amount } }
            .sortedByDescending { it.second }
            .take(5)

        AnalyticsState(range, includeRefunds, total, byCat, byMonth, topMerchants)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnalyticsState())

    fun setRange(r: Range) { rangeFlow.value = r }
    fun setIncludeRefunds(v: Boolean) { refundsFlow.value = v }

    private fun filterByRange(list: List<Expense>, range: Range): List<Expense> {
        val cal = Calendar.getInstance()
        return when (range) {
            Range.ALL -> list
            Range.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                list.filter { it.timestamp >= start }
            }
            Range.LAST_3_MONTHS -> {
                val start = System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000
                list.filter { it.timestamp >= start }
            }
        }
    }

    private fun monthKey(ts: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = ts }
        return "%04d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }
}
