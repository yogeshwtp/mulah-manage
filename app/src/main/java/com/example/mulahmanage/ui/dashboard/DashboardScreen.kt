package com.example.mulahmanage.ui.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mulahmanage.data.TransactionType
import com.example.mulahmanage.ui.theme.RedError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToAddExpense: () -> Unit
) {
    val currentBalance by viewModel.currentBalance.collectAsStateWithLifecycle()
    val safeToSpend by viewModel.safeToSpend.collectAsStateWithLifecycle()
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    var showAddMoneyDialog by remember { mutableStateOf(false) }

    if (showAddMoneyDialog) {
        AddMoneyDialog(
            onDismiss = { showAddMoneyDialog = false },
            onConfirm = { amount ->
                viewModel.addTransaction(
                    amount = amount,
                    type = TransactionType.INCOME,
                    category = "Income",
                    notes = "Added manually"
                )
                showAddMoneyDialog = false
            }
        )
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Current Balance",
                amount = currentBalance,
                modifier = Modifier.weight(1f).height(100.dp)
            )
            StatCard(
                title = "Safe to Spend Today",
                amount = safeToSpend,
                modifier = Modifier.weight(1f).height(100.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onNavigateToAddExpense,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(text = "+ ADD EXPENSE")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { showAddMoneyDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(text = "+ ADD MONEY")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Recent Transactions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(transactions, key = { it.id }) { transaction ->
                val swipeToDismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissDirection ->
                        when (dismissDirection) {
                            SwipeToDismissBoxValue.StartToEnd, SwipeToDismissBoxValue.EndToStart -> {
                                viewModel.deleteTransaction(transaction)
                                true
                            }
                            SwipeToDismissBoxValue.Settled -> false
                        }
                    }
                )

                SwipeToDismissBox(
                    state = swipeToDismissState,
                    enableDismissFromEndToStart = true, // This enables right-to-left swipe
                    enableDismissFromStartToEnd = false, // This disables left-to-right swipe
                    backgroundContent = {
                        val color by animateColorAsState(
                            targetValue = if (swipeToDismissState.targetValue == SwipeToDismissBoxValue.EndToStart) RedError else Color.Transparent,
                            label = "background color"
                        )
                        val scale by animateFloatAsState(
                            if (swipeToDismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 1.2f else 0.8f,
                            label = "icon scale"
                        )

                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(color)
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Icon",
                                modifier = Modifier.scale(scale),
                                tint = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                ) {
                    TransactionItem(transaction = transaction)
                }
                Divider()
            }
        }
    }
}

@Composable
private fun AddMoneyDialog(
    onDismiss: () -> Unit,
    onConfirm: (amount: Double) -> Unit
) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Money") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Amount") },
                prefix = { Text("₹") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(onClick = {
                val amount = text.toDoubleOrNull() ?: 0.0
                if (amount > 0) onConfirm(amount)
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}