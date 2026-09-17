package com.example.mcptestapp.data
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.YearMonth

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val month: YearMonth,
    val budgetLimit: Double
)