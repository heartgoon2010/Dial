package com.dial.models

import android.os.SystemClock

class DiscoveryResult(private val mUPnPServer: UPnPServer) {
    private var mDeviceDescription: DialDeviceDescription = DialDeviceDescription.EMPTY
    private var mDialAppModel: DialAppModel = DialAppModel.EMPTY
    private var mLastSaveTime = SystemClock.elapsedRealtime()

    fun isExpired(expiredTime: Long): Boolean {
        return SystemClock.elapsedRealtime() - mLastSaveTime > expiredTime
    }

    fun setDeviceDescription(deviceDescription: DialDeviceDescription) {
        mLastSaveTime = SystemClock.elapsedRealtime()
        mDeviceDescription = deviceDescription
    }

    fun setDialAppModel(dialAppModel: DialAppModel) {
        mLastSaveTime = SystemClock.elapsedRealtime()
        mDialAppModel = dialAppModel
    }

    fun getUPnPServer() = mUPnPServer

    fun getDeviceDescription() = mDeviceDescription

    fun getDialAppModel() = mDialAppModel

    override fun toString(): String {
        return "${getUPnPServer()}, ${getDeviceDescription()}, ${getDialAppModel()}"
    }

}