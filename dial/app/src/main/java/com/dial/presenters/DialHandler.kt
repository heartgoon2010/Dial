package com.dial.presenters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.SystemClock
import com.dial.models.DialConfig
import com.dial.models.DialDevicesCache
import com.dial.presenters.interfaces.DIALLog

/**
 * entrance of OTT devices discovery
 * */
class DialHandler(private val mContext: Context) {

    companion object {
        private val TAG = DialHandler::class.simpleName

        private const val SEARCH_TARGET = "urn:dial-multiscreen-org:service:dial:1"
        private const val SEARCH_TARGET_FOR_ROKU = "roku:ecp"

        private const val TARGET_APP = "com.tubitv.ott"
        private const val TARGET_APP_FOR_ROKU = "41468"
    }

    private var mNonRokuDiscoveryDriver: DiscoveryDriver? = null
    private var mRokuDiscoveryDriver: DiscoveryDriver? = null
    private val mDialDeviceDescriptionFetcher = DialDeviceDescriptionFetcher()
    private val mDiscoveryPolicy = DefaultDiscoveryPolicy(mContext)
    private val mDeviceCache = DialDevicesCache

    private val mNetworkStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            DIALLog.d(TAG, "on network state change ${intent?.action}")
            if (ConnectivityManager.CONNECTIVITY_ACTION == intent?.action) {
                startDiscoveryIfNeeded()
            }
        }
    }

    fun start() {
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        mContext.applicationContext.registerReceiver(mNetworkStateReceiver, intentFilter)
        startDiscoveryIfNeeded()
    }

    fun stop() {
        mContext.applicationContext.unregisterReceiver(mNetworkStateReceiver)
    }

    private fun startDiscoveryIfNeeded() {
        val dialConfig = DialConfig.getConfig()
        if (!mDiscoveryPolicy.isDIALEnable(dialConfig, mDeviceCache)) {
            DIALLog.d(TAG, "dial is not enabled")
            return
        }
        if (mNonRokuDiscoveryDriver == null) {
            mNonRokuDiscoveryDriver =
                DiscoveryDriver(
                    mContext,
                    TARGET_APP,
                    SEARCH_TARGET,
                    dialConfig,
                    mDialDeviceDescriptionFetcher,
                    DialAppOperator()
                )
            mNonRokuDiscoveryDriver?.start()
            mRokuDiscoveryDriver = DiscoveryDriver(
                mContext,
                TARGET_APP_FOR_ROKU,
                SEARCH_TARGET_FOR_ROKU,
                dialConfig,
                mDialDeviceDescriptionFetcher,
                RokuAppOperator()
            )
            mRokuDiscoveryDriver?.start()
        } else {
            DIALLog.d(TAG, "restart discovery")
            mNonRokuDiscoveryDriver?.restart()
            mRokuDiscoveryDriver?.restart()
        }
        mDiscoveryPolicy.updateDiscoveryTime(SystemClock.elapsedRealtime())

    }

}