package com.example.mulahmanage.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey
    val category: String,
    val budgetAmount: Double
)
