package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import com.example.R
import com.example.model.AppLanguage
import com.example.model.PromoBannerItem
import com.example.model.ServerTrafficLoad
import com.example.model.VpnConfigItem
import com.example.model.VpnState
import com.example.ui.components.AntennaSignalIndicator
import com.example.ui.components.ConnectingGlobeView
import com.example.ui.components.CyberGlobe3DView
import com.example.ui.components.GiantPowerSwitch
import com.example.ui.components.GuestReminderDialog
import com.example.ui.components.ServerSelectionSheet
import com.example.ui.components.SpeedMeterPanel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonOrange
import kotlinx.coroutines.delay
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.VpnViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainVpnScreen(
    viewModel: VpnViewModel,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {}
) {
    val context = LocalContext.current
    val vpnState by viewModel.vpnState.collectAsState()
    val connectionStats by viewModel.connectionStats.collectAsState()
    val configs by viewModel.configs.collectAsState()
    val selectedConfig by viewModel.selectedConfig.collectAsState()
    val language by viewModel.language.collectAsState()
    val speedHistory by viewModel.speedHistory.collectAsState()
    val hideBannerAds by viewModel.hideBannerAds.collectAsState()
    val banners by viewModel.banners.collectAsState()

    val activeBanners = remember(banners) { banners.filter { it.isEnabled } }

    var showServerSheet by remember { mutableStateOf(false) }
    var showGuestReminder by remember { mutableStateOf(false) }
    var isGlobeViewActive by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    // VPN Permission Launcher
    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.toggleVpnConnection(context)
            isGlobeViewActive = true
        }
    }

    val isConnected = vpnState == VpnState.CONNECTED
    val isConnecting = vpnState == VpnState.CONNECTING || vpnState == VpnState.DISCONNECTING
    val activeServer = selectedConfig ?: configs.firstOrNull()
    val activeServerName = activeServer?.persianCountryName ?: "Auto Location"

    // ═════════════════════════════════════════════════════════════════
    // Auto Pull-Down Server Refresh Animation (Screenshot 1, 2, 3)
    // ═════════════════════════════════════════════════════════════════
    var pullRefreshState by remember { mutableStateOf(TopPullRefreshState.IDLE) }
    var refreshTriggerCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshTriggerCount) {
        // Step 1: Pulling down capsule (Screenshot 3)
        pullRefreshState = TopPullRefreshState.PULLING_DOWN
        delay(650L)

        // Step 2: Spinning circular refresh icon (Screenshot 2) & ping test
        pullRefreshState = TopPullRefreshState.REFRESHING
        viewModel.testAllPings()
        delay(1200L)

        // Step 3: Green checkmark badge (Screenshot 1)
        pullRefreshState = TopPullRefreshState.SUCCESS
        delay(1400L)

        // Step 4: Disappear smoothly
        pullRefreshState = TopPullRefreshState.IDLE
    }

    // If in Connecting state and user wants to see the 3D Holographic Earth view (Screenshot 6)
    if (isGlobeViewActive && (isConnecting || isConnected)) {
        ConnectingGlobeView(
            vpnState = vpnState,
            serverName = activeServerName,
            onCancel = {
                if (vpnState != VpnState.DISCONNECTED) {
                    viewModel.toggleVpnConnection(context)
                }
                isGlobeViewActive = false
            }
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkTheme) Color(0xFF0F1015) else Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(scrollState)
            .testTag("main_vpn_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // ═════════════════════════════════════════════════════════════════
            // Top Bar: Mail Icon | Split Tunnel Icon | Green Dot | 3.3.1 | Hamburger Menu (Screenshot 4)
            // ═════════════════════════════════════════════════════════════════
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Mail + Split Tunnel Action Icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showGuestReminder = true },
                        modifier = Modifier.size(38.dp).testTag("mail_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MailOutline,
                            contentDescription = "Messages / Support",
                            tint = Color(0xFFD1D5DB),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.size(38.dp).testTag("split_tunnel_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallSplit,
                            contentDescription = "Split Tunneling",
                            tint = Color(0xFFD1D5DB),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Center: Status Green Dot (Clickable to trigger server ping test & refresh animation)
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .clickable { refreshTriggerCount++ },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) NeonEmerald else Color(0xFF10B981))
                    )
                }

                // Right: App Version "3.3.1" + Hamburger Menu
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "3.3.1",
                        color = if (isDarkTheme) Color(0xFF6B7280) else Color(0xFF475569),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable { viewModel.onSecretLogoTap() }
                            .padding(end = 2.dp)
                    )

                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.size(38.dp).testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkTheme) "Light mode" else "Dark mode",
                            tint = if (isDarkTheme) Color(0xFFFFC107) else Color(0xFF334155),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.size(38.dp).testTag("hamburger_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color(0xFFD1D5DB),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // ═════════════════════════════════════════════════════════════════
            // Auto Pull-Down Server Refresh Animation Area (Screenshot 1, 2, 3)
            // ═════════════════════════════════════════════════════════════════
            AutoPullDownRefreshWidget(
                state = pullRefreshState,
                onClick = { refreshTriggerCount++ }
            )

            // ═════════════════════════════════════════════════════════════════
            // App Title: "ReNo VPN" (Screenshot 4 - Italic, Bright Neon Blue)
            // ═════════════════════════════════════════════════════════════════
            Text(
                text = "ReNo VPN",
                color = Color(0xFF38BDF8),
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .clickable { isGlobeViewActive = true }
                    .testTag("app_title_reno")
            )

            Spacer(modifier = Modifier.height(26.dp))

            // ═════════════════════════════════════════════════════════════════
            // Main Power Switch (Giant Capsule Switch - Screenshot 4)
            // ═════════════════════════════════════════════════════════════════
            GiantPowerSwitch(
                vpnState = vpnState,
                onClick = {
                    try {
                        if (vpnState == VpnState.DISCONNECTED) {
                            val prepareIntent = VpnService.prepare(context)
                            if (prepareIntent != null) {
                                vpnPermissionLauncher.launch(prepareIntent)
                            } else {
                                viewModel.toggleVpnConnection(context)
                                isGlobeViewActive = true
                            }
                        } else {
                            viewModel.toggleVpnConnection(context)
                        }
                    } catch (e: Exception) {
                        viewModel.showToast("خطا در راه‌اندازی: ${e.message}")
                    }
                }
            )

            // 3-Hour Session Timer Indicator
            if (isConnected) {
                Spacer(modifier = Modifier.height(10.dp))
                val elapsedSeconds = connectionStats.durationSeconds
                val maxSeconds = 3L * 3600L
                val remainingSeconds = (maxSeconds - elapsedSeconds).coerceAtLeast(0L)
                val hours = remainingSeconds / 3600
                val minutes = (remainingSeconds % 3600) / 60
                val seconds = remainingSeconds % 60
                val timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF161824))
                        .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "زمان باقی‌مانده از نشست ۳ ساعته: $timeFormatted",
                            color = Color(0xFF93C5FD),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ═════════════════════════════════════════════════════════════
                // Live Upload & Download Speeds & Traffic Meter (Directly under switch)
                // ═════════════════════════════════════════════════════════════
                SpeedMeterPanel(
                    downloadBps = connectionStats.downloadSpeedBps,
                    uploadBps = connectionStats.uploadSpeedBps,
                    totalDownBytes = connectionStats.totalDownloadedBytes,
                    totalUpBytes = connectionStats.totalUploadedBytes,
                    downloadLabel = viewModel.getString("download"),
                    uploadLabel = viewModel.getString("upload"),
                    totalDownLabel = viewModel.getString("total_download"),
                    totalUpLabel = viewModel.getString("total_upload"),
                    speedHistory = speedHistory,
                    isConnected = isConnected
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ═════════════════════════════════════════════════════════════════
            // Promo Banner Carousel (3-Second Auto Flip) - Hidden if user enabled hideBannerAds
            // ═════════════════════════════════════════════════════════════════
            if (!hideBannerAds && activeBanners.isNotEmpty()) {
                PromoBannersCarousel(
                    banners = activeBanners,
                    onBannerClick = { banner ->
                        val link = banner.buttonLink.ifBlank { "https://t.me/Reno_VpN_1" }
                        try {
                            val url = if (!link.startsWith("http://") &&
                                !link.startsWith("https://") &&
                                !link.startsWith("tg://")
                            ) {
                                "https://$link"
                            } else {
                                link
                            }
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            showGuestReminder = true
                        }
                    },
                    modifier = Modifier.testTag("promo_banner_carousel")
                )
            }
        }

        // ═════════════════════════════════════════════════════════════════════
        // Bottom Server Selector & VIP Ribbon (Screenshot 4)
        // ═════════════════════════════════════════════════════════════════
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Label: "انتخاب سرور" with Server Stack Icon on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "انتخاب سرور",
                    color = Color(0xFF9CA3AF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = "Server Icon",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Server Selector Capsule (Screenshot 4)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161722)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262838)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showServerSheet = true }
                    .testTag("server_selector_capsule")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Star icon & Group Users icon
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorites",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Online Users",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Center & Right: Server Name + Blue ReNo Icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = activeServerName,
                            color = TextPrimary,
                            fontSize = 15.sp,
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
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // VIP Gold Banner (Screenshot 4: [V Premium] برای زمان نامحدود به پریمیوم ارتقا دهید)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFD97706),
                                Color(0xFFF59E0B),
                                Color(0xFFB45309)
                            )
                        )
                    )
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/Reno_VpN_1"))
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            showGuestReminder = true
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "برای زمان نامحدود به پریمیوم ارتقا دهید",
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "V Premium",
                            color = Color(0xFFFBBF24),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }

    // ── Server Selection Sheet ──────────────────────────────────────────────
    if (showServerSheet) {
        ServerSelectionSheet(
            sheetState = sheetState,
            configs = configs,
            selectedConfig = selectedConfig,
            onSelectConfig = { config ->
                viewModel.selectConfig(config)
                showServerSheet = false
            },
            onToggleFavorite = { id ->
                viewModel.toggleFavorite(id)
            },
            onDismiss = { showServerSheet = false },
            language = language
        )
    }

    // ── Guest Reminder Dialog ───────────────────────────────────────────────
    if (showGuestReminder) {
        GuestReminderDialog(
            onDismiss = { showGuestReminder = false },
            onRegister = { showGuestReminder = false },
            onLogin = { showGuestReminder = false }
        )
    }
}

// ═════════════════════════════════════════════════════════════════════
// Promo Banners Auto-Carousel (3D Stacked Deck Shuffling & Fluid Flip)
// ═════════════════════════════════════════════════════════════════════
@Composable
fun PromoBannersCarousel(
    banners: List<PromoBannerItem>,
    onBannerClick: (PromoBannerItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (banners.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }
    var isTransitioning by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val cardDragX = remember { Animatable(0f) }

    fun flipNext() {
        if (isTransitioning || banners.size <= 1) return
        coroutineScope.launch {
            isTransitioning = true
            cardDragX.animateTo(
                targetValue = 520f,
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
            )
            currentIndex = (currentIndex + 1) % banners.size
            cardDragX.snapTo(0f)
            isTransitioning = false
        }
    }

    fun flipPrev() {
        if (isTransitioning || banners.size <= 1) return
        coroutineScope.launch {
            isTransitioning = true
            currentIndex = if (currentIndex - 1 < 0) banners.size - 1 else currentIndex - 1
            cardDragX.snapTo(-520f)
            cardDragX.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
            )
            isTransitioning = false
        }
    }

    // Auto flip every 3.8 seconds continuously without getting stuck
    LaunchedEffect(currentIndex, banners.size) {
        if (banners.size > 1) {
            delay(3800L)
            if (!isTransitioning) {
                flipNext()
            }
        }
    }

    val dragProgress = (cardDragX.value.absoluteValue / 520f).coerceIn(0f, 1f)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var totalDrag by remember { mutableFloatStateOf(0f) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(355.dp)
                .pointerInput(banners.size, isTransitioning) {
                    detectHorizontalDragGestures(
                        onDragStart = { totalDrag = 0f },
                        onDragEnd = {
                            if (totalDrag > 60f) {
                                flipNext()
                            } else if (totalDrag < -60f) {
                                flipNext()
                            } else {
                                coroutineScope.launch {
                                    cardDragX.animateTo(0f, spring(dampingRatio = 0.75f, stiffness = 400f))
                                }
                            }
                            totalDrag = 0f
                        },
                        onDragCancel = {
                            coroutineScope.launch { cardDragX.animateTo(0f, spring()) }
                            totalDrag = 0f
                        },
                        onHorizontalDrag = { _, dragDelta ->
                            if (!isTransitioning) {
                                totalDrag += dragDelta
                                coroutineScope.launch {
                                    cardDragX.snapTo(cardDragX.value + dragDelta)
                                }
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Render from back (position 2) to middle (position 1) to front (position 0)
            val count = banners.size.coerceAtMost(3)

            for (stackPos in (count - 1) downTo 0) {
                val bannerIndex = (currentIndex + stackPos) % banners.size
                val banner = banners[bannerIndex]

                val targetX: Float
                val targetY: Float
                val targetRotation: Float
                val targetScale: Float
                val targetAlpha: Float
                val zIndexVal: Float

                when (stackPos) {
                    2 -> {
                        // Deepest background card
                        targetX = -62f + (30f * dragProgress)
                        targetY = 16f - (8f * dragProgress)
                        targetRotation = -9f + (4.5f * dragProgress)
                        targetScale = 0.84f + (0.08f * dragProgress)
                        targetAlpha = 0.65f + (0.22f * dragProgress)
                        zIndexVal = 1f
                    }
                    1 -> {
                        // Middle card peeking on the left
                        targetX = -32f + (32f * dragProgress)
                        targetY = 8f - (8f * dragProgress)
                        targetRotation = -4.5f + (4.5f * dragProgress)
                        targetScale = 0.92f + (0.08f * dragProgress)
                        targetAlpha = 0.88f + (0.12f * dragProgress)
                        zIndexVal = 5f
                    }
                    else -> {
                        // Front top card
                        targetX = cardDragX.value
                        targetY = 0f
                        targetRotation = (cardDragX.value / 25f).coerceIn(-18f, 18f)
                        targetScale = (1f - (cardDragX.value.absoluteValue / 2200f)).coerceIn(0.85f, 1f)
                        targetAlpha = (1f - (cardDragX.value.absoluteValue / 560f)).coerceIn(0f, 1f)
                        zIndexVal = 10f
                    }
                }

                SinglePromoBannerItemCard(
                    banner = banner,
                    onClick = {
                        if (stackPos == 0 && cardDragX.value.absoluteValue < 20f) {
                            onBannerClick(banner)
                        } else if (stackPos != 0) {
                            flipNext()
                        }
                    },
                    modifier = Modifier
                        .zIndex(zIndexVal)
                        .graphicsLayer {
                            translationX = targetX * density
                            translationY = targetY * density
                            rotationZ = targetRotation
                            scaleX = targetScale
                            scaleY = targetScale
                            alpha = targetAlpha
                            cameraDistance = 14f * density
                        }
                        .width(270.dp)
                        .height(345.dp)
                )
            }
        }

        if (banners.size > 1) {
            Spacer(modifier = Modifier.height(14.dp))
            // Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                banners.indices.forEach { index ->
                    val isSelected = currentIndex == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 22.dp else 6.dp,
                        animationSpec = tween(durationMillis = 300),
                        label = "dot_width"
                    )

                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) NeonCyan else Color.White.copy(alpha = 0.25f)
                            )
                            .clickable {
                                if (currentIndex != index && !isTransitioning) {
                                    flipNext()
                                }
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun SinglePromoBannerItemCard(
    banner: PromoBannerItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Choose vertical poster graphic based on banner theme & content
    val posterImageRes = when {
        banner.id.contains("crypto") || banner.id.contains("gift") || banner.title.contains("ترید") || banner.title.contains("LBank") -> R.drawable.vertical_ad_crypto_gift
        banner.id.contains("video") || banner.id.contains("download") || banner.title.contains("ویدئو") || banner.title.contains("دانلود") -> R.drawable.vertical_ad_video_downloader
        banner.id.contains("rocket") || banner.id.contains("vip") || banner.title.contains("VIP") -> R.drawable.vertical_ad_vip_rocket
        banner.themeColorIndex == 3 -> R.drawable.vertical_ad_crypto_gift
        banner.themeColorIndex == 1 -> R.drawable.vertical_ad_video_downloader
        else -> R.drawable.vertical_ad_vip_rocket
    }

    val gradientColors = when (banner.themeColorIndex) {
        1 -> listOf(Color(0xFF4C1D95), Color(0xFF1E1B4B), Color(0xFF090817))
        2 -> listOf(Color(0xFF065F46), Color(0xFF064E3B), Color(0xFF051713))
        3 -> listOf(Color(0xFFB45309), Color(0xFF78350F), Color(0xFF190B04))
        else -> listOf(Color(0xFF1E3A8A), Color(0xFF1E293B), Color(0xFF070B14))
    }

    val accentColor = when (banner.themeColorIndex) {
        1 -> Color(0xFFD946EF)
        2 -> Color(0xFF34D399)
        3 -> Color(0xFFFBBF24)
        else -> Color(0xFF38BDF8)
    }

    val brandName = when {
        banner.title.contains("LBank") || banner.id.contains("crypto") -> "LBANK"
        banner.title.contains("VIP") || banner.id.contains("vip") -> "RENO VIP"
        banner.title.contains("ویدئو") || banner.id.contains("video") -> "POP VIDEO"
        else -> "RENO PRO"
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12141F)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, accentColor.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Background Artwork (Vertical Poster)
            Image(
                painter = painterResource(id = posterImageRes),
                contentDescription = banner.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dynamic Vertical Gradient Tint Overlay (Ensures high contrast & readable typography)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0A0B10).copy(alpha = 0.55f),
                                Color(0xFF0A0B10).copy(alpha = 0.25f),
                                gradientColors[1].copy(alpha = 0.75f),
                                Color(0xFF07090E).copy(alpha = 0.98f)
                            )
                        )
                    )
            )

            // Content Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Row (Brand Badge + Promo Tag)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = brandName,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    if (banner.badgeText.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(accentColor.copy(alpha = 0.25f))
                                .border(1.dp, accentColor.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = banner.badgeText,
                                color = accentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Content Details & CTA
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = banner.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (banner.subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = banner.subtitle,
                            color = Color.White.copy(alpha = 0.88f),
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Full-width modern CTA button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        accentColor,
                                        accentColor.copy(alpha = 0.85f)
                                    )
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = banner.buttonText,
                                color = if (banner.themeColorIndex == 3) Color.Black else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = if (banner.themeColorIndex == 3) Color.Black else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════
// Auto Pull-Down Server Refresh Animation & Component (Screenshot 1, 2, 3)
// ═════════════════════════════════════════════════════════════════════
enum class TopPullRefreshState {
    IDLE,
    PULLING_DOWN,
    REFRESHING,
    SUCCESS
}

@Composable
fun AutoPullDownRefreshWidget(
    state: TopPullRefreshState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val capsuleHeight by animateDpAsState(
        targetValue = when (state) {
            TopPullRefreshState.PULLING_DOWN -> 74.dp
            TopPullRefreshState.REFRESHING, TopPullRefreshState.SUCCESS -> 42.dp
            TopPullRefreshState.IDLE -> 12.dp
        },
        animationSpec = spring(dampingRatio = 0.68f, stiffness = 400f),
        label = "pull_height"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "refresh_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(capsuleHeight)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            TopPullRefreshState.PULLING_DOWN -> {
                // State 1 (Screenshot 3): Capsule pulled down with circle & arrow button inside
                Box(
                    modifier = Modifier
                        .width(34.dp)
                        .height(capsuleHeight)
                        .clip(RoundedCornerShape(17.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF1E3A8A).copy(alpha = 0.85f),
                                    Color(0xFF1E293B),
                                    Color(0xFF0F172A)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Color(0xFF38BDF8).copy(alpha = 0.5f),
                            RoundedCornerShape(17.dp)
                        )
                        .padding(bottom = 3.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF93C5FD), Color(0xFF60A5FA))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Pull Down Refresh",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            TopPullRefreshState.REFRESHING -> {
                // State 2 (Screenshot 2): Spinning circular refresh indicator
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF161F30))
                        .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refreshing Servers",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(rotation)
                    )
                }
            }
            TopPullRefreshState.SUCCESS -> {
                // State 3 (Screenshot 1): Glowing green circular badge with white checkmark
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                        .border(2.dp, Color(0xFF34D399).copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Updated Successfully",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            TopPullRefreshState.IDLE -> {
                // Spacing
            }
        }
    }
}
