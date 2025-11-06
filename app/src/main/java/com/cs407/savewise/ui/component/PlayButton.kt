package com.cs407.savewise.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
    totalDuration: Int = 5000,
    onStart: () -> Unit = {},
    onStop: () -> Unit = {}
) {
    var isRecording by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    // 动态测量宽度
    var boxWidthPx by remember { mutableStateOf(0f) }
    val density = LocalDensity.current

    // 动画旋转
    val iconRotation by animateFloatAsState(
        targetValue = if (isRecording) 180f else 0f,
        animationSpec = tween(durationMillis = 400)
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
            }
        } else progress = 0f
    }

    // 主布局：fillMaxWidth()
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
                    modifier = Modifier.size(80.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start Recording",
                    modifier = Modifier.size(80.dp)
                )
            }
        }
    }
}