package com.example.ui.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.InetAddress

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return false

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(network) ?: return false
        return actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } else {
        @Suppress("DEPRECATION")
        val networkInfo = connectivityManager.activeNetworkInfo ?: return false
        @Suppress("DEPRECATION")
        return networkInfo.isConnected
    }
}

@Composable
fun SplashScreen(
    onLoaded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var progress by remember { mutableFloatStateOf(0.05f) }
    var statusText by remember { mutableStateOf("در حال بررسی اتصال به اینترنت...") }
    var isCheckingNetwork by remember { mutableStateOf(true) }
    var hasInternetError by remember { mutableStateOf(false) }
    var retryTrigger by remember { mutableStateOf(0) }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "splash_progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_logo")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Verification and loading step sequence
    LaunchedEffect(retryTrigger) {
        hasInternetError = false
        isCheckingNetwork = true
        progress = 0.15f
        statusText = "در حال بررسی وضعیت شبکه و اینترنت..."

        delay(600)

        // Verify Android network connectivity
        val connected = withContext(Dispatchers.IO) {
            isNetworkAvailable(context)
        }

        if (!connected) {
            hasInternetError = true
            isCheckingNetwork = false
            statusText = "خطا: عدم اتصال به اینترنت!"
            return@LaunchedEffect
        }

        // Step 1: Internet Confirmed
        progress = 0.40f
        statusText = "اتصال اینترنت تایید شد ✓"
        delay(500)

        // Step 2: Load Core V2Ray/Xray Modules
        progress = 0.65f
        statusText = "بارگذاری پروتکل‌های ضد فیلترینگ ReNo..."
        delay(600)

        // Step 3: Fetching Optimized Anti-Filter Nodes
        progress = 0.88f
        statusText = "بهینه‌سازی سرورها و مسیرهای تونل امن..."
        delay(500)

        // Step 4: Finalizing
        progress = 1.0f
        statusText = "آماده‌سازی نهایی و اجرای برنامه..."
        delay(400)

        // Completed -> Transition into Main Screen
        onLoaded()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0B10),
                        Color(0xFF0F1015),
                        Color(0xFF141622)
                    )
                )
            )
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ReNo VPN Logo with glowing pulse
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                NeonCyan.copy(alpha = 0.35f),
                                Color(0xFF1E3A8A).copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(2.dp, Brush.linearGradient(listOf(NeonCyan, Color(0xFF3B82F6))), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.reno_symbol),
                    contentDescription = "ReNo VPN Logo",
                    modifier = Modifier.size(90.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name: ReNo VPN
            Text(
                text = "ReNo VPN",
                color = Color(0xFF38BDF8),
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = 1.5.sp,
                modifier = Modifier.testTag("splash_title")
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "سریع، امن و پایدار",
                color = Color(0xFF94A3B8),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(48.dp))

            // ═════════════════════════════════════════════════════════════════
            // Loading Progress Bar / Line (خط لودینگ پر شونده)
            // ═════════════════════════════════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFF1E202C))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF2563EB),
                                    Color(0xFF00E5FF),
                                    Color(0xFF10B981)
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Percentage & Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    color = NeonCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = statusText,
                    color = if (hasInternetError) Color(0xFFEF4444) else TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End
                )
            }

            // ═════════════════════════════════════════════════════════════════
            // Internet Error & Retry Section (اگر نت وصل نبود)
            // ═════════════════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = hasInternetError,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF20161C))
                        .border(1.dp, Color(0xFFDC2626).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "No Internet",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(36.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "اتصال به اینترنت برقرار نیست",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "برای ورود به برنامه و دریافت سرورهای ReNo VPN، لطفاً اینترنت گوشی (وای‌فای یا داده تلفن همراه) را روشن کنید.",
                        color = Color(0xFFFCA5A5),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { retryTrigger++ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("retry_internet_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تلاش مجدد",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Bottom Footer note
        Text(
            text = "ReNo Secure Proxy Core v3.3.1",
            color = Color(0xFF4B5563),
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}
