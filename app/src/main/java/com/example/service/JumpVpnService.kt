package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.model.ConnectionStats
import com.example.model.VpnState
import com.example.vpn.ReNoPlatformInterface
import com.example.vpn.SingBoxConfigFactory
import io.nekohasekai.libbox.CommandServer
import io.nekohasekai.libbox.CommandServerHandler
import io.nekohasekai.libbox.OverrideOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.lang.reflect.Proxy
import android.net.TrafficStats

class JumpVpnService : VpnService() {
    private var core: CommandServer? = null
    private var platform: ReNoPlatformInterface? = null
    private var statsJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var lastRx = TrafficStats.getUidRxBytes(android.os.Process.myUid())
    private var lastTx = TrafficStats.getUidTxBytes(android.os.Process.myUid())
    private var totalRx = 0L
    private var totalTx = 0L

    companion object {
        private const val TAG = "ReNoVpnService"
        const val ACTION_CONNECT = "com.example.jumpvpn.CONNECT"
        const val ACTION_DISCONNECT = "com.example.jumpvpn.DISCONNECT"
        const val EXTRA_SERVER_NAME = "extra_server_name"
        const val EXTRA_SERVER_IP = "extra_server_ip"
        const val EXTRA_DNS_IP = "extra_dns_ip"
        const val EXTRA_RAW_CONFIG = "extra_raw_config"
        const val MAX_SESSION_DURATION_SECONDS = 3L * 3600L
        private const val CHANNEL_ID = "reno_vpn_status_channel"
        private const val NOTIFICATION_ID = 1001

        private val _vpnState = MutableStateFlow(VpnState.DISCONNECTED)
        val vpnState: StateFlow<VpnState> = _vpnState.asStateFlow()
        private val _connectionStats = MutableStateFlow(ConnectionStats())
        val connectionStats: StateFlow<ConnectionStats> = _connectionStats.asStateFlow()
        private val _sessionEvent = MutableSharedFlow<String>(extraBufferCapacity = 5)
        val sessionEvent: SharedFlow<String> = _sessionEvent.asSharedFlow()

        fun startVpn(context: Context, serverName: String, serverIp: String, dnsIp: String = "1.1.1.1", rawConfig: String = "") {
            val intent = Intent(context, JumpVpnService::class.java).apply {
                action = ACTION_CONNECT
                putExtra(EXTRA_SERVER_NAME, serverName)
                putExtra(EXTRA_SERVER_IP, serverIp)
                putExtra(EXTRA_DNS_IP, dnsIp)
                putExtra(EXTRA_RAW_CONFIG, rawConfig)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent) else context.startService(intent)
        }

        fun stopVpn(context: Context) {
            context.startService(Intent(context, JumpVpnService::class.java).apply { action = ACTION_DISCONNECT })
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        safeStartForeground("ReNo VPN", "در حال پردازش اتصال...")
        when (intent?.action) {
            ACTION_CONNECT -> connectVpn(
                intent.getStringExtra(EXTRA_SERVER_NAME) ?: "Server",
                intent.getStringExtra(EXTRA_SERVER_IP) ?: "",
                intent.getStringExtra(EXTRA_DNS_IP) ?: "1.1.1.1",
                intent.getStringExtra(EXTRA_RAW_CONFIG).orEmpty()
            )
            ACTION_DISCONNECT -> disconnectVpn("قطع اتصال توسط کاربر")
        }
        return START_NOT_STICKY
    }

    private fun connectVpn(serverName: String, serverIp: String, dnsIp: String, rawConfig: String) {
        if (_vpnState.value == VpnState.CONNECTED || _vpnState.value == VpnState.CONNECTING) return
        _vpnState.value = VpnState.CONNECTING
        updateNotification("ReNo VPN: در حال اتصال...", serverName)
        serviceScope.launch {
            try {
                if (VpnService.prepare(this@JumpVpnService) != null) error("مجوز VPN صادر نشده است")
                val config = SingBoxConfigFactory.build(rawConfig, dnsIp)
                val handler = commandHandlerProxy()
                platform = ReNoPlatformInterface(this@JumpVpnService)
                core = CommandServer(handler, platform!!)
                core!!.start()
                core!!.startOrReloadService(config, OverrideOptions())
                _vpnState.value = VpnState.CONNECTED
                lastRx = TrafficStats.getUidRxBytes(android.os.Process.myUid())
                lastTx = TrafficStats.getUidTxBytes(android.os.Process.myUid())
                totalRx = 0L
                totalTx = 0L
                updateNotification("ReNo VPN: متصل شد ✓", "سرور $serverName فعال است")
                startStatsEngine(serverName, serverIp, dnsIp)
            } catch (e: Exception) {
                Log.e(TAG, "sing-box start failed", e)
                _vpnState.value = VpnState.DISCONNECTED
                _sessionEvent.tryEmit("اتصال برقرار نشد: ${e.message ?: "خطای ناشناخته"}")
                closeCore()
                safeStopForeground()
                stopSelf()
            }
        }
    }

    private fun commandHandlerProxy(): CommandServerHandler {
        val iface = CommandServerHandler::class.java
        return Proxy.newProxyInstance(iface.classLoader, arrayOf(iface)) { _, method, args ->
            when (method.name) {
                "serviceStop" -> { serviceScope.launch { disconnectVpn("هسته VPN اتصال را متوقف کرد") }; null }
                "serviceReload" -> { null }
                "getSystemProxyStatus" -> null
                "setSystemProxyEnabled" -> null
                "writeDebugMessage" -> { Log.d(TAG, args?.firstOrNull()?.toString().orEmpty()); null }
                "sendNotification" -> null
                else -> defaultValue(method.returnType)
            }
        } as CommandServerHandler
    }

    private fun defaultValue(type: Class<*>): Any? = when {
        type == java.lang.Boolean.TYPE -> false
        type == java.lang.Integer.TYPE -> 0
        type == java.lang.Long.TYPE -> 0L
        type == java.lang.Float.TYPE -> 0f
        type == java.lang.Double.TYPE -> 0.0
        type == java.lang.Short.TYPE -> 0.toShort()
        type == java.lang.Byte.TYPE -> 0.toByte()
        type == java.lang.Character.TYPE -> '\u0000'
        else -> null
    }

    private fun startStatsEngine(serverName: String, serverIp: String, dnsIp: String) {
        statsJob?.cancel()
        statsJob = serviceScope.launch {
            var duration = 0L
            while (isActive && _vpnState.value == VpnState.CONNECTED) {
                delay(1000)
                duration++
                if (duration >= MAX_SESSION_DURATION_SECONDS) {
                    _sessionEvent.emit("مدت زمان نشست ۳ ساعته به پایان رسید.")
                    disconnectVpn("پایان زمان نشست")
                    break
                }
                val rx = TrafficStats.getUidRxBytes(android.os.Process.myUid())
                val tx = TrafficStats.getUidTxBytes(android.os.Process.myUid())
                val down = if (rx >= 0 && lastRx >= 0) (rx - lastRx).coerceAtLeast(0L) else 0L
                val up = if (tx >= 0 && lastTx >= 0) (tx - lastTx).coerceAtLeast(0L) else 0L
                if (rx >= 0) { totalRx += down; lastRx = rx }
                if (tx >= 0) { totalTx += up; lastTx = tx }
                _connectionStats.value = ConnectionStats(
                    uploadSpeedBps = up,
                    downloadSpeedBps = down,
                    totalUploadedBytes = totalTx,
                    totalDownloadedBytes = totalRx,
                    durationSeconds = duration,
                    currentPingMs = 0,
                    virtualIp = serverIp,
                    dnsServer = dnsIp
                )
                if (duration % 60L == 0L) updateNotification("ReNo VPN: متصل به $serverName", "ترافیک واقعی: ↓ ${formatBytes(down)}/s  ↑ ${formatBytes(up)}/s")
            }
        }
    }

    private fun disconnectVpn(reason: String) {
        if (_vpnState.value == VpnState.DISCONNECTED) return
        _vpnState.value = VpnState.DISCONNECTING
        statsJob?.cancel()
        closeCore()
        _vpnState.value = VpnState.DISCONNECTED
        _connectionStats.value = ConnectionStats()
        safeStopForeground()
        stopSelf()
    }

    private fun closeCore() {
        runCatching { core?.close() }
        core = null
        runCatching { platform?.closeTun() }
        platform = null
    }

    override fun onDestroy() {
        statsJob?.cancel()
        closeCore()
        _vpnState.value = VpnState.DISCONNECTED
        super.onDestroy()
    }

    private fun formatBytes(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
        bytes < 1024L * 1024L * 1024L -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
        else -> "%.2f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
    }

    private fun safeStartForeground(title: String, content: String) = runCatching {
        startForeground(NOTIFICATION_ID, buildNotification(title, content))
    }
    private fun safeStopForeground() = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) stopForeground(STOP_FOREGROUND_REMOVE) else @Suppress("DEPRECATION") stopForeground(true)
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "ReNo VPN Service", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }
    private fun buildNotification(title: String, content: String): Notification {
        val openIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title).setContentText(content)
            .setSmallIcon(android.R.drawable.ic_lock_lock).setContentIntent(openIntent).setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW).build()
    }
    private fun updateNotification(title: String, content: String) = runCatching {
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(NOTIFICATION_ID, buildNotification(title, content))
    }
}
