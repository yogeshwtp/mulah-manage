package com.example.mulahmanage.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mulahmanage.data.*
import com.example.mulahmanage.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel(private val repository: TransactionRepository) : ViewModel() {
    // Helper extension function for lists
    private fun <T> Flow<List<T>>.stateInDefault(initialValue: List<T> = emptyList()): StateFlow<List<T>> {
        return this.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = initialValue
        )
    }

    // Helper extension function for single values
    private fun <T> Flow<T?>.stateInDefault(initialValue: T): StateFlow<T> {
        return this.filterNotNull().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = initialValue
        )
    }

    // Existing StateFlows
    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions.stateInDefault()
    val currentBalance: StateFlow<Double> =
        repository.totalIncome.combine(repository.totalExpenses) { income, expenses ->
            (income ?: 0.0) - (expenses ?: 0.0)
        }.stateInDefault(0.0)
    val expenseByCategory: StateFlow<List<CategorySum>> = repository.expenseByCategory.stateInDefault()

    // New StateFlow for quick expenses
    val allQuickExpenses: StateFlow<List<QuickExpense>> = repository.allQuickExpenses.stateInDefault()

    fun addTransaction(amount: Double, type: TransactionType, category: String, notes: String) {
        viewModelScope.launch {
            repository.insert(Transaction(amount = amount, type = type, category = category, notes = notes, date = System.currentTimeMillis()))
        }
    }

    fun addQuickExpense(name: String, amount: Double, category: String) {
        viewModelScope.launch {
            repository.insertQuickExpense(QuickExpense(name = name, amount = amount, category = category))
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch { repository.delete(transaction) }
    }

    fun clearAllData() {
        viewModelScope.launch { repository.clearAll() }
    }
}

class DashboardViewModelFactory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

