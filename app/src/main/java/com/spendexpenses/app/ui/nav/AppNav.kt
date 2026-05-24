package com.spendexpenses.app.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spendexpenses.app.ui.analytics.AnalyticsScreen
import com.spendexpenses.app.ui.budgets.BudgetsScreen
import com.spendexpenses.app.ui.home.HomeScreen

private object Routes {
    const val HOME = "home"
    const val BUDGETS = "budgets"
    const val ANALYTICS = "analytics"
}

@Composable
fun AppNav(
    hasSmsPermission: Boolean,
    onRequestPermission: () -> Unit,
    deepLinkExpenseId: Long? = null,
    onDeepLinkConsumed: () -> Unit = {}
) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = current?.hierarchy?.any { it.route == Routes.HOME } == true,
                    onClick = { nav.navigate(Routes.HOME) { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = current?.hierarchy?.any { it.route == Routes.BUDGETS } == true,
                    onClick = { nav.navigate(Routes.BUDGETS) { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                    label = { Text("Budgets") }
                )
                NavigationBarItem(
                    selected = current?.hierarchy?.any { it.route == Routes.ANALYTICS } == true,
                    onClick = { nav.navigate(Routes.ANALYTICS) { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    label = { Text("Analytics") }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    hasSmsPermission = hasSmsPermission,
                    onRequestPermission = onRequestPermission,
                    deepLinkExpenseId = deepLinkExpenseId,
                    onDeepLinkConsumed = onDeepLinkConsumed
                )
            }
            composable(Routes.BUDGETS) { BudgetsScreen() }
            composable(Routes.ANALYTICS) { AnalyticsScreen() }
        }
    }
}
