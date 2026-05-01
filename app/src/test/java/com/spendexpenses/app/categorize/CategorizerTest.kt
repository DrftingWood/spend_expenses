package com.spendexpenses.app.categorize

import org.junit.Assert.assertEquals
import org.junit.Test

class CategorizerTest {

    private val c = Categorizer()

    @Test fun foodMerchantHits() {
        assertEquals(Category.FOOD, c.matchRules("Swiggy"))
        assertEquals(Category.FOOD, c.matchRules("ZOMATO Bangalore"))
    }

    @Test fun transportHits() {
        assertEquals(Category.TRANSPORT, c.matchRules("Uber India"))
        assertEquals(Category.TRANSPORT, c.matchRules("HPCL Petrol Pump"))
    }

    @Test fun shoppingHits() {
        assertEquals(Category.SHOPPING, c.matchRules("Amazon Pay"))
    }

    @Test fun unknownFallsThrough() {
        assertEquals(Category.UNCATEGORIZED, c.matchRules("RandomLocalShop"))
    }
}
