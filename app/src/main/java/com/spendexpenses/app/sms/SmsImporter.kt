package com.spendexpenses.app.sms

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.spendexpenses.app.data.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsImporter(
    private val context: Context,
    private val repo: ExpenseRepository
) {
    suspend fun importSince(sinceMillis: Long): Int = withContext(Dispatchers.IO) {
        val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )
        val cursor = context.contentResolver.query(
            uri,
            projection,
            "${Telephony.Sms.DATE} >= ?",
            arrayOf(sinceMillis.toString()),
            "${Telephony.Sms.DATE} DESC"
        ) ?: return@withContext 0

        var count = 0
        cursor.use { c ->
            val idIdx = c.getColumnIndexOrThrow(Telephony.Sms._ID)
            val addrIdx = c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIdx = c.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIdx = c.getColumnIndexOrThrow(Telephony.Sms.DATE)
            while (c.moveToNext()) {
                val smsId = c.getLong(idIdx)
                val sender = c.getString(addrIdx) ?: continue
                val body = c.getString(bodyIdx) ?: continue
                val date = c.getLong(dateIdx)
                val parsed = SmsParser.parse(body, sender, date, smsId) ?: continue
                if (repo.ingest(parsed) != null) count++
            }
        }
        count
    }
}
