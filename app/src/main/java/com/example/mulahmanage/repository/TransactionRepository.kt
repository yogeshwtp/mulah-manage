package com.example.mulahmanage.repository

import com.example.mulahmanage.data.CategorySum
import com.example.mulahmanage.data.Transaction
import com.example.mulahmanage.data.TransactionDao
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val totalIncome: Flow<Double?> = transactionDao.getTotalIncome()
    val totalExpenses: Flow<Double?> = transactionDao.getTotalExpenses()
    val expenseByCategory: Flow<List<CategorySum>> = transactionDao.getExpenseSumByCategory()

    suspend fun insert(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    // NEW FUNCTION: Allows the ViewModel to request a transaction deletion.
    suspend fun delete(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }
    suspend fun clearAll() {
        transactionDao.clearAllTransactions()
    }
}
