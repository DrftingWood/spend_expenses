package com.spendexpenses.app.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.spendexpenses.app.MainActivity

object Notifications {
    private const val CHANNEL_ID = "uncategorized_spend"
    const val EXTRA_EXPENSE_ID = "expense_id"

    fun ensureChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = ContextCompat.getSystemService(ctx, NotificationManager::class.java) ?: return
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Needs categorization",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Posted when a transaction SMS can't be auto-categorized." }
        )
    }

    fun postUncategorized(ctx: Context, expenseId: Long, merchant: String, amount: Double) {
        ensureChannel(ctx)
        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_EXPENSE_ID, expenseId)
        }
        val pi = PendingIntent.getActivity(
            ctx,
            expenseId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Tag this spend")
            .setContentText("₹${"%,.2f".format(amount)} at $merchant — tap to categorize")
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        val nm = ContextCompat.getSystemService(ctx, NotificationManager::class.java) ?: return
        nm.notify(expenseId.toInt(), notif)
    }
}
