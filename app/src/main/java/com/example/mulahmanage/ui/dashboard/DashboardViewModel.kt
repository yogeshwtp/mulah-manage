package com.example.mulahmanage.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mulahmanage.data.*
import com.example.mulahmanage.repository.TransactionRepository
import com.example.mulahmanage.data.SettingsDataStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Data class to combine budget and spending data for the UI
data class BudgetDetail(
    val category: String,
    val budgetAmount: Double,
    val amountSpent: Double
)

class DashboardViewModel(
    private val repository: TransactionRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    // Helper extension functions for StateFlow creation
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

    // NEW: Date selection functionality
    private val _selectedDate = MutableStateFlow(LocalDate.now())

    // NEW: Formatted month string for the UI header (e.g., "September 2025")
    val formattedMonth: StateFlow<String> = _selectedDate.map {
        it.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), "")

    // Core StateFlows
    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions.stateInDefault()

    val currentBalance: StateFlow<Double> =
        repository.totalIncome.combine(repository.totalExpenses) { income, expenses ->
            (income ?: 0.0) - (expenses ?: 0.0)
        }.stateInDefault(0.0)

    val expenseByCategory: StateFlow<List<CategorySum>> = repository.expenseByCategory.stateInDefault()
    val allQuickExpenses: StateFlow<List<QuickExpense>> = repository.allQuickExpenses.stateInDefault()

    // UPDATED: Filtered transactions by selected month
    @OptIn(kotlinx.coroutines.FlowPreview::class)
    val filteredTransactions: StateFlow<List<Transaction>> = _selectedDate
        .flatMapLatest { date ->
            repository.getTransactionsForMonth(date.year, date.monthValue)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Budget Details StateFlow
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

    // Settings StateFlows
    val themeOption: StateFlow<String> = settingsDataStore.themeOption
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), SettingsDataStore.THEME_SYSTEM)

    val hasCompletedOnboarding: StateFlow<Boolean> = settingsDataStore.hasCompletedOnboarding
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    // NEW: Month Navigation Functions
    fun selectNextMonth() {
        _selectedDate.value = _selectedDate.value.plusMonths(1)
    }

    fun selectPreviousMonth() {
        _selectedDate.value = _selectedDate.value.minusMonths(1)
    }

    // Transaction Management Functions
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

    // Budget Management Functions
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

    // Settings Functions
    fun setThemeOption(option: String) {
        viewModelScope.launch {
            settingsDataStore.setThemeOption(option)
        }
    }

    fun setOnboardingCompleted() {
        viewModelScope.launch {
            settingsDataStore.setOnboardingCompleted(true)
        }
    }
}

class DashboardViewModelFactory(
    private val repository: TransactionRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository, settingsDataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}