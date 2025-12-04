package com.cs407.savewise.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cs407.savewise.model.ExpenseRecord
import kotlin.math.*

@Composable
fun MonthlyExpenseChart(
    expenses: List<ExpenseRecord>,
    modifier: Modifier = Modifier
) {
    if (expenses.isEmpty()) {
        // Ideally this is handled by the parent screen now, but keeping safety check
        return
    }
    // Data Preparation
    val totalAmount = expenses.sumOf { it.amount }
    val categoryMap = expenses.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount }.toFloat() }


    // ✅ 分类颜色
    val colors = listOf(
        Color(0xFF4F8CF9),
        Color(0xFFFFC107),
        Color(0xFF66BB6A),
        Color(0xFFEF5350),
        Color(0xFFAB47BC),
        Color(0xFFFF7043)
    )

    // ✅ 当前选中的类别
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // ✅ 动画偏移量（点击时抬起）
    val liftOffset = 20f
    val animatedLift by animateFloatAsState(
        targetValue = if (selectedCategory != null) liftOffset else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    // ✅ UI 主体
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Monthly Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(220.dp) // Fixed size for the chart area
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val center =
                                Offset((size.width / 2).toFloat(), (size.height / 2).toFloat())
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            val angle = (atan2(
                                dy,
                                dx
                            ) * 180f / Math.PI + 360f + 90f) % 360f // +90 to align with startAngle -90

                            // Distance check to ensure tap is on the ring, not inside the hole or outside
                            val distance = sqrt(dx * dx + dy * dy)
                            val radius = size.width / 2f
                            val strokeWidth = 40.dp.toPx()
                            val innerRadius = radius - strokeWidth

                            if (distance in innerRadius..radius) {
                                var currentAngle = 0f
                                categoryMap.forEach { (category, sum) ->
                                    val sweep = (sum / totalAmount.toFloat()) * 360f
                                    if (angle >= currentAngle && angle < currentAngle + sweep) {
                                        selectedCategory =
                                            if (selectedCategory == category) null else category
                                        return@detectTapGestures
                                    }
                                    currentAngle += sweep
                                }
                            } else {
                                // Tap outside ring clears selection
                                selectedCategory = null
                            }
                        }
                    }
            ) {
                Canvas(
                    modifier = Modifier.matchParentSize()
                ) {
                    val strokeWidth = 40.dp.toPx()
                    val diameter = size.minDimension
                    val radius = diameter / 2

                    // Size for the arc drawing (accounts for stroke width centering)
                    val arcSize = Size(diameter - strokeWidth, diameter - strokeWidth)
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    var startAngle = -90f

                    categoryMap.entries.forEachIndexed { index, entry ->
                        val sweepAngle = (entry.value / totalAmount.toFloat()) * 360f
                        val color = colors[index % colors.size]

                        // Highlight effect for selected category
                        val isSelected = selectedCategory == entry.key
                        val currentStrokeWidth = if (isSelected) strokeWidth * 1.1f else strokeWidth
                        val currentAlpha = if (selectedCategory == null || isSelected) 1f else 0.3f

                        drawArc(
                            color = color.copy(alpha = currentAlpha),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false, // False = Donut/Ring
                            topLeft = if (isSelected)
                                Offset(
                                    topLeft.x - (currentStrokeWidth - strokeWidth) / 2,
                                    topLeft.y - (currentStrokeWidth - strokeWidth) / 2
                                )
                            else topLeft,
                            size = if (isSelected)
                                Size(
                                    arcSize.width + (currentStrokeWidth - strokeWidth),
                                    arcSize.height + (currentStrokeWidth - strokeWidth)
                                )
                            else arcSize,
                            style = Stroke(width = currentStrokeWidth, cap = StrokeCap.Butt)
                        )
                        startAngle += sweepAngle
                    }
                }

                // CENTER TEXT (The "Donut Hole" Content)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val displayLabel = selectedCategory ?: "Total"
                    val displayAmount = selectedCategory?.let { categoryMap[it] } ?: totalAmount

                    Text(
                        text = displayLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("$%.2f", displayAmount),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // LEGEND
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                categoryMap.entries.forEachIndexed { index, entry ->
                    val color = colors[index % colors.size]
                    val percentage = (entry.value / totalAmount * 100).toInt()
                    val isSelected = selectedCategory == entry.key

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(12.dp),
                                shape = CircleShape,
                                color = if (selectedCategory == null || isSelected) color else color.copy(
                                    alpha = 0.3f
                                )
                            ) {}
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = entry.key,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedCategory == null || isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "$percentage%  ($${String.format("%.2f", entry.value)})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}