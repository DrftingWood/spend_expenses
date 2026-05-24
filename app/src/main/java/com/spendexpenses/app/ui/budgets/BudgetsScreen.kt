package com.spendexpenses.app.ui.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spendexpenses.app.categorize.Category

@Composable
fun BudgetsScreen(vm: BudgetsViewModel = viewModel()) {
    val rows by vm.rows.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<BudgetRow?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Monthly budgets", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text(
            "Tap a category to set or change its monthly limit.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(rows, key = { it.category.name }) { row ->
                BudgetCard(row, onClick = { editing = row })
            }
        }
    }

    editing?.let { row ->
        BudgetEditor(
            row = row,
            onDismiss = { editing = null },
            onSave = { newLimit ->
                vm.setBudget(row.category, newLimit)
                editing = null
            }
        )
    }
}

@Composable
private fun BudgetCard(row: BudgetRow, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(row.category.display, style = MaterialTheme.typography.titleMedium)
                if (row.limit > 0.0) {
                    Text(
                        "₹${"%,.0f".format(row.spent)} / ₹${"%,.0f".format(row.limit)}",
                        color = if (row.over) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Text("Spent ₹${"%,.0f".format(row.spent)} · no budget")
                }
            }
            if (row.limit > 0.0) {
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE0E0E0))
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(row.progress)
                            .height(8.dp)
                            .background(
                                if (row.over) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetEditor(
    row: BudgetRow,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var text by remember {
        mutableStateOf(if (row.limit > 0.0) "%.0f".format(row.limit) else "")
    }
    val parsed = text.toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Budget · ${row.category.display}") },
        text = {
            OutlinedTextField(
                value = text, onValueChange = { text = it },
                label = { Text("Monthly limit (₹)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                enabled = parsed != null && parsed >= 0.0,
                onClick = { onSave(parsed ?: 0.0) }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Suppress("unused")
private fun dot(c: Category) = c // keep import lint happy
