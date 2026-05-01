package com.spendexpenses.app.export

import com.spendexpenses.app.data.Expense
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    private val ISO = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun write(out: OutputStream, expenses: List<Expense>) {
        out.bufferedWriter().use { w ->
            w.appendLine("timestamp,direction,amount,merchant,category,sender,raw")
            for (e in expenses) {
                w.append(ISO.format(Date(e.timestamp))).append(',')
                w.append(e.direction.name).append(',')
                w.append(e.amount.toString()).append(',')
                w.append(escape(e.merchant)).append(',')
                w.append(e.category.name).append(',')
                w.append(escape(e.sender)).append(',')
                w.appendLine(escape(e.rawSms))
            }
        }
    }

    private fun escape(s: String): String {
        val needsQuotes = s.contains(',') || s.contains('"') || s.contains('\n')
        val replaced = s.replace("\"", "\"\"")
        return if (needsQuotes) "\"$replaced\"" else replaced
    }
}
