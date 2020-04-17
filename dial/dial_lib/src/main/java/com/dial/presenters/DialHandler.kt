package com.dial.presenters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.SystemClock
import com.dial.models.*
import com.dial.presenters.interfaces.DIALLog
import com.dial.presenters.interfaces.DiscoveryListener

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
    private val mDiscoveryListener = DiscoveryListenerImp()

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
            mNonRokuDiscoveryDriver = DiscoveryDriver(
                mContext, TARGET_APP, SEARCH_TARGET, dialConfig,
                mDialDeviceDescriptionFetcher, DialAppOperator(), mDiscoveryListener
            )
            mNonRokuDiscoveryDriver?.start()
            mRokuDiscoveryDriver = DiscoveryDriver(
                mContext, TARGET_APP_FOR_ROKU, SEARCH_TARGET_FOR_ROKU, dialConfig,
                mDialDeviceDescriptionFetcher, RokuAppOperator(), mDiscoveryListener
            )
            mRokuDiscoveryDriver?.start()
        } else {
            DIALLog.d(TAG, "restart discovery")
            mNonRokuDiscoveryDriver?.restart()
            mRokuDiscoveryDriver?.restart()
        }
        mDiscoveryPolicy.updateDiscoveryTime(SystemClock.elapsedRealtime())

    }

    inner class DiscoveryListenerImp : DiscoveryListener {
        override fun onFindUpnpServer(uPnPServer: UPnPServer): Boolean {
            DIALLog.d(TAG, "onFindUpnpServer $uPnPServer")
            return true
        }

        override fun onDescriptionReceived(uPnPServer: UPnPServer, description: DialDeviceDescription): Boolean {
            DIALLog.d(TAG, "onDescriptionReceived $uPnPServer\n" +
                    "description=$description")
            mDeviceCache.onDeviceDescriptionReady(uPnPServer, description)
            return true
        }

        override fun onAppInfoReceived(uPnPServer: UPnPServer, appModel: DialAppModel) {
            DIALLog.d(TAG, "onAppInfoReceived $uPnPServer\n" +
                    "appModel=$appModel")
            mDeviceCache.onQueryFinished(uPnPServer, appModel)
        }

    }

}