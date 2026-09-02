package com.example.model

enum class VpnState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING
}

enum class VpnProtocol(val displayName: String) {
    VLESS("VLESS"),
    VMESS("VMESS"),
    SHADOWSOCKS("Shadowsocks"),
    TROJAN("Trojan"),
    WIREGUARD("WireGuard"),
    HYSTERIA2("Hysteria 2"),
    CUSTOM("Custom")
}

enum class AppLanguage(val code: String, val nativeName: String, val englishName: String) {
    FA("fa", "فارسی", "Persian"),
    EN("en", "English", "English")
}

enum class ServerTrafficLoad(val bars: Int, val levelNameFa: String, val levelNameEn: String) {
    LOW(4, "خلوت و پرسرعت", "Low Traffic / Fast"),
    MEDIUM(2, "ترافیک متوسط", "Medium Traffic"),
    HIGH(1, "شلوغ و پرکاربر", "High Traffic / Heavy Load")
}

data class VpnConfigItem(
    val id: String,
    val name: String,
    val countryCode: String,
    val flagEmoji: String,
    val serverAddress: String,
    val port: Int = 443,
    val protocol: VpnProtocol = VpnProtocol.VLESS,
    val rawConfig: String = "",
    val pingMs: Int = 45,
    val isVip: Boolean = false,
    val isCustom: Boolean = false,
    val isEnabled: Boolean = true,
    val trafficLoad: ServerTrafficLoad = ServerTrafficLoad.LOW,
    val onlineUsers: Int = 145,
    val locationsCount: Int = 1,
    val isFavorite: Boolean = false,
    val persianCountryName: String = "",
    val addedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null,
    val remark: String = ""
) {
    val isExpired: Boolean
        get() = expiresAt != null && expiresAt < System.currentTimeMillis()
}

data class ConnectionStats(
    val uploadSpeedBps: Long = 0L,
    val downloadSpeedBps: Long = 0L,
    val totalUploadedBytes: Long = 0L,
    val totalDownloadedBytes: Long = 0L,
    val durationSeconds: Long = 0L,
    val currentPingMs: Int = 0,
    val virtualIp: String = "185.220.101.42",
    val dnsServer: String = "1.1.1.1 (Cloudflare)"
)

data class DnsOption(
    val id: String,
    val name: String,
    val primaryIp: String,
    val secondaryIp: String,
    val description: String
)

data class PromoBannerItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val buttonText: String = "دانلود مستقیم",
    val buttonLink: String = "",
    val badgeText: String = "ویژه",
    val themeColorIndex: Int = 0, // 0: Cyan/Blue, 1: Purple/Pink, 2: Emerald/Teal, 3: Orange/Amber
    val isEnabled: Boolean = true,
    val addedAt: Long = System.currentTimeMillis()
)
