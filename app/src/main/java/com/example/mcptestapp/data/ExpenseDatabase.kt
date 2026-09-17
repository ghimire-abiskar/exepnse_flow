package com.example.mcptestapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Database(entities = [ExpenseEntity::class, BudgetEntity::class], version = 2, exportSchema = false)
@TypeConverters(YearMonthConverter::class)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao() : BudgetDao
}
