package com.ribuufing.bloodapp.core.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.*

object NetworkUtils {
    fun getDeviceIpAddress(context: Context): String {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

            // Check if device has internet connection
            if (networkCapabilities != null &&
                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
            ) {
                // Get all network interfaces
                val networkInterfaces = NetworkInterface.getNetworkInterfaces()
                val addresses = mutableListOf<String>()

                while (networkInterfaces.hasMoreElements()) {
                    val networkInterface = networkInterfaces.nextElement()
                    
                    // Skip loopback and inactive interfaces
                    if (networkInterface.isLoopback || !networkInterface.isUp) continue

                    networkInterface.inetAddresses.asSequence()
                        .filter { !it.isLoopbackAddress && it is Inet4Address }
                        .map { it.hostAddress }
                        .forEach { addresses.add(it) }
                }

                // Prefer non-private IP if available
                addresses.firstOrNull { !isPrivateIP(it) }?.let { return it }
                // Fallback to any available IP
                addresses.firstOrNull()?.let { return it }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "0.0.0.0"
    }

    private fun isPrivateIP(ip: String): Boolean {
        return ip.startsWith("10.") ||
                ip.startsWith("172.16.") || ip.startsWith("172.17.") || ip.startsWith("172.18.") ||
                ip.startsWith("172.19.") || ip.startsWith("172.20.") || ip.startsWith("172.21.") ||
                ip.startsWith("172.22.") || ip.startsWith("172.23.") || ip.startsWith("172.24.") ||
                ip.startsWith("172.25.") || ip.startsWith("172.26.") || ip.startsWith("172.27.") ||
                ip.startsWith("172.28.") || ip.startsWith("172.29.") || ip.startsWith("172.30.") ||
                ip.startsWith("172.31.") ||
                ip.startsWith("192.168.")
    }
} 