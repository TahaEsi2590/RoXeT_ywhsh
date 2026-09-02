package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.VpnRepository
import com.example.localization.AppStrings
import com.example.model.AppLanguage
import com.example.model.ConnectionStats
import com.example.model.DnsOption
import com.example.model.PromoBannerItem
import com.example.model.VpnConfigItem
import com.example.model.VpnProtocol
import com.example.model.VpnState
import com.example.service.JumpVpnService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SpeedChartPoint(val timestamp: Long, val downloadMbps: Float, val uploadMbps: Float)

class VpnViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VpnRepository(application)

    val configs: StateFlow<List<VpnConfigItem>> = repository.configs
    val selectedConfig: StateFlow<VpnConfigItem?> = repository.selectedConfig
    val banners: StateFlow<List<PromoBannerItem>> = repository.banners
    val language: StateFlow<AppLanguage> = repository.language
    val killSwitchEnabled: StateFlow<Boolean> = repository.killSwitchEnabled
    val splitTunnelingEnabled: StateFlow<Boolean> = repository.splitTunnelingEnabled
    val autoConnectEnabled: StateFlow<Boolean> = repository.autoConnectEnabled
    val hideBannerAds: StateFlow<Boolean> = repository.hideBannerAds
    val routingMode: StateFlow<String> = repository.routingMode
    val selectedDns: StateFlow<DnsOption> = repository.selectedDns

    val vpnState: StateFlow<VpnState> = JumpVpnService.vpnState
    val connectionStats: StateFlow<ConnectionStats> = JumpVpnService.connectionStats

    private val _speedHistory = MutableStateFlow<List<SpeedChartPoint>>(
        listOf(
            SpeedChartPoint(1, 0f, 0f),
            SpeedChartPoint(2, 0f, 0f),
            SpeedChartPoint(3, 0f, 0f),
            SpeedChartPoint(4, 0f, 0f),
            SpeedChartPoint(5, 0f, 0f)
        )
    )
    val speedHistory: StateFlow<List<SpeedChartPoint>> = _speedHistory.asStateFlow()

    // Admin UI State
    private val _isAdminUnlocked = MutableStateFlow(false)
    val isAdminUnlocked: StateFlow<Boolean> = _isAdminUnlocked.asStateFlow()

    private val _showAdminPinDialog = MutableStateFlow(false)
    val showAdminPinDialog: StateFlow<Boolean> = _showAdminPinDialog.asStateFlow()

    private val _adminPinError = MutableStateFlow<String?>(null)
    val adminPinError: StateFlow<String?> = _adminPinError.asStateFlow()

    private val _secretTapCount = MutableStateFlow(0)
    val secretTapCount: StateFlow<Int> = _secretTapCount.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        // Collect live stats to update chart
        viewModelScope.launch {
            connectionStats.collect { stats ->
                val downMbps = (stats.downloadSpeedBps * 8f) / (1024f * 1024f)
                val upMbps = (stats.uploadSpeedBps * 8f) / (1024f * 1024f)
                val currentList = _speedHistory.value.takeLast(14).toMutableList()
                currentList.add(SpeedChartPoint(System.currentTimeMillis(), downMbps, upMbps))
                _speedHistory.value = currentList
            }
        }

        // Collect session events (e.g. 3-hour auto-disconnect)
        viewModelScope.launch {
            JumpVpnService.sessionEvent.collect { message ->
                _toastMessage.value = message
            }
        }
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun getString(key: String): String {
        return AppStrings.get(key, language.value)
    }

    fun toggleVpnConnection(context: Context) {
        try {
            when (vpnState.value) {
                VpnState.DISCONNECTED -> {
                    val current = selectedConfig.value ?: configs.value.firstOrNull()
                    if (current != null) {
                        JumpVpnService.startVpn(
                            context,
                            current.name,
                            current.serverAddress,
                            selectedDns.value.primaryIp,
                            current.rawConfig
                        )
                    } else {
                        _toastMessage.value = "هیچ سروری انتخاب نشده است"
                    }
                }
                VpnState.CONNECTED, VpnState.CONNECTING -> {
                    JumpVpnService.stopVpn(context)
                }
                VpnState.DISCONNECTING -> {}
            }
        } catch (e: Exception) {
            _toastMessage.value = "خطا در پردازش سرویس وی‌پی‌ان: ${e.localizedMessage ?: "مجدداً تلاش کنید"}"
        }
    }

    fun selectConfig(config: VpnConfigItem) {
        repository.selectConfig(config)
    }

    fun setLanguage(lang: AppLanguage) {
        repository.setLanguage(lang)
    }

    fun toggleKillSwitch(enabled: Boolean) {
        repository.setKillSwitch(enabled)
    }

    fun toggleSplitTunneling(enabled: Boolean) {
        repository.setSplitTunneling(enabled)
    }

    fun toggleAutoConnect(enabled: Boolean) {
        repository.setAutoConnect(enabled)
    }

    fun setSelectedDns(dns: DnsOption) {
        repository.setSelectedDns(dns)
    }

    // Secret Tap Trigger
    fun onSecretLogoTap() {
        val count = _secretTapCount.value + 1
        _secretTapCount.value = count
        if (count >= 5) {
            _secretTapCount.value = 0
            _showAdminPinDialog.value = true
        }
    }

    fun openAdminPinDialog() {
        _adminPinError.value = null
        _showAdminPinDialog.value = true
    }

    fun closeAdminPinDialog() {
        _showAdminPinDialog.value = false
        _adminPinError.value = null
    }

    fun verifyAndUnlockAdmin(pin: String): Boolean {
        if (repository.verifyAdminPin(pin)) {
            _isAdminUnlocked.value = true
            _showAdminPinDialog.value = false
            _adminPinError.value = null
            _toastMessage.value = getString("secret_unlock_hint")
            return true
        } else {
            _adminPinError.value = getString("wrong_pin")
            return false
        }
    }

    fun lockAdmin() {
        _isAdminUnlocked.value = false
    }

    fun changeAdminPin(newPin: String): Boolean {
        val success = repository.setAdminPin(newPin)
        if (success) {
            _toastMessage.value = getString("pin_changed")
        }
        return success
    }

    fun addConfig(
        name: String,
        rawConfig: String,
        countryCode: String,
        flagEmoji: String,
        protocol: VpnProtocol,
        daysValid: Int?,
        remark: String
    ) {
        repository.addConfig(
            name = name,
            rawConfig = rawConfig,
            countryCode = countryCode,
            flagEmoji = flagEmoji,
            protocol = protocol,
            daysValid = daysValid,
            remark = remark
        )
        _toastMessage.value = getString("config_added_success")
    }

    fun importFromClipboard(rawText: String): Int {
        val count = repository.importBatchConfigs(rawText)
        if (count > 0) {
            _toastMessage.value = "${getString("config_added_success")} ($count)"
        }
        return count
    }

    fun deleteConfig(configId: String) {
        repository.deleteConfig(configId)
        _toastMessage.value = getString("config_deleted")
    }

    fun updateConfig(config: VpnConfigItem) {
        repository.updateConfig(config)
    }

    fun restoreDefaults() {
        repository.restoreDefaults()
    }

    fun toggleFavorite(configId: String) {
        repository.toggleFavorite(configId)
    }

    fun setHideBannerAds(hide: Boolean) {
        repository.setHideBannerAds(hide)
    }

    fun setRoutingMode(mode: String) {
        repository.setRoutingMode(mode)
    }

    fun resetToDefaults() {
        repository.resetToDefaults()
        _toastMessage.value = "تنظیمات به حالت پیش‌فرض بازگردانی شد"
    }

    fun cycleAutoSelectLoad() {
        repository.cycleAutoSelectLoad()
    }

    fun testAllPings() {
        repository.updateAllPings()
    }

    fun addBanner(
        title: String,
        subtitle: String,
        buttonText: String = "مشاهده",
        buttonLink: String = "",
        badgeText: String = "ویژه",
        themeColorIndex: Int = 0
    ) {
        repository.addBanner(
            title = title,
            subtitle = subtitle,
            buttonText = buttonText,
            buttonLink = buttonLink,
            badgeText = badgeText,
            themeColorIndex = themeColorIndex
        )
        _toastMessage.value = "تبلیغ جدید با موفقیت اضافه شد"
    }

    fun updateBanner(banner: PromoBannerItem) {
        repository.updateBanner(banner)
        _toastMessage.value = "تبلیغ با موفقیت ویرایش شد"
    }

    fun deleteBanner(bannerId: String) {
        repository.deleteBanner(bannerId)
        _toastMessage.value = "تبلیغ حذف شد"
    }

    fun toggleBannerEnabled(bannerId: String) {
        repository.toggleBannerEnabled(bannerId)
    }

    fun restoreDefaultBanners() {
        repository.restoreDefaultBanners()
        _toastMessage.value = "تبلیغات به حالت پیش‌فرض بازگردانی شد"
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
