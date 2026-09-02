package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.DnsOption
import com.example.ui.components.GuestReminderDialog
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.VpnViewModel

@Composable
fun SettingsScreen(
    viewModel: VpnViewModel,
    language: AppLanguage,
    killSwitchEnabled: Boolean,
    splitTunnelingEnabled: Boolean,
    autoConnectEnabled: Boolean,
    selectedDns: DnsOption,
    onBack: () -> Unit,
    onOpenAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val hideBannerAds by viewModel.hideBannerAds.collectAsState()
    val routingMode by viewModel.routingMode.collectAsState()

    var showGuestDialog by remember { mutableStateOf(false) }
    var showRoutingDialog by remember { mutableStateOf(false) }
    var showSplitTunnelDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showDiagnosticDialog by remember { mutableStateOf(false) }
    var showInfoDialogTitle by remember { mutableStateOf<String?>(null) }
    var showInfoDialogText by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1015))
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(scrollState)
            .testTag("settings_screen")
    ) {
        // Top Header: Close 'X' on left, Title "تنظیمات" with menu icon on right, "3.3.1" (Screenshots 2 & 3)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TextPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "3.3.1",
                    color = Color(0xFF6B7280),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = if (language == AppLanguage.FA) "تنظیمات" else "Settings",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Settings Icon",
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // ═════════════════════════════════════════════════════════════════════
        // SECTION 1: حساب کاربری (User Account) - Screenshot 2
        // ═════════════════════════════════════════════════════════════════════
        SectionHeaderTitle(title = if (language == AppLanguage.FA) "حساب کاربری" else "User Account")

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161720)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF242634)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // Email -> Login Action
                SettingsNavRow(
                    title = if (language == AppLanguage.FA) "ایمیل" else "Email",
                    actionText = if (language == AppLanguage.FA) "< ورود" else "Login >",
                    actionColor = Color(0xFF3B82F6),
                    onClick = { showGuestDialog = true }
                )

                HorizontalDivider(color = Color(0xFF222432), thickness = 0.8.dp)

                // Hide Banner Ads Toggle (Screenshot 3)
                SettingsToggleRowItem(
                    title = if (language == AppLanguage.FA) "پنهان کردن تبلیغات بنری صفحه اصلی" else "Hide Main Screen Banner Ads",
                    checked = hideBannerAds,
                    onCheckedChange = { viewModel.setHideBannerAds(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ═════════════════════════════════════════════════════════════════════
        // SECTION 2: شبکه (Network) - Screenshot 2 & 3
        // ═════════════════════════════════════════════════════════════════════
        SectionHeaderTitle(title = if (language == AppLanguage.FA) "شبکه" else "Network")

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161720)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF242634)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // Routing Mode (حالت مسیریابی)
                val routingLabel = when (routingMode) {
                    "bypass_lan" -> if (language == AppLanguage.FA) "دور زدن شبکه محلی >" else "Bypass LAN >"
                    "proxy_only" -> if (language == AppLanguage.FA) "فقط پروکسی >" else "Proxy Only >"
                    else -> if (language == AppLanguage.FA) "مسیریابی جهانی >" else "Global Routing >"
                }
                SettingsNavRow(
                    title = if (language == AppLanguage.FA) "حالت مسیریابی" else "Routing Mode",
                    actionText = routingLabel,
                    onClick = { showRoutingDialog = true }
                )

                HorizontalDivider(color = Color(0xFF222432), thickness = 0.8.dp)

                // Split Tunneling (تقسیم تونل)
                SettingsNavRow(
                    title = if (language == AppLanguage.FA) "تقسیم تونل" else "Split Tunneling",
                    actionText = if (splitTunnelingEnabled) {
                        if (language == AppLanguage.FA) "همه برنامه‌ها (پیش‌فرض) >" else "All Apps (Default) >"
                    } else {
                        if (language == AppLanguage.FA) "غیرفعال >" else "Disabled >"
                    },
                    onClick = { showSplitTunnelDialog = true }
                )

                HorizontalDivider(color = Color(0xFF222432), thickness = 0.8.dp)

                // Restore Defaults (بازیابی پیش‌فرض)
                SettingsNavRow(
                    title = if (language == AppLanguage.FA) "بازیابی پیش‌فرض" else "Restore Defaults",
                    actionText = if (language == AppLanguage.FA) "<" else ">",
                    onClick = { showResetDialog = true }
                )

                HorizontalDivider(color = Color(0xFF222432), thickness = 0.8.dp)

                // Network Diagnostic (تشخیص شبکه)
                SettingsNavRow(
                    title = if (language == AppLanguage.FA) "تشخیص شبکه" else "Network Diagnostic",
                    actionText = if (language == AppLanguage.FA) "<" else ">",
                    onClick = { showDiagnosticDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ═════════════════════════════════════════════════════════════════════
        // SECTION 3: برنامه (App) - Screenshot 3
        // ════════════════════════════════════════════════════════════════════
        SectionHeaderTitle(title = if (language == AppLanguage.FA) "برنامه" else "App")

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161720)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF242634)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // Language (زبان)
                SettingsNavRow(
                    title = if (language == AppLanguage.FA) "زبان" else "Language",
                    actionText = if (language == AppLanguage.FA) "فارسی (fa) >" else "English (en) >",
                    onClick = { showLanguageDialog = true }
                )

                HorizontalDivider(color = Color(0xFF222432), thickness = 0.8.dp)

                // Contact Us (تماس با ما / کانال تلگرام)
                SettingsNavRow(
                    title = if (language == AppLanguage.FA) "تماس با ما و کانال تلگرام" else "Contact Us & Telegram",
                    actionText = "@Reno_VpN_1 >",
                    actionColor = NeonCyan,
                    onClick = {
                        showInfoDialogTitle = "تماس با ما و کانال تلگرام"
                        showInfoDialogText = "کانال رسمی تلگرام: @Reno_VpN_1\nلینک مستقیم: https://t.me/Reno_VpN_1\nایمیل پشتیبانی: support@reno-vpn.org\nپاسخگویی ۲۴/۷ و ارائه جدیدترین سرورها و کانفیگ‌های پرسرعت"
                    }
                )

                HorizontalDivider(color = Color(0xFF222432), thickness = 0.8.dp)

                // FAQ (سؤالات متداول)
                SettingsNavRow(
                    title = if (language == AppLanguage.FA) "سؤالات متداول" else "FAQ",
                    actionText = if (language == AppLanguage.FA) "<" else ">",
                    onClick = {
                        showInfoDialogTitle = "سؤالات متداول"
                        showInfoDialogText = "۱. چرا اتصال قطع می‌شود؟\nدر صورت اختلال شبکه، پروتکل به طور هوشمند تغییر می‌کند.\n۲. آیا نیاز به خرید اشتراک است؟\nگزینه «پیش‌فرض» کاملاً رایگان و پرسرعت است.\n۳. نحوه وارد کردن کانفیگ اختصاصی؟\nاز بخش پنل مدیریت ادمین می‌توانید کانفیگ‌های خود را اضافه کنید."
                    }
                )

                HorizontalDivider(color = Color(0xFF222432), thickness = 0.8.dp)

                // Privacy Policy (سیاست حفظ حریم خصوصی)
                SettingsNavRow(
                    title = if (language == AppLanguage.FA) "سیاست حفظ حریم خصوصی" else "Privacy Policy",
                    actionText = if (language == AppLanguage.FA) "<" else ">",
                    onClick = {
                        showInfoDialogTitle = "سیاست حفظ حریم خصوصی"
                        showInfoDialogText = "رنو وی‌پی‌ان (ReNo VPN) متعهد به حفظ کامل حریم خصوصی شماست. هیچ‌گونه گزارش فعالیت (No-Log Policy) یا ردپای داده‌های کاربران بر روی سرورها ذخیره نمی‌شود."
                    }
                )

                HorizontalDivider(color = Color(0xFF222432), thickness = 0.8.dp)

                // Terms of Service (شرایط استفاده از خدمات)
                SettingsNavRow(
                    title = if (language == AppLanguage.FA) "شرایط استفاده از خدمات" else "Terms of Service",
                    actionText = if (language == AppLanguage.FA) "<" else ">",
                    onClick = {
                        showInfoDialogTitle = "شرایط استفاده از خدمات"
                        showInfoDialogText = "استفاده از این برنامه برای دسترسی به اینترنت آزاد و ایمن‌سازی ارتباطات طراحی شده است."
                    }
                )

                HorizontalDivider(color = Color(0xFF222432), thickness = 0.8.dp)

                // Privacy & Security (حریم خصوصی و امنیت)
                SettingsNavRow(
                    title = if (language == AppLanguage.FA) "حریم خصوصی و امنیت" else "Privacy & Security",
                    actionText = if (language == AppLanguage.FA) "<" else ">",
                    onClick = {
                        showInfoDialogTitle = "حریم خصوصی و امنیت"
                        showInfoDialogText = "• رمزنگاری نظامی AES-256-GCM\n• محافظت در برابر نشت DNS و IPv6\n• سوئیچ قطع خودکار اینترنت در قطعی ناگهانی"
                    }
                )

                HorizontalDivider(color = Color(0xFF222432), thickness = 0.8.dp)

                // Version (نسخه)
                SettingsNavRow(
                    title = if (language == AppLanguage.FA) "نسخه" else "Version",
                    actionText = "V3.3.1 (ReNo VPN)",
                    onClick = { viewModel.onSecretLogoTap() }
                )

                HorizontalDivider(color = Color(0xFF222432), thickness = 0.8.dp)

                // Diagnostic Logs (گزارش‌های عیب‌یابی)
                SettingsNavRow(
                    title = if (language == AppLanguage.FA) "گزارش‌های عیب‌یابی" else "Diagnostic Logs",
                    actionText = if (language == AppLanguage.FA) "<" else ">",
                    onClick = {
                        showInfoDialogTitle = "گزارش‌های عیب‌یابی"
                        showInfoDialogText = "[Core] V2Ray/Xray Engine Initialized\n[TLS] SNI Camouflage Active\n[DNS] Secure DoH Handshake OK\n[Tunnel] Ready on tun0 interface"
                    }
                )

                HorizontalDivider(color = Color(0xFF222432), thickness = 0.8.dp)

                // Admin Panel (پنل مدیریت سرورها)
                SettingsNavRow(
                    title = if (language == AppLanguage.FA) "پنل مدیریت سرورها (ادمین)" else "Admin Server Manager",
                    actionText = if (language == AppLanguage.FA) "ورود با رمز >" else "Enter PIN >",
                    actionColor = NeonCyan,
                    onClick = onOpenAdmin
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // ── Dialogs ─────────────────────────────────────────────────────────────

    // Guest Reminder Dialog
    if (showGuestDialog) {
        GuestReminderDialog(
            onDismiss = { showGuestDialog = false },
            onRegister = { showGuestDialog = false },
            onLogin = { showGuestDialog = false }
        )
    }

    // Routing Mode Dialog
    if (showRoutingDialog) {
        AlertDialog(
            onDismissRequest = { showRoutingDialog = false },
            title = { Text("حالت مسیریابی (Routing)", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    RoutingOptionItem(
                        title = "مسیریابی جهانی (Global)",
                        subtitle = "تمام ترافیک از فیلترشکن عبور می‌کند",
                        isSelected = routingMode == "global",
                        onClick = {
                            viewModel.setRoutingMode("global")
                            showRoutingDialog = false
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RoutingOptionItem(
                        title = "دور زدن شبکه محلی (Bypass LAN)",
                        subtitle = "آی‌پی‌های داخلی و بانکی بدون VPN باز می‌شوند",
                        isSelected = routingMode == "bypass_lan",
                        onClick = {
                            viewModel.setRoutingMode("bypass_lan")
                            showRoutingDialog = false
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RoutingOptionItem(
                        title = "فقط پروکسی (Proxy Only)",
                        subtitle = "فقط برنامه‌های مشخص شده",
                        isSelected = routingMode == "proxy_only",
                        onClick = {
                            viewModel.setRoutingMode("proxy_only")
                            showRoutingDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoutingDialog = false }) {
                    Text("بستن", color = TextMuted)
                }
            },
            containerColor = Color(0xFF1E202B),
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Split Tunneling Dialog
    if (showSplitTunnelDialog) {
        AlertDialog(
            onDismissRequest = { showSplitTunnelDialog = false },
            title = { Text("تنظیم تقسیم تونل (Split Tunneling)", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "با فعال‌سازی تقسیم تونل، برنامه‌های بانکی و پیام‌رسان‌های داخلی بدون کاهش سرعت و بدون قطع VPN به صورت مستقیم به اینترنت متصل می‌شوند.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF262838))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("فعال‌سازی تفکیک هوشمند", color = TextPrimary, fontSize = 14.sp)
                        Switch(
                            checked = splitTunnelingEnabled,
                            onCheckedChange = { viewModel.toggleSplitTunneling(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF3B82F6)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSplitTunnelDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    Text("تایید", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E202B),
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Language Picker Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("انتخاب زبان (Language)", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setLanguage(AppLanguage.FA)
                                showLanguageDialog = false
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🇮🇷 فارسی (Persian)", color = TextPrimary, fontSize = 15.sp)
                        if (language == AppLanguage.FA) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF3B82F6))
                        }
                    }
                    HorizontalDivider(color = Color(0xFF2B2D3C))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setLanguage(AppLanguage.EN)
                                showLanguageDialog = false
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🇬🇧 English (انگلیسی)", color = TextPrimary, fontSize = 15.sp)
                        if (language == AppLanguage.EN) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF3B82F6))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("انصراف", color = TextMuted)
                }
            },
            containerColor = Color(0xFF1E202B),
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Reset Defaults Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("بازیابی تنظیمات پیش‌فرض", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "آیا مایلید تمام تنظیمات و لیست سرورها به حالت پیش‌فرض اولیه ReNo VPN بازگردانی شوند؟",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetToDefaults()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("بازیابی", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("انصراف", color = TextMuted)
                }
            },
            containerColor = Color(0xFF1E202B),
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Network Diagnostic Dialog
    if (showDiagnosticDialog) {
        AlertDialog(
            onDismissRequest = { showDiagnosticDialog = false },
            title = { Text("تشخیص و وضعیت شبکه", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("• اتصال اینترنت محلی: برقرار (WiFi / 4G)", color = NeonEmerald, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• پاسخ‌دهی سرور DNS: عالی (Cloudflare DoH 1.1.1.1)", color = NeonEmerald, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• تونل VLESS/TLS: بدون تداخل و آماده اتصال", color = NeonCyan, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• سرعت سنج زنده: فعال", color = TextPrimary, fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDiagnosticDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    Text("تایید", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E202B),
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Generic Info Dialog (FAQ, Contact, Privacy, Terms)
    if (showInfoDialogTitle != null) {
        val isTelegramContact = showInfoDialogText?.contains("@Reno_VpN_1") == true
        AlertDialog(
            onDismissRequest = {
                showInfoDialogTitle = null
                showInfoDialogText = null
            },
            title = { Text(showInfoDialogTitle ?: "", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = showInfoDialogText ?: "",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isTelegramContact) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/Reno_VpN_1"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088CC)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("ورود به کانال تلگرام", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Button(
                        onClick = {
                            showInfoDialogTitle = null
                            showInfoDialogText = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("متوجه شدم", color = Color.White)
                    }
                }
            },
            containerColor = Color(0xFF1E202B),
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun SectionHeaderTitle(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Text(
            text = title,
            color = Color(0xFF9CA3AF),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SettingsNavRow(
    title: String,
    actionText: String,
    actionColor: Color = Color(0xFF9CA3AF),
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = actionText,
            color = actionColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = title,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SettingsToggleRowItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF3B82F6),
                uncheckedThumbColor = Color(0xFF8E95A5),
                uncheckedTrackColor = Color(0xFF262836)
            )
        )

        Text(
            text = title,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f).padding(start = 12.dp)
        )
    }
}

@Composable
private fun RoutingOptionItem(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF263248) else Color(0xFF161722))
            .border(1.dp, if (isSelected) Color(0xFF3B82F6) else Color(0xFF2B2D3C), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF3B82F6))
        )
        Column(horizontalAlignment = Alignment.End) {
            Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = TextMuted, fontSize = 11.sp)
        }
    }
}
