package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.R
import com.example.model.VpnState
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ConnectingGlobeView(
    vpnState: VpnState,
    serverName: String = "Auto Location",
    onCancel: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isConnected = vpnState == VpnState.CONNECTED
    val infiniteTransition = rememberInfiniteTransition(label = "connecting_anim")

    // Automatically transition back to main dashboard when connection finishes
    LaunchedEffect(isConnected) {
        if (isConnected) {
            delay(1200L)
            onCancel()
        }
    }

    // Slow globe rotation
    val globeRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "globe_rotation"
    )

    // Fast orbit 1
    val orbit1Angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit1"
    )

    // Fast orbit 2 (counter-clockwise)
    val orbit2Angle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit2"
    )

    // Pulse wave
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Progress bar step index animation
    val progressStep by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress_step"
    )

    val mainColor = if (isConnected) NeonEmerald else Color(0xFF38BDF8)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1015))
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("connecting_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Version bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "3.3.1",
                color = Color(0xFF6B7280),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Center Animated Globe with Title inside
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Box(
                modifier = Modifier.size(290.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer Glow Atmosphere
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    mainColor.copy(alpha = 0.25f),
                                    mainColor.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Back Half Orbit Rings
                Canvas(modifier = Modifier.size(290.dp)) {
                    drawConnectingOrbit(
                        center = center,
                        radiusX = 130.dp.toPx(),
                        radiusY = 46.dp.toPx(),
                        tiltAngle = -26f,
                        orbitAngle = orbit1Angle,
                        color = mainColor,
                        isFront = false
                    )
                    drawConnectingOrbit(
                        center = center,
                        radiusX = 120.dp.toPx(),
                        radiusY = 42.dp.toPx(),
                        tiltAngle = 38f,
                        orbitAngle = orbit2Angle,
                        color = Color(0xFF818CF8),
                        isFront = false
                    )
                }

                // Holographic Particle Earth Globe Sphere
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                listOf(mainColor, mainColor.copy(alpha = 0.4f), Color(0xFF3B82F6))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.earth_globe_cyber),
                        contentDescription = "3D Particle Globe",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(globeRotation)
                    )

                    // Cyber Vignette Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF0F1015).copy(alpha = 0.45f),
                                        Color(0xFF0F1015).copy(alpha = 0.75f)
                                    )
                                )
                            )
                    )

                    // Glowing ReNo VPN Title or Blue Checkmark in Center of Globe
                    if (isConnected) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E5FF).copy(alpha = 0.2f))
                                    .border(2.dp, Color(0xFF00E5FF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Connected",
                                    tint = Color(0xFF00F0FF),
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ReNo VPN",
                                color = Color(0xFF38BDF8),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    } else {
                        Text(
                            text = "ReNo VPN",
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            letterSpacing = 0.5.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Front Half Orbit Rings with Satellites & Particles
                Canvas(modifier = Modifier.size(290.dp)) {
                    drawConnectingOrbit(
                        center = center,
                        radiusX = 130.dp.toPx(),
                        radiusY = 46.dp.toPx(),
                        tiltAngle = -26f,
                        orbitAngle = orbit1Angle,
                        color = mainColor,
                        isFront = true
                    )
                    drawConnectingOrbit(
                        center = center,
                        radiusX = 120.dp.toPx(),
                        radiusY = 42.dp.toPx(),
                        tiltAngle = 38f,
                        orbitAngle = orbit2Angle,
                        color = Color(0xFF818CF8),
                        isFront = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Status: "متصل شد" or "در حال اتصال" in Bright Cyan Blue
            Text(
                text = if (isConnected) "متصل شد ✓" else "در حال اتصال",
                color = if (isConnected) Color(0xFF00E5FF) else Color(0xFF38BDF8),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // "Auto Location" + Blue ReNo Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = serverName,
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(10.dp))
                Image(
                    painter = painterResource(id = R.drawable.reno_symbol),
                    contentDescription = "ReNo Logo",
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle: "ارتباطات امن" (Secure Connections)
            Text(
                text = "ارتباطات امن",
                color = Color(0xFF9CA3AF),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Animated Horizontal Progress Indicator Dots/Pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentStep = progressStep.toInt() % 5
                for (i in 0..4) {
                    val isActive = i == currentStep || isConnected
                    Box(
                        modifier = Modifier
                            .height(4.dp)
                            .width(if (isActive) 24.dp else 12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (isActive) mainColor else Color(0xFF232634)
                            )
                    )
                }
            }
        }

        // Bottom Outline Capsule: "⌛ هنگام اتصال در این صفحه بمانید"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF161821))
                .border(1.dp, Color(0xFFD97706).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                .clickable { onCancel() }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "⌛ هنگام اتصال در این صفحه بمانید",
                    color = Color(0xFFFBBF24),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawConnectingOrbit(
    center: Offset,
    radiusX: Float,
    radiusY: Float,
    tiltAngle: Float,
    orbitAngle: Float,
    color: Color,
    isFront: Boolean
) {
    rotate(tiltAngle, pivot = center) {
        // Draw ellipse ring track
        drawOval(
            color = color.copy(alpha = if (isFront) 0.55f else 0.2f),
            topLeft = Offset(center.x - radiusX, center.y - radiusY),
            size = androidx.compose.ui.geometry.Size(radiusX * 2, radiusY * 2),
            style = Stroke(width = if (isFront) 2.dp.toPx() else 1.2.dp.toPx())
        )

        // Calculate satellite position
        val rad = (orbitAngle * PI / 180.0).toFloat()
        val satX = center.x + radiusX * cos(rad)
        val satY = center.y + radiusY * sin(rad)
        val inFront = sin(rad) >= 0

        if (inFront == isFront) {
            // Satellite glow halo
            drawCircle(
                color = color.copy(alpha = 0.4f),
                radius = 8.dp.toPx(),
                center = Offset(satX, satY)
            )
            // Satellite core dot
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = Offset(satX, satY)
            )
        }
    }
}
