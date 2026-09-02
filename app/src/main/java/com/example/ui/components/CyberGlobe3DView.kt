package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.VpnState
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * A 3D Cyber Holographic Earth Globe with orbiting cyber satellites,
 * glowing particle tracks, and interactive power trigger.
 */
@Composable
fun CyberGlobe3DView(
    vpnState: VpnState,
    statusText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = vpnState == VpnState.CONNECTED
    val isConnecting = vpnState == VpnState.CONNECTING || vpnState == VpnState.DISCONNECTING

    val primaryColor = when (vpnState) {
        VpnState.CONNECTED -> NeonEmerald
        VpnState.CONNECTING, VpnState.DISCONNECTING -> NeonOrange
        VpnState.DISCONNECTED -> NeonCyan
    }

    val infiniteTransition = rememberInfiniteTransition(label = "globe_orbit_transition")

    // Slow rotation for the globe
    val globeRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isConnected || isConnecting) 16000 else 32000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "globe_rotation"
    )

    // Faster rotation for Orbit 1 (Satellites & particles)
    val orbit1Angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isConnected) 4000 else if (isConnecting) 2500 else 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit1_angle"
    )

    // Counter-rotation for Orbit 2
    val orbit2Angle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isConnected) 5500 else if (isConnecting) 3000 else 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit2_angle"
    )

    // Pulse scale for energy wave
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isConnected) 1.22f else if (isConnecting) 1.35f else 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isConnecting) 1000 else 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (isConnected || isConnecting) 0.7f else 0.2f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isConnecting) 1000 else 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    val buttonScale by animateFloatAsState(
        targetValue = if (isConnected) 1.04f else 1.0f,
        animationSpec = tween(300),
        label = "button_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.size(270.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background atmospheric halo
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = pulseAlpha),
                                primaryColor.copy(alpha = pulseAlpha * 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Canvas for Back Half of 3D Orbiting Rings & Satellites (Depth layering)
            Canvas(modifier = Modifier.size(270.dp)) {
                drawOrbitRing(
                    center = center,
                    radiusX = 125.dp.toPx(),
                    radiusY = 45.dp.toPx(),
                    tiltAngle = -28f,
                    orbitAngle = orbit1Angle,
                    color = primaryColor,
                    isFrontLayer = false
                )

                drawOrbitRing(
                    center = center,
                    radiusX = 115.dp.toPx(),
                    radiusY = 40.dp.toPx(),
                    tiltAngle = 42f,
                    orbitAngle = orbit2Angle,
                    color = NeonPurple,
                    isFrontLayer = false
                )
            }

            // 3D Earth Globe Sphere
            Box(
                modifier = Modifier
                    .size(175.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            listOf(primaryColor, primaryColor.copy(alpha = 0.3f), NeonCyan)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Earth Globe Image with slow rotation
                Image(
                    painter = painterResource(id = R.drawable.earth_globe_cyber),
                    contentDescription = "3D Cyber Earth Globe",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(globeRotation)
                )

                // Cyber Grid Overlay & Atmosphere Vignette
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    primaryColor.copy(alpha = if (isConnected) 0.25f else 0.15f),
                                    DarkBackground.copy(alpha = 0.55f)
                                )
                            )
                        )
                )

                // Center Power Control Capsule
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(buttonScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = if (isConnected) {
                                    listOf(NeonEmerald.copy(alpha = 0.4f), DarkSurface.copy(alpha = 0.95f))
                                } else if (isConnecting) {
                                    listOf(NeonOrange.copy(alpha = 0.45f), DarkSurface.copy(alpha = 0.95f))
                                } else {
                                    listOf(DarkCard.copy(alpha = 0.9f), DarkSurface.copy(alpha = 0.95f))
                                }
                            )
                        )
                        .border(
                            width = 2.dp,
                            color = primaryColor,
                            shape = CircleShape
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, radius = 40.dp, color = primaryColor)
                        ) {
                            onClick()
                        }
                        .testTag("vpn_connect_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.Shield else if (isConnecting) Icons.Default.Bolt else Icons.Default.PowerSettingsNew,
                        contentDescription = statusText,
                        tint = primaryColor,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            // Canvas for Front Half of 3D Orbiting Rings, Satellites & Glowing Particles
            Canvas(modifier = Modifier.size(270.dp)) {
                drawOrbitRing(
                    center = center,
                    radiusX = 125.dp.toPx(),
                    radiusY = 45.dp.toPx(),
                    tiltAngle = -28f,
                    orbitAngle = orbit1Angle,
                    color = primaryColor,
                    isFrontLayer = true
                )

                drawOrbitRing(
                    center = center,
                    radiusX = 115.dp.toPx(),
                    radiusY = 40.dp.toPx(),
                    tiltAngle = 42f,
                    orbitAngle = orbit2Angle,
                    color = NeonPurple,
                    isFrontLayer = true
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Connection Status Label
        Text(
            text = statusText,
            color = primaryColor,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

/**
 * Draws a 3D tilted orbit ring and animated cyber satellite + particle trail.
 */
private fun DrawScope.drawOrbitRing(
    center: Offset,
    radiusX: Float,
    radiusY: Float,
    tiltAngle: Float,
    orbitAngle: Float,
    color: Color,
    isFrontLayer: Boolean
) {
    rotate(degrees = tiltAngle, pivot = center) {
        val rad = Math.toRadians(orbitAngle.toDouble())
        val satelliteY = center.y + (radiusY * sin(rad)).toFloat()
        val satelliteX = center.x + (radiusX * cos(rad)).toFloat()
        val isInFront = sin(rad) >= 0

        // Draw Elliptical Orbit Path Track
        val path = Path()
        val steps = 60
        for (i in 0..steps) {
            val stepRad = 2 * PI * (i.toDouble() / steps)
            val yInOrbit = sin(stepRad)
            val px = center.x + (radiusX * cos(stepRad)).toFloat()
            val py = center.y + (radiusY * yInOrbit).toFloat()

            // Filter between front and back layer for 3D depth
            val shouldDraw = if (isFrontLayer) yInOrbit >= -0.05 else yInOrbit <= 0.05
            if (shouldDraw) {
                if (i == 0 || (isFrontLayer && yInOrbit < 0) || (!isFrontLayer && yInOrbit > 0)) {
                    path.moveTo(px, py)
                } else {
                    path.lineTo(px, py)
                }
            }
        }

        drawPath(
            path = path,
            color = if (isFrontLayer) color.copy(alpha = 0.5f) else color.copy(alpha = 0.2f),
            style = Stroke(
                width = if (isFrontLayer) 2.dp.toPx() else 1.2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
            )
        )

        // Draw Satellite and Trailing Particles
        if (isFrontLayer == isInFront) {
            // Satellite Glow Aura
            drawCircle(
                color = color.copy(alpha = if (isFrontLayer) 0.6f else 0.3f),
                radius = if (isFrontLayer) 9.dp.toPx() else 6.dp.toPx(),
                center = Offset(satelliteX, satelliteY)
            )

            // Satellite Core Sphere
            drawCircle(
                color = Color.White,
                radius = if (isFrontLayer) 4.5.dp.toPx() else 3.dp.toPx(),
                center = Offset(satelliteX, satelliteY)
            )

            // Trailing Particle 1
            val trailRad1 = rad - 0.25
            val trailX1 = center.x + (radiusX * cos(trailRad1)).toFloat()
            val trailY1 = center.y + (radiusY * sin(trailRad1)).toFloat()
            drawCircle(
                color = color.copy(alpha = 0.7f),
                radius = 3.dp.toPx(),
                center = Offset(trailX1, trailY1)
            )

            // Trailing Particle 2
            val trailRad2 = rad - 0.5
            val trailX2 = center.x + (radiusX * cos(trailRad2)).toFloat()
            val trailY2 = center.y + (radiusY * sin(trailRad2)).toFloat()
            drawCircle(
                color = color.copy(alpha = 0.4f),
                radius = 2.dp.toPx(),
                center = Offset(trailX2, trailY2)
            )
        }
    }
}
