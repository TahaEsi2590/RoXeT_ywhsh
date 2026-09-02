package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.VpnState
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonOrange

@Composable
fun GiantPowerSwitch(
    vpnState: VpnState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = vpnState == VpnState.CONNECTED
    val isConnecting = vpnState == VpnState.CONNECTING || vpnState == VpnState.DISCONNECTING
    val isActive = isConnected || isConnecting

    // Slider knob offset (Left: 6dp -> Right: 86dp)
    val knobOffset by animateDpAsState(
        targetValue = if (isActive) 86.dp else 6.dp,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "switch_knob_offset"
    )

    // Bright Blue / Cyan glow when connected
    val activeGlowColor = when (vpnState) {
        VpnState.CONNECTED -> Color(0xFF00E5FF)
        VpnState.CONNECTING, VpnState.DISCONNECTING -> NeonOrange
        VpnState.DISCONNECTED -> Color(0xFF3B82F6)
    }

    val switchTrackBg by animateColorAsState(
        targetValue = if (isConnected) {
            Color(0xFF082235)
        } else if (isConnecting) {
            Color(0xFF2C1E16)
        } else {
            Color(0xFF17181F)
        },
        animationSpec = tween(300),
        label = "track_bg"
    )

    val knobBgColor by animateColorAsState(
        targetValue = if (isConnected) {
            Color(0xFF0284C7)
        } else if (isConnecting) {
            Color(0xFFF59E0B)
        } else {
            Color(0xFF2A2C39)
        },
        animationSpec = tween(300),
        label = "knob_bg"
    )

    val iconTint by animateColorAsState(
        targetValue = if (isConnected) Color(0xFF00F0FF) else if (isConnecting) Color.White else Color(0xFF8E95A5),
        animationSpec = tween(300),
        label = "icon_tint"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "power_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier
            .width(160.dp)
            .height(78.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(switchTrackBg)
            .border(
                width = 1.8.dp,
                color = if (isActive) activeGlowColor.copy(alpha = 0.8f) else Color(0xFF2D303E),
                shape = RoundedCornerShape(40.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = 80.dp, color = activeGlowColor)
            ) {
                onClick()
            }
            .testTag("giant_power_switch"),
        contentAlignment = Alignment.CenterStart
    ) {
        // Glowing halo if active
        if (isActive) {
            Box(
                modifier = Modifier
                    .offset(x = knobOffset)
                    .size(66.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                activeGlowColor.copy(alpha = 0.55f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Circular Slider Knob
        Box(
            modifier = Modifier
                .offset(x = knobOffset)
                .size(66.dp)
                .shadow(elevation = if (isActive) 8.dp else 2.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(knobBgColor)
                .border(
                    width = 2.dp,
                    color = if (isConnected) Color(0xFF00E5FF) else if (isActive) Color.White.copy(alpha = 0.6f) else Color(0xFF3D4155),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isConnected) Icons.Default.Check else Icons.Default.PowerSettingsNew,
                contentDescription = if (isConnected) "Connected Check" else "Power Switch",
                tint = iconTint,
                modifier = Modifier.size(if (isConnected) 36.dp else 34.dp)
            )
        }
    }
}
