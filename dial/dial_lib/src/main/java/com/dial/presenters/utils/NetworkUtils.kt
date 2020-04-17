package com.dial.presenters.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.dial.presenters.interfaces.DIALLog

object NetworkUtils {
    private val TAG = NetworkUtils::class.java.simpleName
    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo.isConnected
    }

    fun isWifiAvailable(context: Context): Boolean {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return try {
            val wifiInfo = wifiManager.connectionInfo
            wifiInfo != null && wifiInfo.ipAddress != 0
        } catch (e: Exception) {
            false
        }
    }

    //from Android 8.1 you must turn Location on to get real SSID,
    //if not you can get connection state but "<unknown ssid>" as SSID
    fun getWifiName(context: Context): String? {
        return if (!isWifiAvailable(context)) {
            null
        } else try {
            val wifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            //from Android 8.1 you must turn Location on to get real SSID,
            //if not you can get connection state but "<unknown ssid>" as SSID
            wifiInfo.ssid
        } catch (e: Exception) {
            DIALLog.e(TAG, "getWifiName:" + e.message)
            null
        }
    }
}
