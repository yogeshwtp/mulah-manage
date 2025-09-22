package com.example.mulahmanage.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mulahmanage.R
import com.example.mulahmanage.data.Transaction
import com.example.mulahmanage.data.TransactionType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToEditTransaction: (Int) -> Unit
) {
    val currentBalance by viewModel.currentBalance.collectAsStateWithLifecycle()
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()

    var showAddMoneyDialog by remember { mutableStateOf(false) }
    var balanceVisible by remember { mutableStateOf(true) }

    if (showAddMoneyDialog) {
        AddMoneyDialog(
            onDismiss = { showAddMoneyDialog = false },
            onConfirm = { amount ->
                viewModel.addTransaction(amount, TransactionType.INCOME, "Income", "Pocket Money")
                showAddMoneyDialog = false
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("New Expense") },
                icon = { Icon(Icons.Rounded.Add, contentDescription = "Add Expense") },
                onClick = onNavigateToAddExpense,
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DashboardHeader(balanceVisible, currentBalance) {
                    balanceVisible = !balanceVisible
                }
            }
            item {
                Button(onClick = { showAddMoneyDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Add Pocket Money")
                }
            }
            item {
                Text(
                    "Recent Activity",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            // Animated visibility for the empty state and the list
            if (transactions.isEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(500, delayMillis = 200)),
                        exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(500))
                    ) {
                        EmptyState()
                    }
                }
            } else {
                items(transactions, key = { it.id }) { transaction ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.deleteTransaction(transaction)
                                true
                            } else false
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        modifier = Modifier.animateItemPlacement(), // Animates item reordering
                        enableDismissFromStartToEnd = false,
                        backgroundContent = { SwipeToDeleteBackground(dismissState = dismissState) }
                    ) {
                        Box(
                            modifier = Modifier.combinedClickable(
                                onClick = {},
                                onLongClick = { onNavigateToEditTransaction(transaction.id) }
                            )
                        ) {
                            TransactionItem(transaction = transaction)
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun DashboardHeader(balanceVisible: Boolean, currentBalance: Double, onToggleVisibility: () -> Unit) {
    // Animate the balance amount when it changes
    val animatedBalance by animateFloatAsState(targetValue = currentBalance.toFloat(), label = "balance_animation")

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(getGreeting(), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = if (balanceVisible) "₹${String.format("%,.2f", animatedBalance)}" else "₹*,***.**",
                style = MaterialTheme.typography.displaySmall,
                letterSpacing = 1.sp
            )
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = if (balanceVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                    contentDescription = "Toggle Balance Visibility"
                )
            }
        }
        Text("Current Balance", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 50.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("All clear!", style = MaterialTheme.typography.titleLarge)
            Text(
                "Add your first expense using the '+' button.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteBackground(dismissState: SwipeToDismissBoxState) {
    val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            Icons.Rounded.Delete,
            contentDescription = "Delete",
            tint = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = getIconForCategory(transaction.category)),                contentDescription = transaction.category,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.category, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(formatDate(transaction.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = if (transaction.type == TransactionType.INCOME) "+₹%.2f".format(transaction.amount) else "-₹%.2f".format(transaction.amount),
                color = if (transaction.type == TransactionType.INCOME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun getGreeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good Morning! ☀️"
        in 12..16 -> "Good Afternoon! 👋"
        else -> "Good Evening! 🌙"
    }
}

private fun getIconForCategory(category: String): Int {
    return when (category.lowercase()) {
        "food" -> R.drawable.hamburger_duotone
        "transport" -> R.drawable.bus_duotone
        "shopping" -> R.drawable.shopping_bag_duotone // Assuming you downloaded and imported this
        "bills" -> R.drawable.receipt_duotone // Assuming you downloaded and imported this
        "income" -> R.drawable.arrow_up_duotone // Assuming you downloaded and imported this
        else -> R.drawable.wallet_duotone // Assuming you downloaded and imported this
    }
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return format.format(date)
}

@Composable
private fun AddMoneyDialog(onDismiss: () -> Unit, onConfirm: (amount: Double) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Pocket Money") },
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
            }) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

