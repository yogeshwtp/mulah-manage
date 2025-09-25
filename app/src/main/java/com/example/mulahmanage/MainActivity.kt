package com.example.mulahmanage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mulahmanage.data.AppDatabase
import com.example.mulahmanage.data.SettingsDataStore
import com.example.mulahmanage.repository.TransactionRepository
import com.example.mulahmanage.ui.dashboard.DashboardViewModel
import com.example.mulahmanage.ui.dashboard.DashboardViewModelFactory
import com.example.mulahmanage.ui.main.MainScreen
import com.example.mulahmanage.ui.onboarding.OnboardingScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.mulahmanage.ui.theme.MulahManageTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        // --- Dependency Setup ---
        val database = AppDatabase.getDatabase(this)
        val settingsDataStore = SettingsDataStore(this)
        // This is the corrected line. It now passes all three DAOs.
        val repository = TransactionRepository(
            database.transactionDao(),
            database.quickExpenseDao(),
            database.budgetDao()
        )

        val factory = DashboardViewModelFactory(repository, settingsDataStore)
        val viewModel: DashboardViewModel by viewModels { factory }

        setContent {
            val themeOption by viewModel.themeOption.collectAsStateWithLifecycle()
            val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsStateWithLifecycle()
            val useDarkTheme = when (themeOption) {
                SettingsDataStore.THEME_LIGHT -> false
                SettingsDataStore.THEME_DARK -> true
                else -> isSystemInDarkTheme()
            }

            MulahManageTheme(darkTheme = useDarkTheme) {
                if (hasCompletedOnboarding) {
                    MainScreen(viewModel = viewModel)
                } else {
                    OnboardingScreen(onCompleted = {
                        viewModel.setOnboardingCompleted()
                    })
                }
            }
        }
    }
}