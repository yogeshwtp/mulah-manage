package com.example.mulahmanage.repository

import com.example.mulahmanage.data.*
import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val quickExpenseDao: QuickExpenseDao
) {
    // Transaction flows
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val totalIncome: Flow<Double?> = transactionDao.getTotalIncome()
    val totalExpenses: Flow<Double?> = transactionDao.getTotalExpenses()
    val expenseByCategory: Flow<List<CategorySum>> = transactionDao.getExpenseSumByCategory()

    // Quick Expense flows
    val allQuickExpenses: Flow<List<QuickExpense>> = quickExpenseDao.getAllQuickExpenses()

    suspend fun insert(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    // NEW: Update a transaction
    suspend fun update(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun insertQuickExpense(quickExpense: QuickExpense) {
        quickExpenseDao.insertQuickExpense(quickExpense)
    }

    suspend fun delete(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun clearAll() {
        transactionDao.clearAllTransactions()
    }
}

