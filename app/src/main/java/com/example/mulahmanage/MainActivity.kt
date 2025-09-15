package com.example.mulahmanage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.mulahmanage.data.AppDatabase
import com.example.mulahmanage.repository.TransactionRepository
import com.example.mulahmanage.ui.dashboard.DashboardViewModel
import com.example.mulahmanage.ui.dashboard.DashboardViewModelFactory
import com.example.mulahmanage.ui.main.MainScreen
import com.example.mulahmanage.ui.theme.MulahManageTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Dependency Setup ---
        val database = AppDatabase.getDatabase(this)
        // The repository now needs both DAOs to be passed to it
        val repository = TransactionRepository(database.transactionDao(), database.quickExpenseDao())
        val factory = DashboardViewModelFactory(repository)
        val viewModel: DashboardViewModel by viewModels { factory }

        setContent {
            MulahManageTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

