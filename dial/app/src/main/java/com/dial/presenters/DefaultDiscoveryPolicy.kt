package com.dial.presenters

import android.content.Context
import android.os.SystemClock
import com.dial.models.DialDevicesCache
import com.dial.models.DialParameter
import com.dial.models.PreferenceHelper
import com.dial.presenters.interfaces.DiscoveryPolicy
import com.dial.presenters.utils.NetworkUtils
import java.util.concurrent.TimeUnit

class DefaultDiscoveryPolicy(private val mContext: Context) : DiscoveryPolicy {
    companion object {
        private const val TIME_UNSET = -1L
    }

    private var mLastDiscoverTime =
        PreferenceHelper.getLong(PreferenceHelper.DIAL_LAST_DISCOVER_TIME, TIME_UNSET)

    override fun isDIALEnable(config: DialParameter, cache: DialDevicesCache): Boolean {
        val currentTime = SystemClock.elapsedRealtime()
        val discoveryPeriod = TimeUnit.MINUTES.toMillis(config.discoveryPeriodInMinutes)
        return NetworkUtils.isWifiAvailable(mContext)
                && (mLastDiscoverTime == TIME_UNSET || currentTime - mLastDiscoverTime > discoveryPeriod)
                && !DialDevicesCache.isValidDataFull()
    }

    fun updateDiscoveryTime(elapsedRealtime: Long) {
        mLastDiscoverTime = elapsedRealtime
        PreferenceHelper.set(PreferenceHelper.DIAL_LAST_DISCOVER_TIME, mLastDiscoverTime)
    }
}