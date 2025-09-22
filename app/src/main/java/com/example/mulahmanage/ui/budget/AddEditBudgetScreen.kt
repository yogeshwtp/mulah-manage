package com.example.mulahmanage.ui.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mulahmanage.ui.dashboard.DashboardViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBudgetScreen(
    viewModel: DashboardViewModel,
    category: String?,
    onNavigateBack: () -> Unit
) {
    val isEditMode = category != null
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(category ?: "") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val expenseCategoriesState by viewModel.expenseByCategory.collectAsStateWithLifecycle()
    val dynamicCategories = remember(expenseCategoriesState) {
        expenseCategoriesState.map { it.category }.distinct().sorted()
    }

    // This effect now correctly handles both edit mode and add mode initialization.
    // It runs when the screen first launches and re-runs if dynamicCategories changes.
    LaunchedEffect(dynamicCategories, isEditMode) {
        if (isEditMode) {
            // In edit mode, find the budget and pre-fill the form
            val budget = viewModel.budgetDetails.value.find { it.category == category }
            if (budget != null) {
                amountText = String.format(Locale.US, "%.0f", budget.budgetAmount)
                selectedCategory = budget.category
            }
        } else {
            // In add mode, if no category is selected yet and the list is available,
            // default to the first category.
            if (selectedCategory.isEmpty() && dynamicCategories.isNotEmpty()) {
                selectedCategory = dynamicCategories.first()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(if (isEditMode) "Edit Budget" else "Create Budget")
            })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (dynamicCategories.isEmpty() && !isEditMode) {
                Text("No expense categories found. Please add an expense to create a budget.")
            } else {
                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded && !isEditMode,
                    onExpandedChange = { if (!isEditMode) isDropdownExpanded = !isDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        enabled = !isEditMode,
                        label = { Text("Category") },
                        trailingIcon = {
                            if (!isEditMode) ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    if (!isEditMode) {
                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false }
                        ) {
                            dynamicCategories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Amount field
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Monthly Budget Amount") },
                prefix = { Text("₹") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // Save button
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (selectedCategory.isNotBlank() && amount != null && amount > 0) {
                        viewModel.upsertBudget(selectedCategory, amount)
                        onNavigateBack()
                    }
                },
                enabled = selectedCategory.isNotBlank() && amountText.toDoubleOrNull() ?: 0.0 > 0.0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("SAVE BUDGET")
            }
        }
    }
}
