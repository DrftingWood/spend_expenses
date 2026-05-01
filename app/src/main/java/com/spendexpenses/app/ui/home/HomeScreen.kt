package com.spendexpenses.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spendexpenses.app.categorize.Category
import com.spendexpenses.app.data.Direction
import com.spendexpenses.app.data.Expense
import com.spendexpenses.app.ui.categorize.AssignCategorySheet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun HomeScreen(
    hasSmsPermission: Boolean,
    onRequestPermission: () -> Unit,
    vm: HomeViewModel = viewModel()
) {
    val expenses by vm.expenses.collectAsStateWithLifecycle()
    val uncategorized by vm.uncategorized.collectAsStateWithLifecycle()
    var assigning by remember { mutableStateOf<Expense?>(null) }

    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Recent transactions", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        if (!hasSmsPermission) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Grant SMS permission to import your transactions.")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onRequestPermission) { Text("Grant access") }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        if (uncategorized.isNotEmpty()) {
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Row(
                    Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${uncategorized.size} need a category")
                    Button(onClick = { assigning = uncategorized.first() }) { Text("Categorize") }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(expenses, key = { it.id }) { e ->
                ExpenseRow(e, onClick = { assigning = e })
            }
        }
    }

    assigning?.let { exp ->
        AssignCategorySheet(
            expense = exp,
            onDismiss = { assigning = null },
            onPick = { cat ->
                vm.assign(exp, cat)
                val nextUncat = uncategorized.firstOrNull { it.id != exp.id }
                assigning = nextUncat
            }
        )
    }
}

@Composable
private fun ExpenseRow(e: Expense, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.padding(end = 8.dp)) {
                Text(e.merchant, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${e.category.display} • ${formatDate(e.timestamp)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Column {
                val sign = if (e.direction == Direction.CREDIT) "+ " else "- "
                Text(
                    "$sign₹${"%,.2f".format(abs(e.amount))}",
                    style = MaterialTheme.typography.titleMedium
                )
                if (e.category == Category.UNCATEGORIZED) {
                    Button(onClick = onClick) { Text("Tag") }
                }
            }
        }
    }
}

private val DATE_FMT = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
private fun formatDate(ts: Long): String = DATE_FMT.format(Date(ts))
