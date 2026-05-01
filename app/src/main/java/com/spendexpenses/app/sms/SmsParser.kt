package com.spendexpenses.app.sms

import com.spendexpenses.app.data.Direction

data class ParsedSms(
    val amount: Double,
    val direction: Direction,
    val merchant: String,
    val timestamp: Long,
    val rawBody: String,
    val sender: String,
    val smsId: Long?
)

object SmsParser {

    private val AMOUNT = Regex(
        "(?:INR|Rs\\.?|₹)\\s?([0-9][0-9,]*(?:\\.[0-9]{1,2})?)",
        RegexOption.IGNORE_CASE
    )

    private val DEBIT_CUES = Regex(
        "\\b(debited|spent|paid|purchase|withdrawn|sent|charged|deducted)\\b",
        RegexOption.IGNORE_CASE
    )

    private val CREDIT_CUES = Regex(
        "\\b(credited|received|refund(?:ed)?|reversal|reversed|cashback)\\b",
        RegexOption.IGNORE_CASE
    )

    private val PROMO_CUES = Regex(
        "\\b(offer|discount|sale|deal|win|cashback offer|congratulations|click|http|otp|one[- ]time password)\\b",
        RegexOption.IGNORE_CASE
    )

    // Capture merchant after at|to|@|towards, until the next clause/punctuation.
    private val MERCHANT_AT = Regex(
        "(?:at|to|@|towards|on)\\s+([A-Za-z0-9][A-Za-z0-9 .&'_-]{1,40}?)\\s*(?:\\.|,|;|on|for|ref|upi|txn|info|bal|avl|a/c|account|$)",
        RegexOption.IGNORE_CASE
    )

    fun parse(
        body: String,
        sender: String,
        timestamp: Long,
        smsId: Long? = null
    ): ParsedSms? {
        if (body.isBlank()) return null
        // OTP and promotional messages: skip.
        if (PROMO_CUES.containsMatchIn(body) && !DEBIT_CUES.containsMatchIn(body) && !CREDIT_CUES.containsMatchIn(body)) {
            return null
        }
        if (Regex("\\botp\\b", RegexOption.IGNORE_CASE).containsMatchIn(body)) return null

        val direction = when {
            DEBIT_CUES.containsMatchIn(body) -> Direction.DEBIT
            CREDIT_CUES.containsMatchIn(body) -> Direction.CREDIT
            else -> return null
        }

        val amountStr = AMOUNT.find(body)?.groupValues?.getOrNull(1) ?: return null
        val amount = amountStr.replace(",", "").toDoubleOrNull() ?: return null
        if (amount <= 0.0) return null

        val merchant = extractMerchant(body) ?: deriveFromSender(sender)
        return ParsedSms(
            amount = amount,
            direction = direction,
            merchant = merchant,
            timestamp = timestamp,
            rawBody = body,
            sender = sender,
            smsId = smsId
        )
    }

    private fun extractMerchant(body: String): String? {
        val m = MERCHANT_AT.find(body)?.groupValues?.getOrNull(1) ?: return null
        val cleaned = m.trim().trimEnd('.', ',', ';').replace(Regex("\\s+"), " ")
        // Filter out generic phrases.
        if (cleaned.length < 2) return null
        if (cleaned.matches(Regex("\\d+"))) return null
        return cleaned
    }

    private fun deriveFromSender(sender: String): String {
        // e.g. "VK-HDFCBK" -> "HDFCBK"
        val parts = sender.split('-', '.', ' ')
        return parts.lastOrNull { it.isNotBlank() }?.trim() ?: sender
    }
}
