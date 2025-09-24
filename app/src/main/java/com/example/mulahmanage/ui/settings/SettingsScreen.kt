package com.example.mulahmanage.ui.settings

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mulahmanage.data.Transaction
import com.example.mulahmanage.ui.dashboard.DashboardViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: DashboardViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Erase All Data?") },
            text = { Text("This action is permanent and cannot be undone. Are you sure?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirmation = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // NEW: Theme selection section
            Text("Appearance", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
            ThemeSelector(viewModel = viewModel)
            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))

            // Data Management Section
            Text("Data Management", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 24.dp, bottom = 8.dp))
            SettingsItem(
                icon = Icons.Default.IosShare,
                title = "Export All Data",
                subtitle = "Save a CSV file of all your transactions",
                onClick = {
                    coroutineScope.launch {
                        exportTransactionsToCsv(context, transactions)
                    }
                }
            )
            HorizontalDivider()
            SettingsItem(
                icon = Icons.Default.DeleteForever,
                title = "Erase All Data",
                subtitle = "Permanently delete all transactions",
                onClick = { showDeleteConfirmation = true }
            )
            HorizontalDivider()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelector(viewModel: DashboardViewModel) {
    val themeOption by viewModel.themeOption.collectAsStateWithLifecycle()
    val options = listOf(SettingsDataStore.THEME_LIGHT, SettingsDataStore.THEME_DARK, SettingsDataStore.THEME_SYSTEM)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            FilterChip(
                selected = themeOption == option,
                onClick = { viewModel.setThemeOption(option) },
                label = { Text(option) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

private fun exportTransactionsToCsv(context: Context, transactions: List<Transaction>) {
    val csvHeader = "ID,Date,Type,Category,Amount,Notes\n"
    val csvBody = transactions.joinToString(separator = "\n") {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(it.date))
        "${it.id},${date},${it.type},\"${it.category}\",${it.amount},\"${it.notes}\""
    }
    val csvContent = csvHeader + csvBody

    try {
        val cacheDir = context.cacheDir
        val file = File(cacheDir, "transactions_export_${System.currentTimeMillis()}.csv")
        file.writeText(csvContent)

        val fileUri = FileProvider.getUriForFile(
            context,
            "com.example.mulahmanage.provider",
            file
        )

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, fileUri)
            type = "text/csv"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export Transactions"))

    } catch (e: Exception) {
        e.printStackTrace()
    }
}
