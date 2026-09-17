package com.example.mcptestapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudgetEntity(budget: BudgetEntity)

    @Query("SELECT budgetLimit FROM budgets WHERE month = :month")
    fun getBudgetLimit (month : String) : Flow<Double>
}