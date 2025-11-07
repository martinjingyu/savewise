package com.cs407.savewise.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(16.dp)
                .background(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFEEF2F7), Color(0xFFDDE3EB))
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("No expenses to display")
        }
        return
    }

    // ✅ 分类汇总
    val grouped = expenses.groupBy { it.category }
    val total = expenses.sumOf { it.amount }.toFloat()
    val categorySums = grouped.mapValues { (_, list) -> list.sumOf { it.amount }.toFloat() }

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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFEEF2F7), Color(0xFFDDE3EB))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)

                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val center = Offset((size.width / 2).toFloat(), (size.height / 2).toFloat())
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        val distance = sqrt(dx * dx + dy * dy)
                        val angle = (atan2(dy, dx) * 180f / Math.PI + 360f) % 360f

                        val minDim = min(size.width, size.height)
                        if (distance > minDim / 3f) return@detectTapGestures
                        var startAngle = -90f
                        categorySums.forEach { (category, sum) ->
                            val sweep = (sum / total) * 360f
                            if (angle >= startAngle && angle < startAngle + sweep) {
                                selectedCategory =
                                    if (selectedCategory == category) null else category
                                return@detectTapGestures
                            }
                            startAngle += sweep
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2.5f
                var startAngle = -90f
                var colorIndex = 0

                categorySums.forEach { (category, sum) ->
                    val sweepAngle = (sum / total) * 360f
                    val color = colors[colorIndex % colors.size]

                    val angleRad = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
                    val offsetX = if (category == selectedCategory) cos(angleRad).toFloat() * animatedLift else 0f
                    val offsetY = if (category == selectedCategory) sin(angleRad).toFloat() * animatedLift else 0f

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(
                            (size.width - 2 * radius) / 2 + offsetX,
                            (size.height - 2 * radius) / 2 + offsetY
                        ),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                    )

                    startAngle += sweepAngle
                    colorIndex++
                }
            }

            selectedCategory?.let { cat ->
                val value = categorySums[cat] ?: 0f
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = cat,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF37474F)
                    )
                    Text(
                        text = "$${"%.2f".format(value)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF37474F)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            categorySums.entries.forEachIndexed { index, (category, sum) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(colors[index % colors.size], RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$category: $${"%.2f".format(sum)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}