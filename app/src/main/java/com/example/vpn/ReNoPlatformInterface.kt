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

/** Android bridge used by sing-box libbox to create the system VPN tunnel. */
class ReNoPlatformInterface(private val service: VpnService) : PlatformInterface {
    private var tun: android.os.ParcelFileDescriptor? = null

    override fun usePlatformAutoDetectInterfaceControl(): Boolean = true

    override fun autoDetectInterfaceControl(fd: Int) {
        if (!service.protect(fd)) error("Unable to protect sing-box socket")
    }

    override fun openTun(options: TunOptions): Int {
        if (VpnService.prepare(service) != null) error("VPN permission is not granted")
        val dns = runCatching { options.dnsServerAddress.value }.getOrDefault("1.1.1.1")
        val mtu = if (options.mtu > 0) options.mtu else 1500
        val builder = service.Builder()
            .setSession("ReNo VPN")
            .setMtu(mtu)
            .addAddress("172.19.0.1", 30)
            .addRoute("0.0.0.0", 0)
            .addDnsServer(dns)
        tun?.close()
        tun = builder.establish() ?: error("Unable to establish Android VPN interface")
        return tun!!.fd
    }

    override fun useProcFS(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

    override fun findConnectionOwner(
        ipProtocol: Int,
        sourceAddress: String,
        sourcePort: Int,
        destinationAddress: String,
        destinationPort: Int
    ): ConnectionOwner {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) error("Connection owner lookup unavailable")
        val cm = service.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val uid = cm.getConnectionOwnerUid(
            ipProtocol,
            InetSocketAddress(sourceAddress, sourcePort),
            InetSocketAddress(destinationAddress, destinationPort)
        )
        if (uid == Process.INVALID_UID) error("connection owner not found")
        val owner = ConnectionOwner()
        owner.userId = uid
        owner.userName = service.packageManager.getPackagesForUid(uid)?.firstOrNull() ?: ""
        owner.setAndroidPackageNames(
            StringArray((service.packageManager.getPackagesForUid(uid) ?: emptyArray()).iterator())
        )
        return owner
    }

    override fun startDefaultInterfaceMonitor(listener: InterfaceUpdateListener) {
        val cm = service.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork
        val lp = network?.let { cm.getLinkProperties(it) }
        val name = lp?.interfaceName.orEmpty()
        val index = runCatching { NetworkInterface.getByName(name)?.index ?: -1 }.getOrDefault(-1)
        listener.updateDefaultInterface(name, index, false, false)
    }

    override fun closeDefaultInterfaceMonitor(listener: InterfaceUpdateListener) = Unit

    override fun getInterfaces(): NetworkInterfaceIterator {
        val cm = service.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val list = mutableListOf<BoxNetworkInterface>()
        val all = runCatching {
            NetworkInterface.getNetworkInterfaces()?.toList() ?: emptyList()
        }.getOrDefault(emptyList())

        for (network in cm.allNetworks) {
            val lp = cm.getLinkProperties(network) ?: continue
            val nc = cm.getNetworkCapabilities(network) ?: continue
            val name = lp.interfaceName ?: continue
            val ni = all.firstOrNull { it.name == name } ?: continue
            val item = BoxNetworkInterface()
            item.name = name
            item.index = ni.index
            item.mtu = runCatching { ni.mtu }.getOrDefault(1500)
            item.dnsServer = StringArray(lp.dnsServers.mapNotNull { it.hostAddress }.iterator())
            item.addresses = StringArray(
                ni.interfaceAddresses.map { "${it.address.hostAddress}/${it.networkPrefixLength}" }.iterator()
            )
            item.type = when {
                nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> io.nekohasekai.libbox.Libbox.InterfaceTypeWIFI
                nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> io.nekohasekai.libbox.Libbox.InterfaceTypeCellular
                nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> io.nekohasekai.libbox.Libbox.InterfaceTypeEthernet
                else -> io.nekohasekai.libbox.Libbox.InterfaceTypeOther
            }
            item.metered = !nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            list += item
        }
        return InterfaceArray(list.iterator())
    }

    override fun underNetworkExtension(): Boolean = false
    override fun includeAllNetworks(): Boolean = false
    override fun readWIFIState(): WIFIState? = null
 override fun systemCertificates(): StringIterator =
    object : StringIterator {
        override fun len(): Int = 0
        override fun hasNext(): Boolean = false
        override fun next(): String = ""
    }
    override fun clearDNSCache() = Unit
    override fun sendNotification(notification: Notification) = Unit

    override fun registerMyInterface(name: String?) = Unit

    fun closeTun() {
        runCatching { tun?.close() }
        tun = null
    }

    class StringArray(private val iterator: Iterator<String>) : StringIterator {
        override fun hasNext(): Boolean = iterator.hasNext()
        override fun next(): String = iterator.next()
    }

    private class InterfaceArray(
        private val iterator: Iterator<BoxNetworkInterface>
    ) : NetworkInterfaceIterator {
        override fun hasNext(): Boolean = iterator.hasNext()
        override fun next(): BoxNetworkInterface = iterator.next()
    }
}
