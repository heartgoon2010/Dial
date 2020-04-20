package com.dial.presenters

import android.content.Context
import android.os.SystemClock
import com.dial.models.DialDevicesCache
import com.dial.models.DialParameter
import com.dial.models.PreferenceHelper
import com.dial.presenters.interfaces.DIALLog
import com.dial.presenters.interfaces.DiscoveryPolicy
import com.dial.presenters.utils.NetworkUtils
import java.util.concurrent.TimeUnit

class DefaultDiscoveryPolicy(private val mContext: Context) : DiscoveryPolicy {
    companion object {
        private val TAG = DefaultDiscoveryPolicy::class.java.simpleName
        private const val TIME_UNSET = 0L
    }

    private var mLastDiscoverTime: Long

    init {
        mLastDiscoverTime = PreferenceHelper.getLong(PreferenceHelper.DIAL_LAST_DISCOVER_TIME, TIME_UNSET)
        DIALLog.d(TAG, "mLastDiscoverTime=$mLastDiscoverTime")
    }

    override fun isDIALEnable(config: DialParameter, cache: DialDevicesCache): Boolean {
        DIALLog.d(TAG, "isDIALEnable mLastDiscoverTime=$mLastDiscoverTime")
        val currentTime = SystemClock.elapsedRealtime()
        val discoveryPeriod = TimeUnit.MINUTES.toMillis(config.discoveryPeriodInMinutes)
        return NetworkUtils.isWifiAvailable(mContext)
                && (mLastDiscoverTime == TIME_UNSET || currentTime - mLastDiscoverTime > discoveryPeriod)
                && !DialDevicesCache.isValidDataFull()
    }

    override fun updateDiscoveryTime(discoveryTime: Long) {
        mLastDiscoverTime = discoveryTime
        DIALLog.d(TAG, "updateDiscoveryTime mLastDiscoverTime=$mLastDiscoverTime")
        PreferenceHelper.set(PreferenceHelper.DIAL_LAST_DISCOVER_TIME, mLastDiscoverTime)
    }
}