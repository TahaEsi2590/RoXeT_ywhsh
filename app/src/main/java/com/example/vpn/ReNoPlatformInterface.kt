package com.example.vpn

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.VpnService
import android.os.Build
import android.os.Process
import io.nekohasekai.libbox.ConnectionOwner
import io.nekohasekai.libbox.InterfaceUpdateListener
import io.nekohasekai.libbox.LocalDNSTransport
import io.nekohasekai.libbox.NetworkInterface as BoxNetworkInterface
import io.nekohasekai.libbox.NetworkInterfaceIterator
import io.nekohasekai.libbox.Notification
import io.nekohasekai.libbox.PlatformInterface
import io.nekohasekai.libbox.StringIterator
import io.nekohasekai.libbox.TunOptions
import io.nekohasekai.libbox.WIFIState
import java.net.InetSocketAddress
import java.net.NetworkInterface

/**
 * Android platform bridge for sing-box libbox.
 */
class ReNoPlatformInterface(
    private val service: VpnService
) : PlatformInterface {

    private var tun: android.os.ParcelFileDescriptor? = null

    override fun usePlatformAutoDetectInterfaceControl(): Boolean = true

    override fun autoDetectInterfaceControl(fd: Int) {
        if (!service.protect(fd)) {
            error("Unable to protect sing-box socket")
        }
    }

    override fun openTun(options: TunOptions): Int {
        if (VpnService.prepare(service) != null) {
            error("VPN permission is not granted")
        }

        val dns = runCatching {
            options.dnsServerAddress.value
        }.getOrDefault("1.1.1.1")

        val mtu = if (options.mtu > 0) {
            options.mtu
        } else {
            1500
        }

        val builder = service.Builder()
            .setSession("ReNo VPN")
            .setMtu(mtu)
            .addAddress("172.19.0.1", 30)
            .addRoute("0.0.0.0", 0)
            .addDnsServer(dns)

        runCatching {
            tun?.close()
        }

        tun = builder.establish()
            ?: error("Unable to establish Android VPN interface")

        return tun!!.fd
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
            error("Connection owner lookup unavailable")
        }

        val cm = service.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        val uid = cm.getConnectionOwnerUid(
            ipProtocol,
            InetSocketAddress(sourceAddress, sourcePort),
            InetSocketAddress(destinationAddress, destinationPort)
        )

        if (uid == Process.INVALID_UID) {
            error("Connection owner not found")
        }

        val packages =
            service.packageManager.getPackagesForUid(uid)?.toList()
                ?: emptyList()

        val owner = ConnectionOwner()

        owner.userId = uid
        owner.userName = packages.firstOrNull() ?: ""

        owner.setAndroidPackageNames(
            StringArray(packages)
        )

        return owner
    }

    override fun startDefaultInterfaceMonitor(
        listener: InterfaceUpdateListener
    ) {
        val cm = service.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        val network = cm.activeNetwork

        val linkProperties = network?.let {
            cm.getLinkProperties(it)
        }

        val interfaceName =
            linkProperties?.interfaceName.orEmpty()

        val interfaceIndex = runCatching {
            NetworkInterface
                .getByName(interfaceName)
                ?.index
                ?: -1
        }.getOrDefault(-1)

        listener.updateDefaultInterface(
            interfaceName,
            interfaceIndex,
            false,
            false
        )
    }

    override fun closeDefaultInterfaceMonitor(
        listener: InterfaceUpdateListener
    ) {
        // Nothing to close.
    }

    override fun getInterfaces(): NetworkInterfaceIterator {
        val cm = service.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        val result = mutableListOf<BoxNetworkInterface>()

        val allInterfaces = runCatching {
            NetworkInterface
                .getNetworkInterfaces()
                ?.toList()
                ?: emptyList()
        }.getOrDefault(emptyList())

        for (network in cm.allNetworks) {

            val linkProperties =
                cm.getLinkProperties(network)
                    ?: continue

            val capabilities =
                cm.getNetworkCapabilities(network)
                    ?: continue

            val name =
                linkProperties.interfaceName
                    ?: continue

            val javaInterface =
                allInterfaces.firstOrNull {
                    it.name == name
                } ?: continue

            val item = BoxNetworkInterface()

            item.name = name
            item.index = javaInterface.index

            item.mtu = runCatching {
                javaInterface.mtu
            }.getOrDefault(1500)

            item.dnsServer = StringArray(
                linkProperties.dnsServers
                    .mapNotNull {
                        it.hostAddress
                    }
            )

            item.addresses = StringArray(
                javaInterface.interfaceAddresses.map {
                    "${it.address.hostAddress}/${it.networkPrefixLength}"
                }
            )

            item.type = when {

                capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                ) -> {
                    io.nekohasekai.libbox.Libbox.InterfaceTypeWIFI
                }

                capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_CELLULAR
                ) -> {
                    io.nekohasekai.libbox.Libbox.InterfaceTypeCellular
                }

                capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_ETHERNET
                ) -> {
                    io.nekohasekai.libbox.Libbox.InterfaceTypeEthernet
                }

                else -> {
                    io.nekohasekai.libbox.Libbox.InterfaceTypeOther
                }
            }

            item.metered =
                !capabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_NOT_METERED
                )

            result += item
        }

        return InterfaceArray(result)
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

    override fun systemCertificates(): StringIterator {
        return StringArray(emptyList())
    }

    override fun clearDNSCache() {
        // Nothing to clear.
    }

    override fun sendNotification(
        notification: Notification
    ) {
        // Notifications are handled by Android service/UI.
    }

    override fun registerMyInterface(
        name: String?
    ) {
        // Nothing required here.
    }

    fun closeTun() {
        runCatching {
            tun?.close()
        }

        tun = null
    }

    /**
     * libbox StringIterator.
     *
     * The generated Kotlin API requires len().
     */
    class StringArray(
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

    /**
     * libbox NetworkInterfaceIterator.
     */
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
