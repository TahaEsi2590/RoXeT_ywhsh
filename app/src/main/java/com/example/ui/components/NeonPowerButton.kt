package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VpnState
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun NeonPowerButton(
    vpnState: VpnState,
    onClick: () -> Unit,
    statusText: String,
    modifier: Modifier = Modifier
) {
    val isConnected = vpnState == VpnState.CONNECTED
    val isConnecting = vpnState == VpnState.CONNECTING || vpnState == VpnState.DISCONNECTING

    // Infinite animation for connecting / connected pulse
    val infiniteTransition = rememberInfiniteTransition(label = "neon_pulse")

    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnected) 1.25f else if (isConnecting) 1.18f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse1"
    )

    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = if (isConnected) 0.6f else if (isConnecting) 0.8f else 0.1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha1"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate"
    )

    val buttonScale by animateFloatAsState(
        targetValue = if (isConnected) 1.05f else 1.0f,
        animationSpec = spring(),
        label = "btnScale"
    )

    val glowColor = when (vpnState) {
        VpnState.CONNECTED -> Color(0xFF00E5FF)
        VpnState.CONNECTING, VpnState.DISCONNECTING -> NeonOrange
        VpnState.DISCONNECTED -> Color(0xFF38BDF8)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.size(220.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background pulsing aura rings
            if (isConnected || isConnecting) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(pulseScale1)
                        .clip(CircleShape)
                        .background(glowColor.copy(alpha = pulseAlpha1))
                )
            }

            // Radar dash ring canvas
            Canvas(modifier = Modifier.size(190.dp)) {
                if (isConnecting) {
                    drawCircle(
                        color = glowColor.copy(alpha = 0.6f),
                        style = Stroke(
                            width = 4.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                floatArrayOf(25f, 25f),
                                rotationAngle * 2
                            )
                        )
                    )
                } else {
                    drawCircle(
                        color = if (isConnected) glowColor.copy(alpha = 0.6f) else DarkCard.copy(alpha = 0.5f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            // Main Clickable Circle Button
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(buttonScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = if (isConnected) {
                                listOf(Color(0xFF00E5FF).copy(alpha = 0.35f), Color(0xFF082235), DarkCard)
                            } else if (isConnecting) {
                                listOf(NeonOrange.copy(alpha = 0.25f), DarkSurface, DarkCard)
                            } else {
                                listOf(DarkCard, DarkSurface)
                            }
                        )
                    )
                    .border(
                        width = if (isConnected) 3.dp else 2.dp,
                        brush = Brush.linearGradient(
                            colors = if (isConnected) {
                                listOf(Color(0xFF00E5FF), Color(0xFF38BDF8))
                            } else if (isConnecting) {
                                listOf(NeonOrange, Color(0xFFFFD54F))
                            } else {
                                listOf(Color(0xFF38BDF8).copy(alpha = 0.8f), DarkCard.copy(alpha = 0.3f))
                            }
                        ),
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, radius = 70.dp, color = glowColor)
                    ) {
                        onClick()
                    }
                    .testTag("vpn_connect_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.Check else if (isConnecting) Icons.Default.Bolt else Icons.Default.PowerSettingsNew,
                        contentDescription = statusText,
                        tint = glowColor,
                        modifier = Modifier.size(if (isConnected) 54.dp else 48.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Status Text Label
        Text(
            text = statusText,
            color = if (isConnected) Color(0xFF38BDF8) else if (isConnecting) NeonOrange else TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
