package com.spendexpenses.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.spendexpenses.app.notify.Notifications
import com.spendexpenses.app.ui.nav.AppNav
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val pendingDeepLinkExpenseId = mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        consumeDeepLink(intent)
        setContent {
            MaterialTheme {
                Surface { AppRoot(pendingDeepLinkExpenseId) }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        consumeDeepLink(intent)
    }

    private fun consumeDeepLink(intent: Intent?) {
        val id = intent?.getLongExtra(Notifications.EXTRA_EXPENSE_ID, -1L) ?: -1L
        if (id > 0) pendingDeepLinkExpenseId.value = id
    }

    @Composable
    private fun AppRoot(deepLinkExpenseId: MutableState<Long?>) {
        var hasSms by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) ==
                    PackageManager.PERMISSION_GRANTED
            )
        }

        val perms = buildList {
            add(Manifest.permission.READ_SMS)
            add(Manifest.permission.RECEIVE_SMS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()

        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            hasSms = result[Manifest.permission.READ_SMS] == true
            if (hasSms) runImport()
        }

        LaunchedEffect(Unit) {
            if (!hasSms) launcher.launch(perms) else runImport()
        }

        AppNav(
            hasSmsPermission = hasSms,
            onRequestPermission = { launcher.launch(perms) },
            deepLinkExpenseId = deepLinkExpenseId.value,
            onDeepLinkConsumed = { deepLinkExpenseId.value = null }
        )
    }

    private fun runImport() {
        val app = application as SpendApp
        lifecycleScope.launch {
            val ninetyDaysAgo = System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000
            app.importer.importSince(ninetyDaysAgo)
        }
    }
}
