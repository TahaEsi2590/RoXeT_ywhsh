package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.ServerTrafficLoad
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

/**
 * Visual Antenna Signal Indicator displaying antenna mast and dynamic signal bars:
 * - Green (4 bars) = Low traffic / Optimal
 * - Yellow (2 bars) = Medium traffic
 * - Red (1 bar) = High traffic / Overcrowded
 */
@Composable
fun AntennaSignalIndicator(
    load: ServerTrafficLoad,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false,
    showUsers: Boolean = false,
    onlineUsers: Int = 120,
    language: AppLanguage = AppLanguage.FA,
    size: Dp = 24.dp
) {
    val activeColor = when (load) {
        ServerTrafficLoad.LOW -> NeonEmerald
        ServerTrafficLoad.MEDIUM -> Color(0xFFFFC107) // Amber/Yellow
        ServerTrafficLoad.HIGH -> Color(0xFFFF4444) // Bright Red
    }

    val activeBars = when (load) {
        ServerTrafficLoad.LOW -> 4
        ServerTrafficLoad.MEDIUM -> 2
        ServerTrafficLoad.HIGH -> 1
    }

    val infiniteTransition = rememberInfiniteTransition(label = "antenna_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "antenna_alpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        // Antenna Drawing Canvas (Mast + Bars)
        Box(
            modifier = Modifier
                .size(width = size, height = size)
                .clip(RoundedCornerShape(6.dp))
                .background(activeColor.copy(alpha = 0.12f))
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(width = size - 6.dp, height = size - 6.dp)) {
                val w = this.size.width
                val h = this.size.height

                // Draw Antenna Mast on the left
                val mastX = w * 0.18f
                drawLine(
                    color = activeColor.copy(alpha = pulseAlpha),
                    start = Offset(mastX, h),
                    end = Offset(mastX, h * 0.15f),
                    strokeWidth = 2.dp.toPx()
                )
                // Antenna Top Disc / Transmitter
                drawCircle(
                    color = activeColor,
                    radius = 2.5.dp.toPx(),
                    center = Offset(mastX, h * 0.15f)
                )

                // Draw 4 Signal Bars
                val barCount = 4
                val barWidth = (w * 0.65f) / (barCount * 1.5f)
                val startX = w * 0.38f

                for (i in 0 until barCount) {
                    val isBarActive = i < activeBars
                    val barHeightFraction = (i + 1) / barCount.toFloat()
                    val barH = h * 0.85f * barHeightFraction
                    val bx = startX + i * (barWidth * 1.5f)
                    val by = h - barH

                    val color = if (isBarActive) {
                        activeColor.copy(alpha = if (i == activeBars - 1) pulseAlpha else 1f)
                    } else {
                        DarkCardBorder.copy(alpha = 0.4f)
                    }

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(bx, by),
                        size = Size(barWidth, barH),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }
        }

        if (showLabel || showUsers) {
            Column {
                if (showLabel) {
                    val label = when (load) {
                        ServerTrafficLoad.LOW -> if (language == AppLanguage.FA) "خلوت (سبز)" else "Low Traffic (Green)"
                        ServerTrafficLoad.MEDIUM -> if (language == AppLanguage.FA) "متوسط (زرد)" else "Moderate (Yellow)"
                        ServerTrafficLoad.HIGH -> if (language == AppLanguage.FA) "شلوغ (قرمز)" else "High Load (Red)"
                    }
                    Text(
                        text = label,
                        color = activeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (showUsers) {
                    val userText = if (language == AppLanguage.FA) "$onlineUsers کاربر آنلاین" else "$onlineUsers online"
                    Text(
                        text = userText,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
