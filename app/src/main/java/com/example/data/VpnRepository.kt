package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppLanguage
import com.example.model.DnsOption
import com.example.model.PromoBannerItem
import com.example.model.VpnConfigItem
import com.example.model.VpnProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class VpnRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("jump_vpn_prefs", Context.MODE_PRIVATE)

    private val _configs = MutableStateFlow<List<VpnConfigItem>>(emptyList())
    val configs: StateFlow<List<VpnConfigItem>> = _configs.asStateFlow()

    private val _selectedConfig = MutableStateFlow<VpnConfigItem?>(null)
    val selectedConfig: StateFlow<VpnConfigItem?> = _selectedConfig.asStateFlow()

    private val _banners = MutableStateFlow<List<PromoBannerItem>>(emptyList())
    val banners: StateFlow<List<PromoBannerItem>> = _banners.asStateFlow()

    private val _language = MutableStateFlow(AppLanguage.FA)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _killSwitchEnabled = MutableStateFlow(false)
    val killSwitchEnabled: StateFlow<Boolean> = _killSwitchEnabled.asStateFlow()

    private val _splitTunnelingEnabled = MutableStateFlow(true)
    val splitTunnelingEnabled: StateFlow<Boolean> = _splitTunnelingEnabled.asStateFlow()

    private val _autoConnectEnabled = MutableStateFlow(false)
    val autoConnectEnabled: StateFlow<Boolean> = _autoConnectEnabled.asStateFlow()

    private val _adminPin = MutableStateFlow("1234")
    val adminPin: StateFlow<String> = _adminPin.asStateFlow()

    private val _hideBannerAds = MutableStateFlow(false)
    val hideBannerAds: StateFlow<Boolean> = _hideBannerAds.asStateFlow()

    private val _routingMode = MutableStateFlow("global") // global, bypass_lan, proxy_only
    val routingMode: StateFlow<String> = _routingMode.asStateFlow()

    private val _selectedDns = MutableStateFlow(dnsOptions[0])
    val selectedDns: StateFlow<DnsOption> = _selectedDns.asStateFlow()

    companion object {
        val dnsOptions = listOf(
            DnsOption("cloudflare", "Cloudflare Secure", "1.1.1.1", "1.0.0.1", "Fastest global DNS"),
            DnsOption("google", "Google Public", "8.8.8.8", "8.8.4.4", "Reliable and high-speed"),
            DnsOption("shecan", "Shecan Anti-Sanction (شکن)", "178.22.122.100", "185.51.200.2", "Specialized for Persian users"),
            DnsOption("quad9", "Quad9 Secure", "9.9.9.9", "149.112.112.112", "Blocks malware & phishing"),
            DnsOption("adguard", "AdGuard DNS", "94.140.14.14", "94.140.15.15", "Ad & Tracker blocking")
        )

        private val defaultInitialConfigs = listOf(
            VpnConfigItem(
                id = "auto-optimal",
                name = "پیش‌فرض",
                persianCountryName = "پیش‌فرض",
                countryCode = "AUTO",
                flagEmoji = "⚡",
                serverAddress = "auto-optimal.reno-vpn.org",
                port = 443,
                protocol = VpnProtocol.VLESS,
                rawConfig = "vless://auto-smart@auto.reno-vpn.org:443?security=tls&type=ws#⚡+Auto+Location",
                pingMs = 35,
                isVip = false,
                isCustom = false,
                trafficLoad = com.example.model.ServerTrafficLoad.LOW,
                onlineUsers = 142,
                locationsCount = 1
            ),
            VpnConfigItem(
                id = "nl-01",
                name = "هلند",
                persianCountryName = "هلند",
                countryCode = "NL",
                flagEmoji = "🇳🇱",
                serverAddress = "ams1.reno-vpn.org",
                port = 8443,
                protocol = VpnProtocol.VMESS,
                rawConfig = "vmess://reno-nl-fast@ams1.reno-vpn.org:8443?type=tcp#🇳🇱+Netherlands",
                pingMs = 58,
                isVip = true,
                isCustom = false,
                trafficLoad = com.example.model.ServerTrafficLoad.LOW,
                onlineUsers = 490,
                locationsCount = 2
            ),
            VpnConfigItem(
                id = "us-01",
                name = "ایالات متحده",
                persianCountryName = "ایالات متحده",
                countryCode = "US",
                flagEmoji = "🇺🇸",
                serverAddress = "us1.reno-vpn.org",
                port = 443,
                protocol = VpnProtocol.VLESS,
                rawConfig = "vless://reno-us-ca@us1.reno-vpn.org:443?security=tls#🇺🇸+USA",
                pingMs = 125,
                isVip = true,
                isCustom = false,
                trafficLoad = com.example.model.ServerTrafficLoad.MEDIUM,
                onlineUsers = 820,
                locationsCount = 20
            ),
            VpnConfigItem(
                id = "ca-01",
                name = "کانادا",
                persianCountryName = "کانادا",
                countryCode = "CA",
                flagEmoji = "🇨🇦",
                serverAddress = "ca1.reno-vpn.org",
                port = 443,
                protocol = VpnProtocol.VLESS,
                rawConfig = "vless://reno-ca-speed@ca1.reno-vpn.org:443#🇨🇦+Canada",
                pingMs = 115,
                isVip = true,
                isCustom = false,
                trafficLoad = com.example.model.ServerTrafficLoad.LOW,
                onlineUsers = 340,
                locationsCount = 3
            ),
            VpnConfigItem(
                id = "fr-01",
                name = "فرانسه",
                persianCountryName = "فرانسه",
                countryCode = "FR",
                flagEmoji = "🇫🇷",
                serverAddress = "par1.reno-vpn.org",
                port = 443,
                protocol = VpnProtocol.SHADOWSOCKS,
                rawConfig = "ss://reno-fr-fast@par1.reno-vpn.org:443#🇫🇷+France",
                pingMs = 61,
                isVip = true,
                isCustom = false,
                trafficLoad = com.example.model.ServerTrafficLoad.LOW,
                onlineUsers = 270,
                locationsCount = 5
            ),
            VpnConfigItem(
                id = "ch-01",
                name = "سوئیس",
                persianCountryName = "سوئیس",
                countryCode = "CH",
                flagEmoji = "🇨🇭",
                serverAddress = "ch1.reno-vpn.org",
                port = 443,
                protocol = VpnProtocol.VLESS,
                rawConfig = "vless://reno-ch-safe@ch1.reno-vpn.org:443#🇨🇭+Switzerland",
                pingMs = 48,
                isVip = true,
                isCustom = false,
                trafficLoad = com.example.model.ServerTrafficLoad.LOW,
                onlineUsers = 190,
                locationsCount = 2
            ),
            VpnConfigItem(
                id = "pl-01",
                name = "لهستان",
                persianCountryName = "لهستان",
                countryCode = "PL",
                flagEmoji = "🇵🇱",
                serverAddress = "waw1.reno-vpn.org",
                port = 443,
                protocol = VpnProtocol.VLESS,
                rawConfig = "vless://reno-pl-fast@waw1.reno-vpn.org:443#🇵🇱+Poland",
                pingMs = 55,
                isVip = true,
                isCustom = false,
                trafficLoad = com.example.model.ServerTrafficLoad.MEDIUM,
                onlineUsers = 220,
                locationsCount = 2
            ),
            VpnConfigItem(
                id = "gb-01",
                name = "بریتانیا",
                persianCountryName = "بریتانیا",
                countryCode = "GB",
                flagEmoji = "🇬🇧",
                serverAddress = "lon1.reno-vpn.org",
                port = 443,
                protocol = VpnProtocol.HYSTERIA2,
                rawConfig = "hysteria2://reno-uk-gaming@lon1.reno-vpn.org:443#🇬🇧+UK",
                pingMs = 52,
                isVip = true,
                isCustom = false,
                trafficLoad = com.example.model.ServerTrafficLoad.HIGH,
                onlineUsers = 2480,
                locationsCount = 2
            ),
            VpnConfigItem(
                id = "it-01",
                name = "ایتالیا",
                persianCountryName = "ایتالیا",
                countryCode = "IT",
                flagEmoji = "🇮🇹",
                serverAddress = "mil1.reno-vpn.org",
                port = 443,
                protocol = VpnProtocol.VLESS,
                rawConfig = "vless://reno-it-fast@mil1.reno-vpn.org:443#🇮🇹+Italy",
                pingMs = 50,
                isVip = true,
                isCustom = false,
                trafficLoad = com.example.model.ServerTrafficLoad.LOW,
                onlineUsers = 310,
                locationsCount = 3
            ),
            VpnConfigItem(
                id = "cz-01",
                name = "چک",
                persianCountryName = "چک",
                countryCode = "CZ",
                flagEmoji = "🇨🇿",
                serverAddress = "prg1.reno-vpn.org",
                port = 443,
                protocol = VpnProtocol.VLESS,
                rawConfig = "vless://reno-cz-fast@prg1.reno-vpn.org:443#🇨🇿+Czech",
                pingMs = 49,
                isVip = true,
                isCustom = false,
                trafficLoad = com.example.model.ServerTrafficLoad.LOW,
                onlineUsers = 180,
                locationsCount = 2
            ),
            VpnConfigItem(
                id = "de-01",
                name = "آلمان",
                persianCountryName = "آلمان",
                countryCode = "DE",
                flagEmoji = "🇩🇪",
                serverAddress = "fra1.reno-vpn.org",
                port = 443,
                protocol = VpnProtocol.VLESS,
                rawConfig = "vless://reno-de-fast@fra1.reno-vpn.org:443?security=tls#🇩🇪+Germany",
                pingMs = 42,
                isVip = true,
                isCustom = false,
                trafficLoad = com.example.model.ServerTrafficLoad.LOW,
                onlineUsers = 410,
                locationsCount = 4
            ),
            VpnConfigItem(
                id = "tr-01",
                name = "ترکیه",
                persianCountryName = "ترکیه",
                countryCode = "TR",
                flagEmoji = "🇹🇷",
                serverAddress = "ist1.reno-vpn.org",
                port = 443,
                protocol = VpnProtocol.TROJAN,
                rawConfig = "trojan://reno-tr-lowping@ist1.reno-vpn.org:443#🇹🇷+Turkey",
                pingMs = 38,
                isVip = true,
                isCustom = false,
                trafficLoad = com.example.model.ServerTrafficLoad.LOW,
                onlineUsers = 530,
                locationsCount = 3
            ),
            VpnConfigItem(
                id = "ae-01",
                name = "امارات",
                persianCountryName = "امارات",
                countryCode = "AE",
                flagEmoji = "🇦🇪",
                serverAddress = "dxb1.reno-vpn.org",
                port = 443,
                protocol = VpnProtocol.HYSTERIA2,
                rawConfig = "hysteria2://reno-ae-dubai@dxb1.reno-vpn.org:443#🇦🇪+UAE",
                pingMs = 40,
                isVip = true,
                isCustom = false,
                trafficLoad = com.example.model.ServerTrafficLoad.MEDIUM,
                onlineUsers = 950,
                locationsCount = 2
            )
        )

        const val TELEGRAM_CHANNEL_URL = "https://t.me/Reno_VpN_1"
        const val TELEGRAM_CHANNEL_ID = "@Reno_VpN_1"

        private val defaultInitialBanners = listOf(
            PromoBannerItem(
                id = "banner-vip-servers",
                title = "سرورهای VIP ارتقا یافتند",
                subtitle = "اتصال آسان‌تر، پینگ فوق‌العاده پایین و سرعت چند برابر بالاتر",
                buttonText = "خرید اشتراک VIP",
                buttonLink = "https://t.me/Reno_VpN_1",
                badgeText = "VIP اختصاصی",
                themeColorIndex = 0
            ),
            PromoBannerItem(
                id = "banner-crypto-gift",
                title = "هدیه خوش‌آمدگویی و ترید",
                subtitle = "تا 10,000 USDT پاداش ویژه ثبت‌نام، واریز و دریافت پاداش مرحله‌ای",
                buttonText = "همین حالا شروع کنید",
                buttonLink = "https://t.me/Reno_VpN_1",
                badgeText = "ویژه کاربران جدید",
                themeColorIndex = 3
            ),
            PromoBannerItem(
                id = "banner-video-downloader",
                title = "دانلودکننده همه‌کاره ویدئو",
                subtitle = "رایگان، بدون تبلیغات و دانلود با بالاترین کیفیت ممکن از تمام شبکه‌ها",
                buttonText = "دانلود مستقیم برنامه",
                buttonLink = "https://t.me/Reno_VpN_1",
                badgeText = "رایگان",
                themeColorIndex = 1
            )
        )
    }

    init {
        loadSettings()
        loadConfigs()
        loadBanners()
    }

    private fun loadSettings() {
        val langCode = prefs.getString("app_lang", "fa") ?: "fa"
        _language.value = if (langCode == "en") AppLanguage.EN else AppLanguage.FA
        _killSwitchEnabled.value = prefs.getBoolean("kill_switch", false)
        _splitTunnelingEnabled.value = prefs.getBoolean("split_tunneling", true)
        _autoConnectEnabled.value = prefs.getBoolean("auto_connect", false)
        _hideBannerAds.value = prefs.getBoolean("hide_banner_ads", false)
        _routingMode.value = prefs.getString("routing_mode", "global") ?: "global"
        _adminPin.value = prefs.getString("admin_pin", "1234") ?: "1234"

        val dnsId = prefs.getString("dns_id", "cloudflare")
        _selectedDns.value = dnsOptions.find { it.id == dnsId } ?: dnsOptions[0]
    }

    private fun loadConfigs() {
        val jsonStr = prefs.getString("vpn_configs_json", null)
        val loadedList = mutableListOf<VpnConfigItem>()

        if (!jsonStr.isNullOrEmpty()) {
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    loadedList.add(
                        VpnConfigItem(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            persianCountryName = obj.optString("persianCountryName", obj.getString("name")),
                            countryCode = obj.optString("countryCode", "UN"),
                            flagEmoji = obj.optString("flagEmoji", "🌐"),
                            serverAddress = obj.optString("serverAddress", "127.0.0.1"),
                            port = obj.optInt("port", 443),
                            protocol = try {
                                VpnProtocol.valueOf(obj.optString("protocol", "VLESS"))
                            } catch (e: Exception) {
                                VpnProtocol.VLESS
                            },
                            rawConfig = obj.optString("rawConfig", ""),
                            pingMs = obj.optInt("pingMs", 50),
                            trafficLoad = try {
                                com.example.model.ServerTrafficLoad.valueOf(obj.optString("trafficLoad", "LOW"))
                            } catch (e: Exception) {
                                com.example.model.ServerTrafficLoad.LOW
                            },
                            onlineUsers = obj.optInt("onlineUsers", (100..450).random()),
                            locationsCount = obj.optInt("locationsCount", 1),
                            isFavorite = obj.optBoolean("isFavorite", false),
                            isVip = obj.optBoolean("isVip", false),
                            isCustom = obj.optBoolean("isCustom", false),
                            isEnabled = obj.optBoolean("isEnabled", true),
                            addedAt = obj.optLong("addedAt", System.currentTimeMillis()),
                            expiresAt = if (obj.has("expiresAt") && !obj.isNull("expiresAt")) obj.getLong("expiresAt") else null,
                            remark = obj.optString("remark", "")
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (loadedList.isEmpty()) {
            loadedList.addAll(defaultInitialConfigs)
            saveConfigsList(loadedList)
        }

        _configs.value = loadedList
        val selectedId = prefs.getString("selected_config_id", null)
        _selectedConfig.value = loadedList.find { it.id == selectedId } ?: loadedList.firstOrNull()
    }

    private fun saveConfigsList(list: List<VpnConfigItem>) {
        try {
            val array = JSONArray()
            list.forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("name", item.name)
                    put("persianCountryName", item.persianCountryName)
                    put("countryCode", item.countryCode)
                    put("flagEmoji", item.flagEmoji)
                    put("serverAddress", item.serverAddress)
                    put("port", item.port)
                    put("protocol", item.protocol.name)
                    put("rawConfig", item.rawConfig)
                    put("pingMs", item.pingMs)
                    put("trafficLoad", item.trafficLoad.name)
                    put("onlineUsers", item.onlineUsers)
                    put("locationsCount", item.locationsCount)
                    put("isFavorite", item.isFavorite)
                    put("isVip", item.isVip)
                    put("isCustom", item.isCustom)
                    put("isEnabled", item.isEnabled)
                    put("addedAt", item.addedAt)
                    if (item.expiresAt != null) put("expiresAt", item.expiresAt)
                    put("remark", item.remark)
                }
                array.put(obj)
            }
            prefs.edit().putString("vpn_configs_json", array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleFavorite(configId: String) {
        val updated = _configs.value.map {
            if (it.id == configId) it.copy(isFavorite = !it.isFavorite) else it
        }
        _configs.value = updated
        saveConfigsList(updated)
    }

    fun setHideBannerAds(hide: Boolean) {
        _hideBannerAds.value = hide
        prefs.edit().putBoolean("hide_banner_ads", hide).apply()
    }

    fun setRoutingMode(mode: String) {
        _routingMode.value = mode
        prefs.edit().putString("routing_mode", mode).apply()
    }

    fun resetToDefaults() {
        _killSwitchEnabled.value = false
        _splitTunnelingEnabled.value = true
        _autoConnectEnabled.value = false
        _hideBannerAds.value = false
        _routingMode.value = "global"
        _configs.value = defaultInitialConfigs
        _selectedConfig.value = defaultInitialConfigs.first()
        saveConfigsList(defaultInitialConfigs)
        prefs.edit()
            .putBoolean("kill_switch", false)
            .putBoolean("split_tunneling", true)
            .putBoolean("auto_connect", false)
            .putBoolean("hide_banner_ads", false)
            .putString("routing_mode", "global")
            .putString("selected_config_id", defaultInitialConfigs.first().id)
            .apply()
    }

    fun cycleAutoSelectLoad() {
        val currentList = _configs.value
        val updated = currentList.map { item ->
            if (item.id == "auto-optimal") {
                val nextLoad = when (item.trafficLoad) {
                    com.example.model.ServerTrafficLoad.LOW -> com.example.model.ServerTrafficLoad.MEDIUM
                    com.example.model.ServerTrafficLoad.MEDIUM -> com.example.model.ServerTrafficLoad.HIGH
                    com.example.model.ServerTrafficLoad.HIGH -> com.example.model.ServerTrafficLoad.LOW
                }
                val newUsers = when (nextLoad) {
                    com.example.model.ServerTrafficLoad.LOW -> (110..180).random()
                    com.example.model.ServerTrafficLoad.MEDIUM -> (750..920).random()
                    com.example.model.ServerTrafficLoad.HIGH -> (2100..3200).random()
                }
                item.copy(trafficLoad = nextLoad, onlineUsers = newUsers)
            } else item
        }
        _configs.value = updated
        saveConfigsList(updated)
        _selectedConfig.value?.let { current ->
            _selectedConfig.value = updated.find { it.id == current.id }
        }
    }

    fun selectConfig(config: VpnConfigItem) {
        _selectedConfig.value = config
        prefs.edit().putString("selected_config_id", config.id).apply()
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
        prefs.edit().putString("app_lang", lang.code).apply()
    }

    fun setKillSwitch(enabled: Boolean) {
        _killSwitchEnabled.value = enabled
        prefs.edit().putBoolean("kill_switch", enabled).apply()
    }

    fun setSplitTunneling(enabled: Boolean) {
        _splitTunnelingEnabled.value = enabled
        prefs.edit().putBoolean("split_tunneling", enabled).apply()
    }

    fun setAutoConnect(enabled: Boolean) {
        _autoConnectEnabled.value = enabled
        prefs.edit().putBoolean("auto_connect", enabled).apply()
    }

    fun setSelectedDns(dns: DnsOption) {
        _selectedDns.value = dns
        prefs.edit().putString("dns_id", dns.id).apply()
    }

    fun setAdminPin(newPin: String): Boolean {
        if (newPin.length in 4..8) {
            _adminPin.value = newPin
            prefs.edit().putString("admin_pin", newPin).apply()
            return true
        }
        return false
    }

    fun verifyAdminPin(pin: String): Boolean {
        return pin.trim() == _adminPin.value.trim()
    }

    fun addConfig(
        name: String,
        rawConfig: String,
        countryCode: String = "DE",
        flagEmoji: String = "🇩🇪",
        protocol: VpnProtocol = VpnProtocol.VLESS,
        daysValid: Int? = null,
        remark: String = ""
    ): VpnConfigItem {
        val parsedHost = parseHostFromRaw(rawConfig)
        val parsedPort = parsePortFromRaw(rawConfig)
        val expiresAt = daysValid?.let { System.currentTimeMillis() + it * 24L * 60L * 60L * 1000L }

        val newConfig = VpnConfigItem(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Custom Server ${System.currentTimeMillis() % 1000}" },
            countryCode = countryCode,
            flagEmoji = flagEmoji,
            serverAddress = parsedHost,
            port = parsedPort,
            protocol = detectProtocol(rawConfig, protocol),
            rawConfig = rawConfig,
            pingMs = (35..120).random(),
            isVip = true,
            isCustom = true,
            isEnabled = true,
            addedAt = System.currentTimeMillis(),
            expiresAt = expiresAt,
            remark = remark
        )

        val updated = listOf(newConfig) + _configs.value
        _configs.value = updated
        saveConfigsList(updated)
        selectConfig(newConfig)
        return newConfig
    }

    fun importBatchConfigs(rawText: String): Int {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
        var count = 0
        val newItems = mutableListOf<VpnConfigItem>()

        for (line in lines) {
            if (line.startsWith("vless://") || line.startsWith("vmess://") ||
                line.startsWith("ss://") || line.startsWith("trojan://") ||
                line.startsWith("hysteria2://") || line.startsWith("wireguard://")
            ) {
                val protocol = detectProtocol(line, VpnProtocol.VLESS)
                val flagAndCountry = detectCountryAndFlag(line)
                val host = parseHostFromRaw(line)
                val port = parsePortFromRaw(line)
                val configName = parseNameFromRaw(line) ?: "Imported ${protocol.displayName} #${count + 1}"

                newItems.add(
                    VpnConfigItem(
                        id = UUID.randomUUID().toString(),
                        name = configName,
                        countryCode = flagAndCountry.first,
                        flagEmoji = flagAndCountry.second,
                        serverAddress = host,
                        port = port,
                        protocol = protocol,
                        rawConfig = line,
                        pingMs = (40..130).random(),
                        isVip = true,
                        isCustom = true,
                        isEnabled = true
                    )
                )
                count++
            }
        }

        if (newItems.isNotEmpty()) {
            val updated = newItems + _configs.value
            _configs.value = updated
            saveConfigsList(updated)
            selectConfig(newItems.first())
        }
        return count
    }

    fun deleteConfig(configId: String) {
        val updated = _configs.value.filter { it.id != configId }
        _configs.value = updated
        saveConfigsList(updated)
        if (_selectedConfig.value?.id == configId) {
            _selectedConfig.value = updated.firstOrNull()
        }
    }

    fun updateConfig(config: VpnConfigItem) {
        val updated = _configs.value.map { if (it.id == config.id) config else it }
        _configs.value = updated
        saveConfigsList(updated)
        if (_selectedConfig.value?.id == config.id) {
            _selectedConfig.value = config
        }
    }

    private fun loadBanners() {
        val jsonStr = prefs.getString("promo_banners_json", null)
        val loadedList = mutableListOf<PromoBannerItem>()

        if (!jsonStr.isNullOrEmpty()) {
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    loadedList.add(
                        PromoBannerItem(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            subtitle = obj.optString("subtitle", ""),
                            buttonText = obj.optString("buttonText", "دانلود مستقیم"),
                            buttonLink = obj.optString("buttonLink", ""),
                            badgeText = obj.optString("badgeText", "ویژه"),
                            themeColorIndex = obj.optInt("themeColorIndex", 0),
                            isEnabled = obj.optBoolean("isEnabled", true),
                            addedAt = obj.optLong("addedAt", System.currentTimeMillis())
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (loadedList.isEmpty()) {
            loadedList.addAll(defaultInitialBanners)
            saveBannersList(loadedList)
        }

        _banners.value = loadedList
    }

    private fun saveBannersList(list: List<PromoBannerItem>) {
        try {
            val array = JSONArray()
            list.forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("subtitle", item.subtitle)
                    put("buttonText", item.buttonText)
                    put("buttonLink", item.buttonLink)
                    put("badgeText", item.badgeText)
                    put("themeColorIndex", item.themeColorIndex)
                    put("isEnabled", item.isEnabled)
                    put("addedAt", item.addedAt)
                }
                array.put(obj)
            }
            prefs.edit().putString("promo_banners_json", array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addBanner(
        title: String,
        subtitle: String,
        buttonText: String = "مشاهده",
        buttonLink: String = "",
        badgeText: String = "ویژه",
        themeColorIndex: Int = 0
    ): PromoBannerItem {
        val newBanner = PromoBannerItem(
            id = UUID.randomUUID().toString(),
            title = title.ifBlank { "تبلیغ جدید ReNo" },
            subtitle = subtitle,
            buttonText = buttonText.ifBlank { "مشاهده" },
            buttonLink = buttonLink,
            badgeText = badgeText.ifBlank { "ویژه" },
            themeColorIndex = themeColorIndex,
            isEnabled = true,
            addedAt = System.currentTimeMillis()
        )
        val updated = listOf(newBanner) + _banners.value
        _banners.value = updated
        saveBannersList(updated)
        return newBanner
    }

    fun updateBanner(banner: PromoBannerItem) {
        val updated = _banners.value.map { if (it.id == banner.id) banner else it }
        _banners.value = updated
        saveBannersList(updated)
    }

    fun deleteBanner(bannerId: String) {
        val updated = _banners.value.filter { it.id != bannerId }
        _banners.value = updated
        saveBannersList(updated)
    }

    fun toggleBannerEnabled(bannerId: String) {
        val updated = _banners.value.map {
            if (it.id == bannerId) it.copy(isEnabled = !it.isEnabled) else it
        }
        _banners.value = updated
        saveBannersList(updated)
    }

    fun restoreDefaultBanners() {
        _banners.value = defaultInitialBanners
        saveBannersList(defaultInitialBanners)
    }

    fun restoreDefaults() {
        _configs.value = defaultInitialConfigs
        saveConfigsList(defaultInitialConfigs)
        selectConfig(defaultInitialConfigs.first())
    }

    fun updateAllPings() {
        val updated = _configs.value.map {
            it.copy(pingMs = (28..120).random())
        }
        _configs.value = updated
        saveConfigsList(updated)
        _selectedConfig.value?.let { current ->
            _selectedConfig.value = updated.find { it.id == current.id }
        }
    }

    private fun detectProtocol(raw: String, fallback: VpnProtocol): VpnProtocol {
        return when {
            raw.startsWith("vless://", ignoreCase = true) -> VpnProtocol.VLESS
            raw.startsWith("vmess://", ignoreCase = true) -> VpnProtocol.VMESS
            raw.startsWith("ss://", ignoreCase = true) -> VpnProtocol.SHADOWSOCKS
            raw.startsWith("trojan://", ignoreCase = true) -> VpnProtocol.TROJAN
            raw.startsWith("hysteria2://", ignoreCase = true) || raw.startsWith("hy2://", ignoreCase = true) -> VpnProtocol.HYSTERIA2
            raw.startsWith("wireguard://", ignoreCase = true) || raw.startsWith("wg://", ignoreCase = true) -> VpnProtocol.WIREGUARD
            else -> fallback
        }
    }

    private fun parseHostFromRaw(raw: String): String {
        return try {
            if (raw.contains("@")) {
                val afterAt = raw.substringAfter("@")
                afterAt.substringBefore(":").substringBefore("?").substringBefore("/")
            } else if (raw.contains("://")) {
                val afterProto = raw.substringAfter("://")
                afterProto.substringBefore(":").substringBefore("?").substringBefore("/")
            } else {
                "custom.server.net"
            }
        } catch (e: Exception) {
            "node.jump-vpn.net"
        }
    }

    private fun parsePortFromRaw(raw: String): Int {
        return try {
            if (raw.contains("@")) {
                val afterAt = raw.substringAfter("@")
                val portStr = afterAt.substringAfter(":").substringBefore("?").substringBefore("/").substringBefore("#")
                portStr.toIntOrNull() ?: 443
            } else {
                443
            }
        } catch (e: Exception) {
            443
        }
    }

    private fun parseNameFromRaw(raw: String): String? {
        return try {
            if (raw.contains("#")) {
                java.net.URLDecoder.decode(raw.substringAfter("#"), "UTF-8")
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun detectCountryAndFlag(raw: String): Pair<String, String> {
        val lower = raw.lowercase()
        return when {
            "germany" in lower || "de" in lower || "frankfurt" in lower || "🇩🇪" in raw -> "DE" to "🇩🇪"
            "netherlands" in lower || "nl" in lower || "amsterdam" in lower || "🇳🇱" in raw -> "NL" to "🇳🇱"
            "finland" in lower || "fi" in lower || "helsinki" in lower || "🇫🇮" in raw -> "FI" to "🇫🇮"
            "turkey" in lower || "tr" in lower || "istanbul" in lower || "🇹🇷" in raw -> "TR" to "🇹🇷"
            "usa" in lower || "us" in lower || "america" in lower || "united states" in lower || "🇺🇸" in raw -> "US" to "🇺🇸"
            "uk" in lower || "gb" in lower || "london" in lower || "britain" in lower || "🇬🇧" in raw -> "GB" to "🇬🇧"
            "france" in lower || "fr" in lower || "paris" in lower || "🇫🇷" in raw -> "FR" to "🇫🇷"
            "canada" in lower || "ca" in lower || "toronto" in lower || "🇨🇦" in raw -> "CA" to "🇨🇦"
            "singapore" in lower || "sg" in lower || "🇸🇬" in raw -> "SG" to "🇸🇬"
            "switzerland" in lower || "ch" in lower || "zurich" in lower || "🇨🇭" in raw -> "CH" to "🇨🇭"
            "uae" in lower || "dubai" in lower || "🇦🇪" in raw -> "AE" to "🇦🇪"
            else -> "UN" to "⚡"
        }
    }
}
