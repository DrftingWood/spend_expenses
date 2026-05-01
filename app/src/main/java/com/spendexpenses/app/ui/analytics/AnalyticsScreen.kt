package com.spendexpenses.app.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spendexpenses.app.categorize.Category
import kotlin.math.abs
import kotlin.math.max

@Composable
fun AnalyticsScreen(vm: AnalyticsViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Analytics", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Range.values().forEach { r ->
                FilterChip(
                    selected = state.range == r,
                    onClick = { vm.setRange(r) },
                    label = { Text(r.label) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = state.includeRefunds, onCheckedChange = { vm.setIncludeRefunds(it) })
            Spacer(Modifier.width(8.dp))
            Text("Include refunds (net)")
        }

        Spacer(Modifier.height(16.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Total ${if (state.includeRefunds) "(net)" else "(debits)"}")
                Text(
                    "₹${"%,.2f".format(state.total)}",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("By category")
        BarList(state.byCategory.map { it.first.display to it.second })

        Spacer(Modifier.height(16.dp))
        SectionTitle("By month")
        BarList(state.byMonth.map { it.first to it.second })

        Spacer(Modifier.height(16.dp))
        SectionTitle("Top merchants")
        BarList(state.topMerchants)
    }
}

@Composable
private fun SectionTitle(t: String) {
    Text(t, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun BarList(items: List<Pair<String, Double>>) {
    if (items.isEmpty()) {
        Text("No data yet", style = MaterialTheme.typography.bodySmall)
        return
    }
    val maxAbs = max(items.maxOf { abs(it.second) }, 1.0)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { (label, value) ->
            Column(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                    Text("₹${"%,.2f".format(value)}", style = MaterialTheme.typography.bodyMedium)
                }
                val frac = (abs(value) / maxAbs).toFloat().coerceIn(0f, 1f)
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE0E0E0))
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(frac)
                            .height(8.dp)
                            .background(
                                if (value < 0) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.primary
                            )
                    )
                }
            }
        }
    }
}

@Suppress("unused")
private fun categoryColor(c: Category): Color = when (c) {
    Category.FOOD -> Color(0xFFEF5350)
    Category.GROCERIES -> Color(0xFF66BB6A)
    Category.TRANSPORT -> Color(0xFF42A5F5)
    Category.SHOPPING -> Color(0xFFAB47BC)
    Category.BILLS -> Color(0xFFFFA726)
    Category.ENTERTAINMENT -> Color(0xFFEC407A)
    Category.HEALTH -> Color(0xFF26A69A)
    Category.TRAVEL -> Color(0xFF7E57C2)
    Category.OTHER -> Color(0xFF8D8D8D)
    Category.UNCATEGORIZED -> Color(0xFFB0BEC5)
}
