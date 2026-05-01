package com.spendexpenses.app.sms

import com.spendexpenses.app.data.Direction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsParserTest {

    @Test fun parsesHdfcDebit() {
        val body = "Rs.450.00 debited from a/c XXXX1234 on 02-05-25 at SWIGGY. Avl bal Rs.20,000."
        val p = SmsParser.parse(body, "VK-HDFCBK", 1_700_000_000_000L, 1L)
        assertNotNull(p)
        assertEquals(450.0, p!!.amount, 0.001)
        assertEquals(Direction.DEBIT, p.direction)
        assertTrue(p.merchant.lowercase().contains("swiggy"))
    }

    @Test fun parsesIciciUpi() {
        val body = "INR 199 paid to ZOMATO via UPI. Ref 1234567890. -ICICI"
        val p = SmsParser.parse(body, "AX-ICICIB", 1L, 2L)
        assertNotNull(p)
        assertEquals(199.0, p!!.amount, 0.001)
        assertEquals(Direction.DEBIT, p.direction)
        assertTrue(p.merchant.lowercase().contains("zomato"))
    }

    @Test fun parsesCreditAsCredit() {
        val body = "₹500.00 credited to a/c XXXX1234 from AMAZON refund on 02-05-25."
        val p = SmsParser.parse(body, "VK-AXISBK", 1L, 3L)
        assertNotNull(p)
        assertEquals(Direction.CREDIT, p!!.direction)
        assertEquals(500.0, p.amount, 0.001)
    }

    @Test fun ignoresOtp() {
        val body = "123456 is your OTP for txn of Rs 999. Do not share. -SBI"
        assertNull(SmsParser.parse(body, "VM-SBIINB", 1L, 4L))
    }

    @Test fun ignoresPromo() {
        val body = "Sale! Get 50% discount on your next order. Shop now at amazon.in"
        assertNull(SmsParser.parse(body, "AD-AMAZON", 1L, 5L))
    }

    @Test fun parsesAmountWithCommas() {
        val body = "Rs 1,250.50 debited at FLIPKART"
        val p = SmsParser.parse(body, "VK-HDFCBK", 1L, 6L)
        assertNotNull(p)
        assertEquals(1250.50, p!!.amount, 0.001)
    }
}
