package com.ribuufing.bloodapp.core.base

import android.os.Build
import com.ribuufing.bloodapp.BuildConfig

object BaseUrls {

    val BASE_URL_VIRTUAL_DEVICE: String
        get() = if (isEmulator()) {
            "http://10.0.2.2:8000/"
        } else {
            "http://${BuildConfig.PHYSICAL_IP}:8000/"
        }

    val WEBSOCKET_URL_VIRTUAL_DEVICE: String
        get() = if (isEmulator()) {
            "ws://10.0.2.2:8000/chat/ws"
        } else {
            "ws://${BuildConfig.PHYSICAL_IP}:8000/chat/ws"
        }

    fun isEmulator(): Boolean {
        return Build.FINGERPRINT.contains("generic") ||
                Build.FINGERPRINT.startsWith("google/sdk_gphone") ||
                Build.MODEL.contains("sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK")
    }

    // Physical device URLs
    const val PHYSICAL_BASE_URL = "http://192.168.1.125:8000/"
    const val PHYSICAL_WEBSOCKET_URL = "ws://192.168.1.125:8000/chat/ws"
}