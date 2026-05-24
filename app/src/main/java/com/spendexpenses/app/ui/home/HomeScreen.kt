package com.spendexpenses.app.ui.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spendexpenses.app.categorize.Category
import com.spendexpenses.app.data.Direction
import com.spendexpenses.app.data.Expense
import com.spendexpenses.app.ui.categorize.AssignCategorySheet
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun HomeScreen(
    hasSmsPermission: Boolean,
    onRequestPermission: () -> Unit,
    deepLinkExpenseId: Long? = null,
    onDeepLinkConsumed: () -> Unit = {},
    vm: HomeViewModel = viewModel()
) {
    val expenses by vm.expenses.collectAsStateWithLifecycle()
    val uncategorized by vm.uncategorized.collectAsStateWithLifecycle()
    var assigning by remember { mutableStateOf<Expense?>(null) }
    var adding by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Expense?>(null) }
    var editing by remember { mutableStateOf<Expense?>(null) }
    var rowMenuFor by remember { mutableStateOf<Expense?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            vm.exportCsv(uri) { count ->
                scope.launch { snackbar.showSnackbar("Exported $count rows") }
            }
        }
    }

    LaunchedEffect(deepLinkExpenseId, expenses) {
        val id = deepLinkExpenseId ?: return@LaunchedEffect
        val match = expenses.firstOrNull { it.id == id } ?: return@LaunchedEffect
        assigning = match
        onDeepLinkConsumed()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spend Tracker") },
                actions = {
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("Export CSV") },
                                onClick = {
                                    menuOpen = false
                                    exportLauncher.launch("spend-export.csv")
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { adding = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add expense")
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
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
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${uncategorized.size} need a category")
                        Button(onClick = { assigning = uncategorized.first() }) {
                            Text("Categorize")
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(expenses, key = { it.id }) { e ->
                    ExpenseRow(
                        e,
                        onClick = { assigning = e },
                        onLongPress = { rowMenuFor = e }
                    )
                }
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

    if (adding) {
        AddExpenseDialog(
            onDismiss = { adding = false },
            onSave = { amount, dir, merchant, cat, ts, note ->
                vm.addManual(amount, dir, merchant, cat, ts, note)
                adding = false
            }
        )
    }

    editing?.let { exp ->
        AddExpenseDialog(
            initial = exp,
            onDismiss = { editing = null },
            onSave = { amount, dir, merchant, cat, ts, _ ->
                vm.update(exp.id, amount, dir, merchant, cat, ts)
                editing = null
            }
        )
    }

    rowMenuFor?.let { exp ->
        AlertDialog(
            onDismissRequest = { rowMenuFor = null },
            title = { Text(exp.merchant) },
            text = { Text("₹${"%,.2f".format(abs(exp.amount))} • ${exp.category.display}") },
            confirmButton = {
                Button(onClick = { editing = exp; rowMenuFor = null }) { Text("Edit") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = exp; rowMenuFor = null }) {
                    Text("Delete")
                }
            }
        )
    }

    pendingDelete?.let { exp ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete this entry?") },
            text = { Text("${exp.merchant} • ₹${"%,.2f".format(abs(exp.amount))}") },
            confirmButton = {
                Button(onClick = { vm.delete(exp); pendingDelete = null }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ExpenseRow(e: Expense, onClick: () -> Unit, onLongPress: () -> Unit) {
    Card(
        Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
    ) {
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
            Column(horizontalAlignment = Alignment.End) {
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
