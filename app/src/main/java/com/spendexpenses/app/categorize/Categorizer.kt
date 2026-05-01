package com.spendexpenses.app.categorize

import com.spendexpenses.app.data.MerchantCategoryDao

class Categorizer(private val rules: Map<Category, List<Regex>> = DEFAULT_RULES) {

    suspend fun categorize(merchant: String, dao: MerchantCategoryDao): Category {
        if (merchant.isBlank()) return Category.UNCATEGORIZED
        dao.find(merchant)?.let { return it }
        return matchRules(merchant)
    }

    fun matchRules(merchant: String): Category {
        val m = merchant.lowercase()
        for ((category, regs) in rules) {
            if (regs.any { it.containsMatchIn(m) }) return category
        }
        return Category.UNCATEGORIZED
    }

    companion object {
        private fun kw(vararg words: String): List<Regex> =
            words.map { Regex("\\b${Regex.escape(it)}\\b", RegexOption.IGNORE_CASE) }

        val DEFAULT_RULES: Map<Category, List<Regex>> = linkedMapOf(
            Category.FOOD to kw(
                "swiggy", "zomato", "ubereats", "dominos", "pizzahut", "kfc",
                "mcdonald", "starbucks", "cafe", "restaurant", "barbeque", "biryani"
            ),
            Category.GROCERIES to kw(
                "bigbasket", "blinkit", "zepto", "instamart", "dmart", "grofers",
                "reliancefresh", "more", "spencer"
            ),
            Category.TRANSPORT to kw(
                "uber", "ola", "rapido", "metro", "irctc", "redbus", "petrol",
                "hpcl", "iocl", "bpcl", "fuel"
            ),
            Category.TRAVEL to kw(
                "makemytrip", "goibibo", "yatra", "easemytrip", "airbnb",
                "booking.com", "indigo", "vistara", "airindia", "spicejet"
            ),
            Category.SHOPPING to kw(
                "amazon", "flipkart", "myntra", "ajio", "meesho", "nykaa",
                "tatacliq", "shoppersstop", "lifestyle", "ikea"
            ),
            Category.BILLS to kw(
                "electricity", "bescom", "msedcl", "tneb", "airtel", "jio",
                "vodafone", "vi", "tatapower", "gas", "broadband", "act",
                "recharge", "postpaid", "rent"
            ),
            Category.ENTERTAINMENT to kw(
                "netflix", "primevideo", "hotstar", "spotify", "youtube",
                "bookmyshow", "pvr", "inox", "sonyliv", "zee5"
            ),
            Category.HEALTH to kw(
                "apollo", "pharmeasy", "1mg", "netmeds", "medplus",
                "practo", "hospital", "clinic", "diagnostic"
            )
        )
    }
}
