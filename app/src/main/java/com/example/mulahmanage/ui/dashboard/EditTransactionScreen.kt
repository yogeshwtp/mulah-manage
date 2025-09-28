package com.example.mulahmanage.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mulahmanage.data.Transaction
import com.example.mulahmanage.data.TransactionType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    viewModel: DashboardViewModel,
    transaction: Transaction,
    transactionId: Int,
    onNavigateBack: () -> Unit
) {
    // Get the transaction to edit, with fallback to the passed transaction
    val transactionToEdit = remember { viewModel.getTransaction(transactionId) ?: transaction }

    // State for the form fields, pre-filled with existing data
    var amountText by remember { mutableStateOf(transactionToEdit.amount.toString()) }
    var notesText by remember { mutableStateOf(transactionToEdit.notes) }
    val categories = listOf("Food", "Transport", "Shopping", "Bills", "Income", "Other")
    var selectedCategory by remember { mutableStateOf(transactionToEdit.category) }

    // Date picker state
    var selectedDateMillis by remember { mutableStateOf(transactionToEdit.date) }
    var showDatePicker by remember { mutableStateOf(false) }
    val formatter = remember { SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()) }

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDateMillis = millis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
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
            // Amount input field
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount") },
                prefix = { Text("₹") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Category selection
            Text("Category", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.chunked(3).forEach { rowCategories ->
                    Column {
                        rowCategories.forEach { category ->
                            FilterChip(
                                selected = category == selectedCategory,
                                onClick = { selectedCategory = category },
                                label = { Text(category) },
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Date selection
            Text("Date", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = formatter.format(Date(selectedDateMillis)),
                onValueChange = { }, // Read-only
                readOnly = true,
                label = { Text("Transaction Date") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Rounded.CalendarToday, contentDescription = "Select date")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            )

            // Notes input field
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.weight(1f))

            // Update button
            Button(
                onClick = {
                    val newAmount = amountText.toDoubleOrNull() ?: 0.0
                    if (newAmount > 0) {
                        val updatedTransaction = transactionToEdit.copy(
                            amount = newAmount,
                            category = selectedCategory,
                            notes = notesText,
                            date = selectedDateMillis
                        )
                        viewModel.updateTransaction(updatedTransaction)
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = amountText.toDoubleOrNull()?.let { it > 0 } ?: false
            ) {
                Text("UPDATE TRANSACTION")
            }
        }
    }
}