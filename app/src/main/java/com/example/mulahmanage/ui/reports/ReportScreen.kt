package com.example.mulahmanage.ui.reports

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mulahmanage.data.CategorySum
import com.example.mulahmanage.ui.dashboard.DashboardViewModel
import com.example.mulahmanage.ui.theme.BlueAccent
import com.example.mulahmanage.ui.theme.GreenSuccess
import com.example.mulahmanage.ui.theme.Orange
import com.example.mulahmanage.ui.theme.PurpleAccent
import com.example.mulahmanage.ui.theme.RedError
import com.example.mulahmanage.ui.theme.YellowWarning

// More distinct and meaningful colors for different categories
private val PieChartColors = listOf(
    BlueAccent,        // Blue for Bills/Utilities
    GreenSuccess,      // Green for Food/Groceries
    Orange,            // Orange for Shopping
    PurpleAccent,      // Purple for Entertainment
    RedError,          // Red for Emergency/Medical
    YellowWarning,     // Yellow for Transport
    Color(0xFF00BCD4), // Cyan for Education
    Color(0xFF795548), // Brown for Other
    Color(0xFF9E9E9E), // Grey for Miscellaneous
    Color(0xFFE91E63)  // Pink for Personal Care
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: DashboardViewModel) {
    val expenseCategories by viewModel.expenseByCategory.collectAsStateWithLifecycle()
    val totalExpenses = expenseCategories.sumOf { it.total }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "Total Spending",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "₹${String.format("%.2f", totalExpenses)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(32.dp))
                if (expenseCategories.isNotEmpty()) {
                    PieChart(
                        data = expenseCategories.map { it.total },
                        colors = PieChartColors.take(expenseCategories.size)
                    )
                } else {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No expense data to show.")
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
            itemsIndexed(expenseCategories) { index, categoryData ->
                CategoryReportItem(
                    categorySum = categoryData,
                    total = totalExpenses,
                    color = PieChartColors[index % PieChartColors.size]
                )
                Divider()
            }
        }
    }
}

@Composable
fun PieChart(
    data: List<Double>,
    colors: List<Color>,
    radiusOuter: Dp = 100.dp,
    chartBarWidth: Dp = 35.dp,
    animDuration: Int = 1000,
) {
    val totalSum = data.sum()
    val floatValue = mutableListOf<Float>()
    data.forEach {
        floatValue.add(360 * it.toFloat() / totalSum.toFloat())
    }

    var animationPlayed by remember { mutableStateOf(false) }
    var lastValue = 0f
    val animateRotation by animateFloatAsState(
        targetValue = if (animationPlayed) 360f else 0f,
        animationSpec = tween(
            durationMillis = animDuration,
            delayMillis = 0,
            easing = LinearOutSlowInEasing
        ), label = ""
    )
    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    Box(
        modifier = Modifier.size(radiusOuter * 2f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(radiusOuter * 2f)
                .rotate(animateRotation)
        ) {
            floatValue.forEachIndexed { index, value ->
                drawArc(
                    color = colors[index],
                    startAngle = lastValue,
                    sweepAngle = value,
                    useCenter = false,
                    style = Stroke(chartBarWidth.toPx(), cap = StrokeCap.Butt)
                )
                lastValue += value
            }
        }
    }
}

@Composable
fun CategoryReportItem(categorySum: CategorySum, total: Double, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, shape = MaterialTheme.shapes.small)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(categorySum.category, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Column(horizontalAlignment = Alignment.End) {
            Text("₹${String.format("%.2f", categorySum.total)}", fontWeight = FontWeight.SemiBold)
            val percentage = if (total > 0) (categorySum.total / total) * 100 else 0.0
            Text(
                "${String.format("%.1f", percentage)}%",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}