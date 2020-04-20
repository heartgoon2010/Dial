package com.dial.presenters

import android.content.Context
import android.os.SystemClock
import com.dial.models.*
import com.dial.presenters.interfaces.DIALLog
import com.dial.presenters.interfaces.DiscoveryListener
import com.dial.presenters.interfaces.DiscoveryPolicy

/**
 * entrance of OTT devices discovery
 * */
class DialHandler(
    private val mContext: Context,
    private val mTargetApp: String,
    private val mTargetAppIdForRoku: String,
    private val mDiscoveryPolicy: DiscoveryPolicy = DefaultDiscoveryPolicy(mContext)
) {

    companion object {
        private val TAG = DialHandler::class.simpleName

        private const val SEARCH_TARGET = "urn:dial-multiscreen-org:service:dial:1"
        private const val SEARCH_TARGET_FOR_ROKU = "roku:ecp"
    }

    private var mNonRokuDiscoveryDriver: DiscoveryDriver? = null
    private var mRokuDiscoveryDriver: DiscoveryDriver? = null
    private val mDialDeviceDescriptionFetcher = DialDeviceDescriptionFetcher()
    private val mDeviceCache = DialDevicesCache
    private val mDiscoveryListener = DiscoveryListenerImp()

    fun start() {
        startDiscoveryIfNeeded()
    }

    fun stop() {
        mNonRokuDiscoveryDriver?.stop()
        mRokuDiscoveryDriver?.stop()
    }

    private fun startDiscoveryIfNeeded() {
        val dialConfig = DialConfig.getConfig()
        if (!mDiscoveryPolicy.isDIALEnable(dialConfig, mDeviceCache)) {
            DIALLog.d(TAG, "dial is not enabled")
            return
        }
        if (mNonRokuDiscoveryDriver == null) {
            mNonRokuDiscoveryDriver = DiscoveryDriver(
                mContext, mTargetApp, SEARCH_TARGET, dialConfig,
                mDialDeviceDescriptionFetcher, DialAppOperator(), mDiscoveryListener
            )
            mNonRokuDiscoveryDriver?.start()
            mRokuDiscoveryDriver = DiscoveryDriver(
                mContext, mTargetAppIdForRoku, SEARCH_TARGET_FOR_ROKU, dialConfig,
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
            DIALLog.d(TAG, "onDescriptionReceived $uPnPServer\n description=$description")
            mDeviceCache.onDeviceDescriptionReady(uPnPServer, description)
            return true
        }

        override fun onAppInfoReceived(uPnPServer: UPnPServer, appModel: DialAppModel) {
            DIALLog.d(TAG, "onAppInfoReceived $uPnPServer\n appModel=$appModel")
            mDeviceCache.onQueryFinished(uPnPServer, appModel)
        }

    }

}