package com.example.mulahmanage.ui.dashboard

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToEditTransaction: (Int) -> Unit
) {
    val currentBalance by viewModel.currentBalance.collectAsStateWithLifecycle()
    val formattedMonth by viewModel.formattedMonth.collectAsStateWithLifecycle()
    val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    var balanceVisible by remember { mutableStateOf(true) }
    var showAddMoneyDialog by remember { mutableStateOf(false) }

    val groupedTransactions = remember(filteredTransactions) {
        filteredTransactions.groupBy {
            Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
        }
    }

    if (showAddMoneyDialog) {
        AddMoneyDialog(
            onDismiss = { showAddMoneyDialog = false },
            onConfirm = { amount ->
                viewModel.addTransaction(amount, TransactionType.INCOME, "Income", "Pocket Money")
                showAddMoneyDialog = false
            }
        )
    }

    Scaffold { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            DashboardHeader(balanceVisible, currentBalance) {
                balanceVisible = !balanceVisible
            }

            MonthSelector(
                formattedMonth = formattedMonth,
                onPrevious = { viewModel.selectPreviousMonth() },
                onNext = { viewModel.selectNextMonth() }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onNavigateToAddExpense,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50), // Green color
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add Expense")
                    Spacer(Modifier.width(8.dp))
                    Text("New Expense")
                }
                OutlinedButton(
                    onClick = { showAddMoneyDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Add Pocket Money")
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (groupedTransactions.isEmpty()) {
                    item { EmptyState() }
                } else {
                    groupedTransactions.forEach { (date, transactionsOnDate) ->
                        stickyHeader {
                            DateHeader(date = date)
                        }
                        items(transactionsOnDate, key = { it.id }) { transaction ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = {
                                    if (it == SwipeToDismissBoxValue.EndToStart) {
                                        viewModel.deleteTransaction(transaction)
                                        return@rememberSwipeToDismissBoxState true
                                    }
                                    false
                                }
                            )
                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromEndToStart = true,
                                enableDismissFromStartToEnd = false,
                                backgroundContent = {
                                    SwipeToDeleteBackground(dismissState = dismissState)
                                }
                            ) {
                                Box(modifier = Modifier.pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = { onNavigateToEditTransaction(transaction.id) }
                                    )
                                }) {
                                    TransactionItem(transaction = transaction)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun DashboardHeader(balanceVisible: Boolean, currentBalance: Double, onToggleVisibility: () -> Unit) {
    val animatedBalance by animateFloatAsState(targetValue = currentBalance.toFloat(), label = "balance_animation")

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
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
                painter = painterResource(id = getIconForCategory(transaction.category)),
                contentDescription = transaction.category,
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

@Composable
fun MonthSelector(formattedMonth: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) { Icon(Icons.Rounded.ChevronLeft, "Previous Month") }
        Text(formattedMonth, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        IconButton(onClick = onNext) { Icon(Icons.Rounded.ChevronRight, "Next Month") }
    }
}

@Composable
fun DateHeader(date: LocalDate) {
    val dayText = when {
        date.isEqual(LocalDate.now()) -> "Today"
        date.isEqual(LocalDate.now().minusDays(1)) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("EEEE, dd MMM"))
    }
    Text(
        text = dayText,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 8.dp, horizontal = 16.dp)
    )
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
        "shopping" -> R.drawable.shopping_bag_duotone
        "bills" -> R.drawable.receipt_duotone
        "income" -> R.drawable.arrow_up_duotone
        else -> R.drawable.wallet_duotone
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
