package com.example.mcptestapp.data
import androidx.room.TypeConverter
import java.time.YearMonth

class YearMonthConverter {
    @TypeConverter
    fun fromYearMonth(value: YearMonth?): String? {
        return value?.toString() // e.g., "2026-09"
    }

    @TypeConverter
    fun toYearMonth(value: String?): YearMonth? {
        return value?.let { YearMonth.parse(it) }
    }
}