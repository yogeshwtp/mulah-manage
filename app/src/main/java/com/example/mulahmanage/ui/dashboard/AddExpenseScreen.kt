package com.example.mulahmanage.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mulahmanage.data.QuickExpense
import com.example.mulahmanage.data.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: DashboardViewModel,
    onNavigateBack: () -> Unit
) {
    val quickExpenses by viewModel.allQuickExpenses.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }
    var showAddQuickExpenseDialog by remember { mutableStateOf(false) }

    if (showAddQuickExpenseDialog) {
        AddQuickExpenseDialog(
            onDismiss = { showAddQuickExpenseDialog = false },
            onConfirm = { name, amount, category ->
                viewModel.addQuickExpense(name, amount, category)
                showAddQuickExpenseDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log a New Expense") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Quick Add") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Manual") })
            }

            when (selectedTab) {
                0 -> QuickAddTab(
                    quickExpenses = quickExpenses,
                    onQuickAdd = {
                        viewModel.addTransaction(it.amount, TransactionType.EXPENSE, it.category, it.name)
                        onNavigateBack()
                    },
                    onAddNew = { showAddQuickExpenseDialog = true }
                )
                1 -> ManualAddTab(onAddExpense = { amount, category, notes ->
                    viewModel.addTransaction(amount, TransactionType.EXPENSE, category, notes)
                    onNavigateBack()
                })
            }
        }
    }
}

@Composable
fun QuickAddTab(
    quickExpenses: List<QuickExpense>,
    onQuickAdd: (QuickExpense) -> Unit,
    onAddNew: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(quickExpenses) { expense ->
            QuickExpenseCard(expense = expense, onClick = { onQuickAdd(expense) })
        }
        item {
            AddNewCard(onClick = onAddNew)
        }
    }
}

@Composable
fun QuickExpenseCard(expense: QuickExpense, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(expense.name, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text("₹${expense.amount}", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            Text(expense.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun AddNewCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add New")
            Text("Create New", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ManualAddTab(
    onAddExpense: (amount: Double, category: String, notes: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    val categories = listOf("Food", "Transport", "Shopping", "Bills", "Other")
    var selectedCategory by remember { mutableStateOf(categories.first()) }

    Column(
        modifier = Modifier.padding(16.dp),
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
                val amount = amountText.toDoubleOrNull() ?: 0.0
                if (amount > 0) {
                    onAddExpense(amount, selectedCategory, notesText)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("SAVE EXPENSE")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddQuickExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, amount: Double, category: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val categories = listOf("Food", "Transport", "Shopping", "Bills", "Other")
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var isCategoryMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Quick Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Button Name (e.g., Chai)") })
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") }, prefix = { Text("₹") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                ExposedDropdownMenuBox(
                    expanded = isCategoryMenuExpanded,
                    onExpandedChange = { isCategoryMenuExpanded = !isCategoryMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryMenuExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = isCategoryMenuExpanded,
                        onDismissRequest = { isCategoryMenuExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(text = { Text(category) }, onClick = {
                                selectedCategory = category
                                isCategoryMenuExpanded = false
                            })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val parsedAmount = amount.toDoubleOrNull()
                if (name.isNotBlank() && parsedAmount != null && parsedAmount > 0) {
                    onConfirm(name, parsedAmount, selectedCategory)
                }
            }) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

