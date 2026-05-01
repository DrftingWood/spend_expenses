package com.spendexpenses.app.categorize

enum class Category(val display: String) {
    FOOD("Food & Dining"),
    GROCERIES("Groceries"),
    TRANSPORT("Transport"),
    SHOPPING("Shopping"),
    BILLS("Bills & Utilities"),
    ENTERTAINMENT("Entertainment"),
    HEALTH("Health"),
    TRAVEL("Travel"),
    OTHER("Other"),
    UNCATEGORIZED("Uncategorized");

    companion object {
        fun selectable(): List<Category> = values().filter { it != UNCATEGORIZED }
    }
}
