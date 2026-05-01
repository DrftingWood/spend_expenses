package com.spendexpenses.app.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.spendexpenses.app.SpendApp
import com.spendexpenses.app.categorize.Category
import com.spendexpenses.app.notify.Notifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (messages.isEmpty()) return

        // Concatenate multi-part SMS by sender.
        val sender = messages.first().originatingAddress ?: return
        val body = messages.joinToString(separator = "") { it.messageBody.orEmpty() }
        val ts = messages.first().timestampMillis

        val parsed = SmsParser.parse(body, sender, ts, smsId = null) ?: return
        val app = context.applicationContext as SpendApp
        val repo = app.repository
        scope.launch {
            val result = repo.ingest(parsed) ?: return@launch
            if (result.category == Category.UNCATEGORIZED) {
                Notifications.postUncategorized(app, result.id, result.merchant, result.amount)
            }
        }
    }
}
