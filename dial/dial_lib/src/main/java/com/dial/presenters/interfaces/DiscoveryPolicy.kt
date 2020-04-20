package com.dial.presenters.interfaces

import com.dial.models.DialDevicesCache
import com.dial.models.DialParameter

interface DiscoveryPolicy {

    fun isDIALEnable(config: DialParameter, cache: DialDevicesCache): Boolean

    fun updateDiscoveryTime(discoveryTime: Long)
}