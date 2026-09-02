package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppLanguage
import com.example.model.ServerTrafficLoad
import com.example.model.VpnConfigItem
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class ServerTab {
    LOCATIONS, FAVORITES, RECENT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSelectionSheet(
    sheetState: SheetState,
    configs: List<VpnConfigItem>,
    selectedConfig: VpnConfigItem?,
    onSelectConfig: (VpnConfigItem) -> Unit,
    onToggleFavorite: (String) -> Unit = {},
    onDismiss: () -> Unit,
    language: AppLanguage = AppLanguage.FA
) {
    var selectedTab by remember { mutableStateOf(ServerTab.LOCATIONS) }
    var lockedDialogConfig by remember { mutableStateOf<VpnConfigItem?>(null) }
    var expandedCountryId by remember { mutableStateOf<String?>(null) }

    val autoConfig = configs.find { it.id == "auto-optimal" || it.countryCode == "AUTO" } ?: configs.firstOrNull()
    val otherConfigs = configs.filter { it.id != (autoConfig?.id ?: "") }

    val displayedConfigs = when (selectedTab) {
        ServerTab.LOCATIONS -> configs
        ServerTab.FAVORITES -> configs.filter { it.isFavorite || it.id == "auto-optimal" }
        ServerTab.RECENT -> configs.take(4)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF14151C),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 38.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2C2E3D))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .heightIn(max = 640.dp)
        ) {
            // Header: Close 'X' button on left, Title "مکان" on right (Screenshot 5)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }

                Text(
                    text = if (language == AppLanguage.FA) "مکان" else "Locations",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Filter Tabs: [مکان ها] [مورد علاقه] [اخیر] (Screenshot 5)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1B1C25))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ServerTabButton(
                    title = if (language == AppLanguage.FA) "مکان ها" else "Locations",
                    isSelected = selectedTab == ServerTab.LOCATIONS,
                    onClick = { selectedTab = ServerTab.LOCATIONS },
                    modifier = Modifier.weight(1f)
                )
                ServerTabButton(
                    title = if (language == AppLanguage.FA) "مورد علاقه" else "Favorites",
                    isSelected = selectedTab == ServerTab.FAVORITES,
                    onClick = { selectedTab = ServerTab.FAVORITES },
                    modifier = Modifier.weight(1f)
                )
                ServerTabButton(
                    title = if (language == AppLanguage.FA) "اخیر" else "Recent",
                    isSelected = selectedTab == ServerTab.RECENT,
                    onClick = { selectedTab = ServerTab.RECENT },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Server Location List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayedConfigs, key = { it.id }) { item ->
                    val isSelected = selectedConfig?.id == item.id
                    val isAuto = item.id == "auto-optimal" || item.countryCode == "AUTO"

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF1B2433) else Color(0xFF181922)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) Color(0xFF3B82F6) else Color(0xFF262835)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isAuto) {
                                    onSelectConfig(item)
                                    onDismiss()
                                } else {
                                    // Locked VIP country
                                    lockedDialogConfig = item
                                }
                            }
                            .testTag("server_item_${item.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left Side: Antenna Signal Indicator & Favorite Star
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Antenna Indicator
                                AntennaSignalIndicator(
                                    load = item.trafficLoad,
                                    showLabel = false,
                                    size = 22.dp
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                // Favorite Star
                                IconButton(
                                    onClick = { onToggleFavorite(item.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                                        contentDescription = "Favorite",
                                        tint = if (item.isFavorite) Color(0xFFFBBF24) else Color(0xFF555B6E),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Right Side: Country Title, Subtitle, Flag / Logo, and VIP Badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (item.isVip) {
                                            // Golden VIP [V] Badge (Screenshot 5)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        Brush.horizontalGradient(
                                                            listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                                                        )
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "V",
                                                    color = Color.Black,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }

                                        Text(
                                            text = if (language == AppLanguage.FA) item.persianCountryName else item.name,
                                            color = TextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = if (language == AppLanguage.FA) "${item.locationsCount} مکان ها" else "${item.locationsCount} Locations",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Flag or ReNo Logo
                                if (isAuto) {
                                    Image(
                                        painter = painterResource(id = R.drawable.reno_symbol),
                                        contentDescription = "ReNo VPN",
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Text(
                                        text = item.flagEmoji,
                                        fontSize = 26.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Locked Country / VIP Dialog
    lockedDialogConfig?.let { target ->
        AlertDialog(
            onDismissRequest = { lockedDialogConfig = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "${target.persianCountryName} (VIP)",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "این سرور در نسخه رایگان قفل است. در حال حاضر گزینه «پیش‌فرض (Auto Location)» برای شما فعال است و به صورت خودکار به پایدارترین نود متصل می‌شود.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AntennaSignalIndicator(
                        load = target.trafficLoad,
                        showLabel = true,
                        language = language
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // User can still choose to test or switch
                        onSelectConfig(target)
                        lockedDialogConfig = null
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("اتصال آزمایشی (VIP)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { lockedDialogConfig = null }) {
                    Text("بستن", color = TextMuted)
                }
            },
            containerColor = Color(0xFF1E202B),
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun ServerTabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFF2B2D3C) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) TextPrimary else TextMuted,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
