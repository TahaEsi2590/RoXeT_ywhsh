package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PromoBannerItem
import com.example.model.VpnConfigItem
import com.example.model.VpnProtocol
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.VpnViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: VpnViewModel,
    configs: List<VpnConfigItem>,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val banners by viewModel.banners.collectAsState()
    val hideBannerAds by viewModel.hideBannerAds.collectAsState()

    var activeAdminTab by remember { mutableIntStateOf(0) } // 0: Servers, 1: Banners

    var showAddDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var configToEdit by remember { mutableStateOf<VpnConfigItem?>(null) }
    var configToDelete by remember { mutableStateOf<VpnConfigItem?>(null) }

    var showAddBannerDialog by remember { mutableStateOf(false) }
    var bannerToEdit by remember { mutableStateOf<PromoBannerItem?>(null) }
    var bannerToDelete by remember { mutableStateOf<PromoBannerItem?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .testTag("admin_panel_screen")
    ) {
        // Admin Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = viewModel.getString("admin_title"),
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonOrange.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ADMIN",
                                color = NeonOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = if (activeAdminTab == 0) "${configs.size} ${viewModel.getString("active_configs")}"
                        else "${banners.size} تبلیغ ثبت شده",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            IconButton(
                onClick = { showChangePinDialog = true },
                modifier = Modifier.testTag("change_pin_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = viewModel.getString("change_admin_pin"),
                    tint = NeonCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Navigation Tabs: Servers & Banners
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurface)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Tab 1: Servers
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (activeAdminTab == 0) NeonCyan.copy(alpha = 0.22f) else Color.Transparent)
                    .border(
                        1.dp,
                        if (activeAdminTab == 0) NeonCyan else Color.Transparent,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { activeAdminTab = 0 },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = null,
                        tint = if (activeAdminTab == 0) NeonCyan else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "سرورها (${configs.size})",
                        color = if (activeAdminTab == 0) NeonCyan else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = if (activeAdminTab == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            // Tab 2: Banners & Ads
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (activeAdminTab == 1) NeonPurple.copy(alpha = 0.22f) else Color.Transparent)
                    .border(
                        1.dp,
                        if (activeAdminTab == 1) NeonPurple else Color.Transparent,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { activeAdminTab = 1 },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = if (activeAdminTab == 1) NeonPurple else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تبلیغات و بنرها (${banners.size})",
                        color = if (activeAdminTab == 1) NeonPurple else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = if (activeAdminTab == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (activeAdminTab == 0) {
            // TAB 1: SERVERS MANAGEMENT
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("admin_add_config_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = DarkBackground,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = viewModel.getString("add_new_config"),
                        color = DarkBackground,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clipData = clipboard.primaryClip
                        if (clipData != null && clipData.itemCount > 0) {
                            val text = clipData.getItemAt(0).text?.toString() ?: ""
                            viewModel.importFromClipboard(text)
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonEmerald),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonEmerald),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("admin_import_clipboard_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = viewModel.getString("import_clipboard"),
                        color = NeonEmerald,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { viewModel.testAllPings() },
                    modifier = Modifier.testTag("admin_ping_all_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.NetworkCheck,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = viewModel.getString("test_ping"),
                        color = NeonCyan,
                        fontSize = 12.sp
                    )
                }

                TextButton(
                    onClick = { viewModel.restoreDefaults() },
                    modifier = Modifier.testTag("admin_restore_defaults_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = viewModel.getString("reset_defaults"),
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (configs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = viewModel.getString("no_configs"),
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(configs, key = { it.id }) { item ->
                        AdminConfigItemCard(
                            item = item,
                            onToggle = { enabled ->
                                viewModel.updateConfig(item.copy(isEnabled = enabled))
                            },
                            onEdit = { configToEdit = item },
                            onDelete = { configToDelete = item },
                            onCopy = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("VPN Config", item.rawConfig))
                            },
                            copyLabel = viewModel.getString("copy_config"),
                            expiredLabel = viewModel.getString("expired"),
                            activeLabel = viewModel.getString("active")
                        )
                    }
                }
            }
        } else {
            // TAB 2: PROMO BANNERS & ADS MANAGEMENT
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "نمایش تبلیغات در صفحه اصلی",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (!hideBannerAds) "تبلیغات به صورت اسلایدر خودکار هر ۳ ثانیه ورق می‌خورند"
                            else "تبلیغات در صفحه اصلی پنهان است",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Switch(
                        checked = !hideBannerAds,
                        onCheckedChange = { isEnabled ->
                            viewModel.setHideBannerAds(!isEnabled)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NeonPurple,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = DarkCard
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showAddBannerDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("admin_add_banner_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "افزودن تبلیغ جدید",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.restoreDefaultBanners() },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA78BFA)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA78BFA)),
                    modifier = Modifier
                        .weight(0.9f)
                        .height(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color(0xFFA78BFA),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "بنرهای پیش‌فرض",
                        color = Color(0xFFA78BFA),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E1B4B).copy(alpha = 0.5f))
                    .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ViewCarousel,
                        contentDescription = null,
                        tint = Color(0xFF818CF8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "حالت ورقه‌ای هوشمند: بنرها هر ۳ ثانیه به صورت حلقه‌ای جابه‌جا می‌شوند.",
                        color = Color(0xFFC7D2FE),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (banners.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "هیچ تبلیغی ثبت نشده است. با دکمه بالا تبلیغ جدید اضافه کنید.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(banners, key = { it.id }) { banner ->
                        AdminBannerItemCard(
                            banner = banner,
                            onToggle = { viewModel.toggleBannerEnabled(banner.id) },
                            onEdit = { bannerToEdit = banner },
                            onDelete = { bannerToDelete = banner }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Config Dialog
    if (showAddDialog || configToEdit != null) {
        val editItem = configToEdit
        AddOrEditConfigDialog(
            initialConfig = editItem,
            onDismiss = {
                showAddDialog = false
                configToEdit = null
            },
            onSave = { name, rawUrl, country, flag, protocol, days, remark ->
                if (editItem != null) {
                    val expiresAt = days?.let { System.currentTimeMillis() + it * 24L * 60L * 60L * 1000L }
                    viewModel.updateConfig(
                        editItem.copy(
                            name = name,
                            rawConfig = rawUrl,
                            countryCode = country,
                            flagEmoji = flag,
                            protocol = protocol,
                            expiresAt = expiresAt,
                            remark = remark
                        )
                    )
                } else {
                    viewModel.addConfig(
                        name = name,
                        rawConfig = rawUrl,
                        countryCode = country,
                        flagEmoji = flag,
                        protocol = protocol,
                        daysValid = days,
                        remark = remark
                    )
                }
                showAddDialog = false
                configToEdit = null
            },
            viewModel = viewModel
        )
    }

    // Add / Edit Banner Dialog
    if (showAddBannerDialog || bannerToEdit != null) {
        val editBanner = bannerToEdit
        AddOrEditBannerDialog(
            initialBanner = editBanner,
            onDismiss = {
                showAddBannerDialog = false
                bannerToEdit = null
            },
            onSave = { title, subtitle, buttonText, buttonLink, badgeText, themeColorIndex ->
                if (editBanner != null) {
                    viewModel.updateBanner(
                        editBanner.copy(
                            title = title,
                            subtitle = subtitle,
                            buttonText = buttonText,
                            buttonLink = buttonLink,
                            badgeText = badgeText,
                            themeColorIndex = themeColorIndex
                        )
                    )
                } else {
                    viewModel.addBanner(
                        title = title,
                        subtitle = subtitle,
                        buttonText = buttonText,
                        buttonLink = buttonLink,
                        badgeText = badgeText,
                        themeColorIndex = themeColorIndex
                    )
                }
                showAddBannerDialog = false
                bannerToEdit = null
            }
        )
    }

    // Delete Config Confirmation Dialog
    if (configToDelete != null) {
        val item = configToDelete!!
        AlertDialog(
            onDismissRequest = { configToDelete = null },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = viewModel.getString("delete"),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "${viewModel.getString("delete_confirm")}\n\n${item.name}",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteConfig(item.id)
                        configToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed)
                ) {
                    Text(viewModel.getString("delete"), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { configToDelete = null }) {
                    Text(viewModel.getString("cancel"), color = TextSecondary)
                }
            }
        )
    }

    // Delete Banner Confirmation Dialog
    if (bannerToDelete != null) {
        val banner = bannerToDelete!!
        AlertDialog(
            onDismissRequest = { bannerToDelete = null },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = "حذف تبلیغ",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "آیا از حذف این تبلیغ اطمینان دارید؟\n\n«${banner.title}»",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteBanner(banner.id)
                        bannerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed)
                ) {
                    Text("حذف", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { bannerToDelete = null }) {
                    Text("انصراف", color = TextSecondary)
                }
            }
        )
    }

    // Change PIN Dialog
    if (showChangePinDialog) {
        var newPin by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = viewModel.getString("change_admin_pin"),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 8) newPin = it },
                        placeholder = { Text("Enter 4-8 digits", color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = pinError!!, color = NeonRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin.length in 4..8) {
                            viewModel.changeAdminPin(newPin)
                            showChangePinDialog = false
                        } else {
                            pinError = "PIN must be between 4 and 8 digits"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text(viewModel.getString("save_pin"), color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) {
                    Text(viewModel.getString("cancel"), color = TextSecondary)
                }
            }
        )
    }
}

// ═════════════════════════════════════════════════════════════════════
// Admin Banner Item Card
// ═════════════════════════════════════════════════════════════════════
@Composable
fun AdminBannerItemCard(
    banner: PromoBannerItem,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradientColors = when (banner.themeColorIndex) {
        1 -> listOf(Color(0xFF581C87), Color(0xFF1E1B4B))
        2 -> listOf(Color(0xFF065F46), Color(0xFF0F172A))
        3 -> listOf(Color(0xFF7C2D12), Color(0xFF1C1917))
        else -> listOf(Color(0xFF1E3A8A), Color(0xFF0F172A))
    }

    val accentColor = when (banner.themeColorIndex) {
        1 -> Color(0xFFE879F9)
        2 -> Color(0xFF34D399)
        3 -> Color(0xFFFBBF24)
        else -> Color(0xFF38BDF8)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (banner.isEnabled) accentColor.copy(alpha = 0.5f) else DarkCardBorder
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (banner.isEnabled) NeonEmerald else TextMuted)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (banner.isEnabled) "فعال در اسلایدر" else "غیرفعال",
                        color = if (banner.isEnabled) NeonEmerald else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = NeonRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Switch(
                        checked = banner.isEnabled,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accentColor,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = DarkSurface
                        ),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(gradientColors))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = banner.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        if (banner.badgeText.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(accentColor.copy(alpha = 0.25f))
                                    .border(1.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = banner.badgeText,
                                    color = accentColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (banner.subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = banner.subtitle,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(accentColor)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = banner.buttonText,
                                color = if (banner.themeColorIndex == 3) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (banner.buttonLink.isNotBlank()) {
                            Text(
                                text = banner.buttonLink,
                                color = accentColor.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════
// Add / Edit Banner Dialog
// ═════════════════════════════════════════════════════════════════════
@Composable
fun AddOrEditBannerDialog(
    initialBanner: PromoBannerItem?,
    onDismiss: () -> Unit,
    onSave: (title: String, subtitle: String, buttonText: String, buttonLink: String, badgeText: String, themeIndex: Int) -> Unit
) {
    var title by remember { mutableStateOf(initialBanner?.title ?: "") }
    var subtitle by remember { mutableStateOf(initialBanner?.subtitle ?: "") }
    var buttonText by remember { mutableStateOf(initialBanner?.buttonText ?: "دانلود مستقیم") }
    var buttonLink by remember { mutableStateOf(initialBanner?.buttonLink ?: "") }
    var badgeText by remember { mutableStateOf(initialBanner?.badgeText ?: "ویژه") }
    var themeIndex by remember { mutableIntStateOf(initialBanner?.themeColorIndex ?: 0) }

    val themeOptions = listOf(
        "نئونی آبی" to Color(0xFF38BDF8),
        "ارغوانی / بنفش" to Color(0xFFE879F9),
        "سبز زمردی" to Color(0xFF34D399),
        "طلایی / نارنجی" to Color(0xFFFBBF24)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = if (initialBanner != null) "ویرایش تبلیغ" else "افزودن تبلیغ جدید",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان تبلیغ") },
                        placeholder = { Text("مثال: کانال رسمی تلگرام ReNo", color = TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = subtitle,
                        onValueChange = { subtitle = it },
                        label = { Text("توضیحات کوتاه تبلیغ") },
                        placeholder = { Text("مثال: دریافت آخرین سرورها و کانفیگ‌های رایگان", color = TextMuted) },
                        maxLines = 2,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = buttonText,
                        onValueChange = { buttonText = it },
                        label = { Text("متن روی دکمه") },
                        placeholder = { Text("مثال: دانلود مستقیم / عضویت در کانال", color = TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = buttonLink,
                        onValueChange = { buttonLink = it },
                        label = { Text("آدرس یا لینک دکمه (اختیاری)") },
                        placeholder = { Text("https://t.me/Reno_VpN_1", color = TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = badgeText,
                        onValueChange = { badgeText = it },
                        label = { Text("برچسب یا نشان") },
                        placeholder = { Text("ویژه / رایگان / VIP / تلگرام", color = TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text(
                        text = "انتخاب تم رنگی بنر:",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        themeOptions.forEachIndexed { index, (name, color) ->
                            val isSelected = themeIndex == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) color.copy(alpha = 0.25f) else DarkCard)
                                    .border(
                                        1.5.dp,
                                        if (isSelected) color else DarkCardBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { themeIndex = index },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(title, subtitle, buttonText, buttonLink, badgeText, themeIndex)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("ذخیره تبلیغ", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun AdminConfigItemCard(
    item: VpnConfigItem,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    copyLabel: String,
    expiredLabel: String,
    activeLabel: String
) {
    val isExpired = item.isExpired
    val statusColor = if (isExpired) NeonRed else if (item.isEnabled) NeonEmerald else TextMuted

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isExpired) NeonRed.copy(alpha = 0.4f) else DarkCardBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_config_card_${item.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top: Flag + Name + Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = item.flagEmoji, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.name,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = item.protocol.displayName,
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(text = "•", color = TextMuted, fontSize = 10.sp)
                            Text(
                                text = if (isExpired) expiredLabel else activeLabel,
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (item.expiresAt != null) {
                                val dateStr = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                                    .format(Date(item.expiresAt))
                                Text(
                                    text = "($dateStr)",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Switch(
                    checked = item.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = NeonEmerald,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = DarkSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Raw config URI preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkBackground)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = item.rawConfig.ifBlank { "${item.protocol.displayName}://${item.serverAddress}:${item.port}" },
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons (Copy, Edit, Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCopy, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = copyLabel,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = NeonRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddOrEditConfigDialog(
    initialConfig: VpnConfigItem?,
    onDismiss: () -> Unit,
    onSave: (name: String, rawUrl: String, country: String, flag: String, protocol: VpnProtocol, days: Int?, remark: String) -> Unit,
    viewModel: VpnViewModel
) {
    var name by remember { mutableStateOf(initialConfig?.name ?: "") }
    var rawUrl by remember { mutableStateOf(initialConfig?.rawConfig ?: "") }
    var flag by remember { mutableStateOf(initialConfig?.flagEmoji ?: "🇩🇪") }
    var country by remember { mutableStateOf(initialConfig?.countryCode ?: "DE") }
    var protocol by remember { mutableStateOf(initialConfig?.protocol ?: VpnProtocol.VLESS) }
    var daysText by remember {
        mutableStateOf(
            if (initialConfig?.expiresAt != null) {
                val remainingDays = ((initialConfig.expiresAt - System.currentTimeMillis()) / (24L * 60 * 60 * 1000)).coerceAtLeast(1)
                remainingDays.toString()
            } else ""
        )
    }
    var remark by remember { mutableStateOf(initialConfig?.remark ?: "") }
    var protocolMenuExpanded by remember { mutableStateOf(false) }

    val flagsList = listOf(
        "🇩🇪" to "DE", "🇳🇱" to "NL", "🇫🇮" to "FI", "🇹🇷" to "TR",
        "🇺🇸" to "US", "🇬🇧" to "GB", "🇫🇷" to "FR", "🇨🇦" to "CA",
        "🇸🇬" to "SG", "🇨🇭" to "CH", "🇦🇪" to "AE", "⚡" to "AUTO"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = if (initialConfig != null) viewModel.getString("edit_config") else viewModel.getString("add_new_config"),
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(viewModel.getString("config_name")) },
                    placeholder = { Text(viewModel.getString("config_name_hint"), color = TextMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Raw URL Field
                OutlinedTextField(
                    value = rawUrl,
                    onValueChange = { rawUrl = it },
                    label = { Text(viewModel.getString("config_url")) },
                    placeholder = { Text(viewModel.getString("config_url_hint"), color = TextMuted) },
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Flag Selection Row
                Column {
                    Text(
                        text = viewModel.getString("flag"),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(flagsList) { (fEmoji, cCode) ->
                            val isSelected = flag == fEmoji
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else DarkCard)
                                    .border(
                                        1.dp,
                                        if (isSelected) NeonCyan else DarkCardBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        flag = fEmoji
                                        country = cCode
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = fEmoji, fontSize = 18.sp)
                            }
                        }
                    }
                }

                // Protocol Dropdown
                ExposedDropdownMenuBox(
                    expanded = protocolMenuExpanded,
                    onExpandedChange = { protocolMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = protocol.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(viewModel.getString("protocol_type")) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = protocolMenuExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = protocolMenuExpanded,
                        onDismissRequest = { protocolMenuExpanded = false },
                        containerColor = DarkSurface
                    ) {
                        VpnProtocol.values().forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.displayName, color = TextPrimary) },
                                onClick = {
                                    protocol = p
                                    protocolMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Expiration Days
                OutlinedTextField(
                    value = daysText,
                    onValueChange = { daysText = it.filter { ch -> ch.isDigit() } },
                    label = { Text(viewModel.getString("expiration_date")) },
                    placeholder = { Text("30 (leave blank for unlimited)", color = TextMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val days = daysText.toIntOrNull()
                    onSave(name, rawUrl, country, flag, protocol, days, remark)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(viewModel.getString("save_config"), color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(viewModel.getString("cancel"), color = TextSecondary)
            }
        }
    )
}
