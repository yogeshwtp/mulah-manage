package com.example.mulahmanage.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mulahmanage.data.*
import com.example.mulahmanage.repository.TransactionRepository
import com.example.mulahmanage.ui.settings.SettingsDataStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// New data class to combine budget and spending data for the UI
data class BudgetDetail(
    val category: String,
    val budgetAmount: Double,
    val amountSpent: Double
)

class DashboardViewModel(private val repository: TransactionRepository, private val settingsDataStore: SettingsDataStore) : ViewModel() {
    // Helper extension functions
    private fun <T> Flow<List<T>>.stateInDefault(initialValue: List<T> = emptyList()): StateFlow<List<T>> {
        return this.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = initialValue
        )
    }

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
    val allQuickExpenses: StateFlow<List<QuickExpense>> = repository.allQuickExpenses.stateInDefault()

    // New StateFlow for Budget Details
    val budgetDetails: StateFlow<List<BudgetDetail>> = repository.allBudgets
        .combine(repository.expenseByCategory) { budgets, expenses ->
            budgets.map { budget ->
                val spent = expenses.find { it.category == budget.category }?.total ?: 0.0
                BudgetDetail(
                    category = budget.category,
                    budgetAmount = budget.budgetAmount,
                    amountSpent = spent
                )
            }
        }.stateInDefault()
    val themeOption: StateFlow<String> = settingsDataStore.themeOption
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), SettingsDataStore.THEME_SYSTEM)


    // Existing Transaction Functions
    fun addTransaction(amount: Double, type: TransactionType, category: String, notes: String) {
        viewModelScope.launch {
            repository.insert(Transaction(
                amount = amount,
                type = type,
                category = category,
                notes = notes,
                date = System.currentTimeMillis()
            ))
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.update(transaction)
        }
    }

    fun getTransaction(id: Int): Transaction? {
        return allTransactions.value.find { it.id == id }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.delete(transaction)
        }
    }

    // Quick Expense Functions
    fun addQuickExpense(name: String, amount: Double, category: String) {
        viewModelScope.launch {
            repository.insertQuickExpense(QuickExpense(
                name = name,
                amount = amount,
                category = category
            ))
        }
    }

    // New Budget Management Functions
    fun upsertBudget(category: String, amount: Double) {
        viewModelScope.launch {
            repository.upsertBudget(Budget(category, amount))
        }
    }

    fun deleteBudget(category: String) {
        viewModelScope.launch {
            repository.deleteBudget(category)
        }
    }

    // Data Management Functions
    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
    fun setThemeOption(option: String) {
        viewModelScope.launch {
            settingsDataStore.setThemeOption(option)
        }
    }
}

class DashboardViewModelFactory(private val repository: TransactionRepository,     private val settingsDataStore: SettingsDataStore
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository, settingsDataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}