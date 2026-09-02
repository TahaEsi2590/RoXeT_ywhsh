package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DownloadGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UploadBlue
import com.example.viewmodel.SpeedChartPoint

@Composable
fun SpeedMeterPanel(
    downloadBps: Long,
    uploadBps: Long,
    totalDownBytes: Long,
    totalUpBytes: Long,
    downloadLabel: String,
    uploadLabel: String,
    totalDownLabel: String,
    totalUpLabel: String,
    speedHistory: List<SpeedChartPoint>,
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Download Card
        SpeedCard(
            title = downloadLabel,
            speedBps = downloadBps,
            totalBytes = totalDownBytes,
            totalLabel = totalDownLabel,
            isDownload = true,
            color = DownloadGreen,
            speedHistory = speedHistory.map { it.downloadMbps },
            isConnected = isConnected,
            modifier = Modifier
                .weight(1f)
                .testTag("download_speed_card")
        )

        // Upload Card
        SpeedCard(
            title = uploadLabel,
            speedBps = uploadBps,
            totalBytes = totalUpBytes,
            totalLabel = totalUpLabel,
            isDownload = false,
            color = UploadBlue,
            speedHistory = speedHistory.map { it.uploadMbps },
            isConnected = isConnected,
            modifier = Modifier
                .weight(1f)
                .testTag("upload_speed_card")
        )
    }
}

@Composable
private fun SpeedCard(
    title: String,
    speedBps: Long,
    totalBytes: Long,
    totalLabel: String,
    isDownload: Boolean,
    color: Color,
    speedHistory: List<Float>,
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "speed_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val formattedSpeed = formatSpeed(speedBps, isConnected)
    val formattedTotal = formatBytes(totalBytes)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(DarkCard)
            .border(
                width = 1.dp,
                color = if (isConnected) color.copy(alpha = 0.4f) else DarkCardBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(14.dp)
    ) {
        Column {
            // Header Row: Icon + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDownload) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = title,
                            tint = color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Live status dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isConnected) color.copy(alpha = pulseAlpha) else TextMuted
                        )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Speed Value
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = formattedSpeed.first,
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formattedSpeed.second,
                    color = color,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mini Wave Canvas Chart
            MiniWaveChart(
                points = speedHistory,
                lineColor = color,
                isConnected = isConnected,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Total Traffic Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = totalLabel,
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Text(
                    text = formattedTotal,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MiniWaveChart(
    points: List<Float>,
    lineColor: Color,
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        if (points.isEmpty() || !isConnected) {
            // Flat baseline
            drawLine(
                color = DarkCardBorder,
                start = Offset(0f, height * 0.8f),
                end = Offset(width, height * 0.8f),
                strokeWidth = 2f
            )
            return@Canvas
        }

        val maxVal = (points.maxOrNull() ?: 1f).coerceAtLeast(1f)
        val stepX = width / (points.size - 1).coerceAtLeast(1)

        val path = Path()
        val fillPath = Path()

        points.forEachIndexed { index, value ->
            val x = index * stepX
            val normalizedY = (1f - (value / maxVal).coerceIn(0f, 1f)) * (height * 0.75f) + (height * 0.15f)

            if (index == 0) {
                path.moveTo(x, normalizedY)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, normalizedY)
            } else {
                val prevX = (index - 1) * stepX
                val prevY = (1f - (points[index - 1] / maxVal).coerceIn(0f, 1f)) * (height * 0.75f) + (height * 0.15f)
                val cX = (prevX + x) / 2f
                path.cubicTo(cX, prevY, cX, normalizedY, x, normalizedY)
                fillPath.cubicTo(cX, prevY, cX, normalizedY, x, normalizedY)
            }

            if (index == points.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }

        // Draw gradient fill below wave
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.25f),
                    lineColor.copy(alpha = 0.0f)
                ),
                startY = 0f,
                endY = height
            )
        )

        // Draw glowing wave line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

private fun formatSpeed(bytesPerSec: Long, isConnected: Boolean): Pair<String, String> {
    if (!isConnected || bytesPerSec <= 0) {
        return "0.0" to "KB/s"
    }
    val kbps = bytesPerSec / 1024.0
    return if (kbps >= 1000.0) {
        val mbps = kbps / 1024.0
        String.format(java.util.Locale.US, "%.1f", mbps) to "MB/s"
    } else {
        String.format(java.util.Locale.US, "%.1f", kbps) to "KB/s"
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 MB"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format(java.util.Locale.US, "%.2f GB", gb)
        mb >= 1.0 -> String.format(java.util.Locale.US, "%.1f MB", mb)
        else -> String.format(java.util.Locale.US, "%.0f KB", kb)
    }
}
