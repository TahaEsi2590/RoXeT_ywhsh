package com.example.vpn

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.Process
import android.util.Log
import io.nekohasekai.libbox.ConnectionOwner
import io.nekohasekai.libbox.ExchangeContext
import io.nekohasekai.libbox.InterfaceUpdateListener
import io.nekohasekai.libbox.LocalDNSTransport
import io.nekohasekai.libbox.NetworkInterface as BoxNetworkInterface
import io.nekohasekai.libbox.NetworkInterfaceIterator
import io.nekohasekai.libbox.Notification
import io.nekohasekai.libbox.PlatformInterface
import io.nekohasekai.libbox.StringIterator
import io.nekohasekai.libbox.TunOptions
import io.nekohasekai.libbox.WIFIState
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface as JavaNetworkInterface
import java.net.UnknownHostException

class ReNoPlatformInterface(
    private val service: VpnService
) : PlatformInterface {

    private var tun: ParcelFileDescriptor? = null

    /**
     * sing-box asks Android to protect its outbound sockets
     * from being captured by the VPN TUN interface.
     */
    override fun usePlatformAutoDetectInterfaceControl(): Boolean {
        return true
    }

    override fun autoDetectInterfaceControl(fd: Int) {
        if (!service.protect(fd)) {
            throw IllegalStateException(
                "Unable to protect sing-box socket"
            )
        }
    }

    /**
     * Creates the Android VPN TUN interface.
     */
    override fun openTun(options: TunOptions): Int {
        if (VpnService.prepare(service) != null) {
            throw IllegalStateException(
                "VPN permission is not granted"
            )
        }

        val dns = runCatching {
            options.dnsServerAddress.value
        }.getOrDefault("1.1.1.1")

        val mtu = if (options.mtu > 0) {
            options.mtu
        } else {
            1500
        }

        runCatching {
            tun?.close()
        }

        val builder = service.Builder()
            .setSession("ReNo VPN")
            .setMtu(mtu)
            .addAddress("172.19.0.1", 30)
            .addRoute("0.0.0.0", 0)

        runCatching {
            builder.addDnsServer(dns)
        }

        tun = builder.establish()
            ?: throw IllegalStateException(
                "Unable to establish Android VPN interface"
            )

        return tun!!.fd
    }

    override fun writeLog(message: String) {
        Log.i("ReNoVPN", message)
    }

    override fun useProcFS(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
    }

    override fun findConnectionOwner(
        ipProtocol: Int,
        sourceAddress: String,
        sourcePort: Int,
        destinationAddress: String,
        destinationPort: Int
    ): ConnectionOwner {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            throw UnsupportedOperationException(
                "Connection owner lookup requires Android 10+"
            )
        }

        val connectivity =
            service.getSystemService(
                ConnectivityManager::class.java
            )

        val uid = connectivity.getConnectionOwnerUid(
            ipProtocol,
            InetSocketAddress(
                sourceAddress,
                sourcePort
            ),
            InetSocketAddress(
                destinationAddress,
                destinationPort
            )
        )

        if (uid == Process.INVALID_UID) {
            throw IllegalStateException(
                "Android connection owner not found"
            )
        }

        val packages =
            service.packageManager.getPackagesForUid(uid)
                ?.toList()
                ?: emptyList()

        return ConnectionOwner().apply {
            userId = uid
            userName = packages.firstOrNull() ?: ""
            setAndroidPackageNames(
                StringArray(packages)
            )
        }
    }

    override fun packageNameByUid(uid: Int): String {
        return service.packageManager
            .getPackagesForUid(uid)
            ?.firstOrNull()
            ?: ""
    }

    override fun uidByPackageName(packageName: String): Int {
        return service.packageManager
            .getApplicationInfo(
                packageName,
                0
            )
            .uid
    }

    override fun startDefaultInterfaceMonitor(
        listener: InterfaceUpdateListener
    ) {
        val connectivity =
            service.getSystemService(
                ConnectivityManager::class.java
            )

        runCatching {
            val network =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    connectivity.activeNetwork
                } else {
                    null
                }

            if (network != null) {
                val properties =
                    connectivity.getLinkProperties(network)

                val interfaceName =
                    properties?.interfaceName

                if (!interfaceName.isNullOrEmpty()) {
                    val index =
                        runCatching {
                            JavaNetworkInterface
                                .getByName(interfaceName)
                                ?.index
                                ?: -1
                        }.getOrDefault(-1)

                    listener.updateDefaultInterface(
                        interfaceName,
                        index,
                        false,
                        false
                    )
                }
            }
        }.onFailure {
            Log.w(
                "ReNoVPN",
                "Unable to detect default interface",
                it
            )
        }
    }

    override fun closeDefaultInterfaceMonitor(
        listener: InterfaceUpdateListener
    ) {
        // Nothing to unregister.
    }

    override fun getInterfaces(): NetworkInterfaceIterator {
        val connectivity =
            service.getSystemService(
                ConnectivityManager::class.java
            )

        val interfaces =
            mutableListOf<BoxNetworkInterface>()

        val javaInterfaces =
            runCatching {
                JavaNetworkInterface
                    .getNetworkInterfaces()
                    ?.toList()
                    ?: emptyList()
            }.getOrDefault(emptyList())

        val networks =
            runCatching {
                connectivity.allNetworks
                    .toList()
            }.getOrDefault(emptyList())

        for (network in networks) {
            val properties =
                runCatching {
                    connectivity.getLinkProperties(network)
                }.getOrNull()
                    ?: continue

            val capabilities =
                runCatching {
                    connectivity.getNetworkCapabilities(network)
                }.getOrNull()
                    ?: continue

            val name =
                properties.interfaceName
                    ?: continue

            val javaInterface =
                javaInterfaces.firstOrNull {
                    it.name == name
                } ?: continue

            val boxInterface =
                BoxNetworkInterface()

            boxInterface.name = name

            boxInterface.index =
                runCatching {
                    javaInterface.index
                }.getOrDefault(-1)

            boxInterface.mtu =
                runCatching {
                    javaInterface.mtu
                }.getOrDefault(1500)

            boxInterface.addresses =
                StringArray(
                    javaInterface.interfaceAddresses
                        .mapNotNull {
                            val address =
                                it.address.hostAddress
                                    ?: return@mapNotNull null

                            "$address/${it.networkPrefixLength}"
                        }
                )

            boxInterface.dnsServer =
                StringArray(
                    properties.dnsServers
                        .mapNotNull {
                            it.hostAddress
                        }
                )

            boxInterface.type =
                when {
                    capabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_WIFI
                    ) -> {
                        io.nekohasekai.libbox.Libbox
                            .InterfaceTypeWIFI
                    }

                    capabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_CELLULAR
                    ) -> {
                        io.nekohasekai.libbox.Libbox
                            .InterfaceTypeCellular
                    }

                    capabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_ETHERNET
                    ) -> {
                        io.nekohasekai.libbox.Libbox
                            .InterfaceTypeEthernet
                    }

                    else -> {
                        io.nekohasekai.libbox.Libbox
                            .InterfaceTypeOther
                    }
                }

            boxInterface.metered =
                !capabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_NOT_METERED
                )

            interfaces.add(boxInterface)
        }

        return InterfaceArray(interfaces)
    }

    override fun underNetworkExtension(): Boolean {
        return false
    }

    override fun includeAllNetworks(): Boolean {
        return false
    }

    override fun readWIFIState(): WIFIState? {
        return null
    }

    /**
     * IMPORTANT:
     * libbox 1.13.x expects a non-null LocalDNSTransport.
     */
    override fun localDNSTransport(): LocalDNSTransport {
        return ReNoLocalDnsTransport
    }

    override fun systemCertificates(): StringIterator {
        return StringArray(emptyList())
    }

    override fun clearDNSCache() {
        // sing-box manages its own DNS cache.
    }

    override fun sendNotification(
        notification: Notification
    ) {
        // No Android notification bridge is required here.
    }

    fun closeTun() {
        runCatching {
            tun?.close()
        }

        tun = null
    }

    /**
     * Implementation of libbox LocalDNSTransport.
     *
     * raw() = false means sing-box will use lookup()
     * instead of raw DNS packet exchange.
     */
    private object ReNoLocalDnsTransport : LocalDNSTransport {

        override fun raw(): Boolean {
            return false
        }

        override fun lookup(
            ctx: ExchangeContext,
            network: String,
            domain: String
        ) {
            try {
                val addresses =
                    InetAddress.getAllByName(domain)
                        .mapNotNull {
                            it.hostAddress
                        }
                        .joinToString("\n")

                if (addresses.isEmpty()) {
                    ctx.errorCode(3)
                    return
                }

                ctx.success(addresses)

            } catch (_: UnknownHostException) {
                // DNS RCODE NXDOMAIN
                ctx.errorCode(3)

            } catch (e: Exception) {
                Log.w(
                    "ReNoVPN",
                    "DNS lookup failed: $domain",
                    e
                )

                ctx.errorCode(2)
            }
        }

        override fun exchange(
            ctx: ExchangeContext,
            message: ByteArray
        ) {
            /*
             * raw() returns false, so sing-box should not use
             * this method for normal DNS resolution.
             */
            ctx.errorCode(2)
        }
    }

    private class StringArray(
        private val values: List<String>
    ) : StringIterator {

        private var index = 0

        override fun len(): Int {
            return values.size
        }

        override fun hasNext(): Boolean {
            return index < values.size
        }

        override fun next(): String {
            if (!hasNext()) {
                return ""
            }

            return values[index++]
        }
    }

    private class InterfaceArray(
        private val values: List<BoxNetworkInterface>
    ) : NetworkInterfaceIterator {

        private var index = 0

        override fun hasNext(): Boolean {
            return index < values.size
        }

        override fun next(): BoxNetworkInterface {
            return values[index++]
        }
    }
}
