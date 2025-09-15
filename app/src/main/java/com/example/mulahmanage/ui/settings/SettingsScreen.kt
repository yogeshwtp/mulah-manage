package com.example.mulahmanage.ui.settings

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.mulahmanage.data.Transaction
import com.example.mulahmanage.ui.dashboard.DashboardViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: DashboardViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val transactions by viewModel.allTransactions.collectAsState()
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
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
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
            Divider()
            SettingsItem(
                icon = Icons.Default.DeleteForever,
                title = "Erase All Data",
                subtitle = "Permanently delete all transactions",
                onClick = { showDeleteConfirmation = true }
            )
            Divider()
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