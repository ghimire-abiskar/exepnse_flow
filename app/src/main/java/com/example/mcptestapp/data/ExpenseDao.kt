package com.example.mcptestapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY dateEpochDay DESC, id DESC")
    fun observeAll(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY dateEpochDay DESC, id DESC")
    suspend fun getAll(): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<ExpenseEntity>)

    @Delete
    suspend fun delete(expense: ExpenseEntity)


}
