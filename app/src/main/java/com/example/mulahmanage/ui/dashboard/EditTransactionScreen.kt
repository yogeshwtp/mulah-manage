package com.example.mulahmanage.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mulahmanage.data.Transaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    viewModel: DashboardViewModel,
    transactionId: Int,
    onNavigateBack: () -> Unit
) {
    val transactionToEdit = remember { viewModel.getTransaction(transactionId) }

    if (transactionToEdit == null) {
        // This is a safeguard. If the transaction is not found, just go back.
        LaunchedEffect(Unit) {
            onNavigateBack()
        }
        return
    }

    // State for the form fields, pre-filled with existing data
    var amountText by remember { mutableStateOf(transactionToEdit.amount.toString()) }
    var notesText by remember { mutableStateOf(transactionToEdit.notes) }
    val categories = listOf("Food", "Transport", "Shopping", "Bills", "Income", "Other")
    var selectedCategory by remember { mutableStateOf(transactionToEdit.category) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount") },
                prefix = { Text("₹") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Category", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { category ->
                    FilterChip(
                        selected = category == selectedCategory,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }

            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val newAmount = amountText.toDoubleOrNull()
                    if (newAmount != null) {
                        val updatedTransaction = transactionToEdit.copy(
                            amount = newAmount,
                            category = selectedCategory,
                            notes = notesText
                        )
                        viewModel.updateTransaction(updatedTransaction)
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("SAVE CHANGES")
            }
        }
    }
}