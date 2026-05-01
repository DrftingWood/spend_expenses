package com.spendexpenses.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.spendexpenses.app.ui.nav.AppNav
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface { AppRoot() }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun AppRoot() {
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
            if (hasSms) {
                val app = application as SpendApp
                lifecycleScope.launch {
                    val ninetyDaysAgo = System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000
                    app.importer.importSince(ninetyDaysAgo)
                }
            }
        }

        LaunchedEffect(Unit) {
            if (!hasSms) launcher.launch(perms)
            else {
                val app = application as SpendApp
                val ninetyDaysAgo = System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000
                app.importer.importSince(ninetyDaysAgo)
            }
        }

        AppNav(hasSmsPermission = hasSms, onRequestPermission = { launcher.launch(perms) })
    }
}
