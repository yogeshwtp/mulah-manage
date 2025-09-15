package com.example.mulahmanage.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quick_expenses")
data class QuickExpense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val amount: Double,
    val category: String
)
