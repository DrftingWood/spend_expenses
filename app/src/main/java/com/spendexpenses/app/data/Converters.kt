package com.spendexpenses.app.data

import androidx.room.TypeConverter
import com.spendexpenses.app.categorize.Category

class Converters {
    @TypeConverter fun fromCategory(c: Category): String = c.name
    @TypeConverter fun toCategory(s: String): Category = Category.valueOf(s)
    @TypeConverter fun fromDirection(d: Direction): String = d.name
    @TypeConverter fun toDirection(s: String): Direction = Direction.valueOf(s)
}
