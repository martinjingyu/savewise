package com.cs407.savewise.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun AnimatedRecordButton(
    totalDuration: Int = 50000,
    onStart: () -> Unit = {},
    onStop: () -> Unit = {},
    onFinished: () -> Unit = {},
    externalStopSignal: Int = 0
) {
    var isRecording by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    // NEW: react to external auto-stop
    LaunchedEffect(externalStopSignal) {
        if (externalStopSignal > 0 && isRecording) {
            isRecording = false
            onStop()
            onFinished()
        }
    }

    // 动态测量宽度
    var boxWidthPx by remember { mutableStateOf(0f) }
    val density = LocalDensity.current

    // 1. 定义无限循环动画，用于涟漪效果
    val infiniteTransition = rememberInfiniteTransition(label = "ripple")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    // 动画旋转
    val iconRotation by animateFloatAsState(
        targetValue = if (isRecording) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "rotation"
    )

    // 倒计时动画逻辑
    LaunchedEffect(isRecording) {
        if (isRecording) {
            val totalTime = totalDuration
            val frameTime = 16L
            var elapsed = 0L
            progress = 0f
            while (elapsed < totalTime && isRecording) {
                delay(frameTime)
                elapsed += frameTime
                progress = elapsed / totalTime.toFloat()
            }
            if (isRecording) {
                isRecording = false
                onStop()
                onFinished()
            }
        } else progress = 0f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .aspectRatio(1f)
            .onGloballyPositioned { layout ->
                boxWidthPx = layout.size.width.toFloat()
            },
        contentAlignment = Alignment.Center
    ) {
        val buttonSizePx = boxWidthPx / 1.5f
        val buttonRadius = buttonSizePx / 2f

        if (isRecording && boxWidthPx > 0f) {
            Canvas(modifier = Modifier.matchParentSize()) {
                // 绘制 3 个波纹，通过相位偏移 (offset) 错开
                val ripples = listOf(0f, 0.33f, 0.66f)

                ripples.forEach { offset ->
                    val currentPhase = (phase + offset) % 1f

                    // 半径从 按钮半径(1.0x) 扩散到 容器边缘(大约 1.5x)
                    val rippleRadius = buttonRadius + (buttonRadius * 0.6f * currentPhase)

                    // 透明度从 0.5 变到 0
                    val rippleAlpha = (0.5f * (1f - currentPhase)).coerceIn(0f, 1f)

                    drawCircle(
                        color = Color(0xFF4F8CF9).copy(alpha = rippleAlpha),
                        radius = rippleRadius,
                        center = center
                    )
                }
            }
        }

        Canvas(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    scaleX = 0.85f
                    scaleY = 0.85f
                }
        ) {
            if (isRecording && boxWidthPx > 0f) {

                val radius = boxWidthPx / 2f
                drawArc(
                    color = Color(0xFF4CAF50),
                    startAngle = -90f,
                    sweepAngle = 360 * progress,
                    useCenter = false,
                    style = Stroke(width = radius * 0.08f)
                )
            }
        }

        Button(
            onClick = {
                if (!isRecording) {
                    isRecording = true
                    onStart()
                } else {
                    isRecording = false
                    onStop()
                    onFinished()
                }
            },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRecording) Color(0xFF4F8CF9) else Color(0xFF6BA4FF),
                contentColor = Color.White
            ),
            modifier = Modifier
                .size(with(density) { (boxWidthPx / 1.5f).toDp() })
                .graphicsLayer { rotationY = iconRotation }
        ) {
            if (isRecording) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop Recording",
                    modifier = Modifier.size(56.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Start Recording",
                    modifier = Modifier.size(56.dp)
                )
            }
        }
    }
}