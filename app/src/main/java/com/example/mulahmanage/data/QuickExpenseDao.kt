package com.example.mulahmanage.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuickExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuickExpense(quickExpense: QuickExpense)

    @Query("SELECT * FROM quick_expenses ORDER BY name ASC")
    fun getAllQuickExpenses(): Flow<List<QuickExpense>>
}
