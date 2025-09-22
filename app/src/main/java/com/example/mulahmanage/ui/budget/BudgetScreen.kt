package com.example.mulahmanage.ui.budget

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mulahmanage.ui.dashboard.DashboardViewModel
import com.example.mulahmanage.ui.theme.PrimaryGreen
import com.example.mulahmanage.ui.theme.RedError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: DashboardViewModel,
    onNavigateToAddBudget: () -> Unit,
    onNavigateToEditBudget: (String) -> Unit // New navigation callback
) {
    val budgetDetails by viewModel.budgetDetails.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Your Budgets") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddBudget) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Budget")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(budgetDetails, key = { it.category }) { detail ->
                BudgetCard(
                    category = detail.category,
                    budgetAmount = detail.budgetAmount,
                    amountSpent = detail.amountSpent,
                    onEdit = { onNavigateToEditBudget(detail.category) }, // Pass category to edit
                    onDelete = { viewModel.deleteBudget(detail.category) }
                )
            }
        }
    }
}

@Composable
fun BudgetCard(
    category: String,
    budgetAmount: Double,
    amountSpent: Double,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val progress = (amountSpent / budgetAmount).toFloat().coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress_anim")
    val remaining = budgetAmount - amountSpent
    val remainingColor = if (remaining >= 0) PrimaryGreen else RedError
    var isMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "₹${String.format("%,.0f", remaining)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = remainingColor
                )
                // More Options Icon & Menu
                Box {
                    IconButton(onClick = { isMenuExpanded = true }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                onEdit()
                                isMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                onDelete()
                                isMenuExpanded = false
                            }
                        )
                    }
                }
            }
            // Progress Bar
            LinearProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(MaterialTheme.shapes.small)
            )
            // Subtitles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spent: ₹${String.format("%,.0f", amountSpent)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Budget: ₹${String.format("%,.0f", budgetAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
