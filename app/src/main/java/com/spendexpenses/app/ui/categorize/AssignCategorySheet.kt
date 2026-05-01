package com.spendexpenses.app.ui.categorize

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spendexpenses.app.categorize.Category
import com.spendexpenses.app.data.Expense
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignCategorySheet(
    expense: Expense,
    onDismiss: () -> Unit,
    onPick: (Category) -> Unit
) {
    rememberCoroutineScope()
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(expense.merchant, style = MaterialTheme.typography.titleLarge)
            Text("₹${"%,.2f".format(abs(expense.amount))}", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(expense.rawSms, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
            Text("Pick a category", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Category.selectable()) { cat ->
                    AssistChip(
                        onClick = { onPick(cat) },
                        label = { Text(cat.display) }
                    )
                }
            }
        }
    }
}
