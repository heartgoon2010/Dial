package com.dial.models

import com.dial.presenters.utils.Base64
import com.dial.presenters.utils.JsonUtils
import java.util.concurrent.TimeUnit

interface DeviceObserver {
    fun onDeviceFound(uPnpServer: UPnPServer): Boolean
    fun onDeviceDescriptionReady(
        uPnpServer: UPnPServer,
        dialDeviceDescription: DialDeviceDescription
    )

    fun onQueryFinished(uPnpServer: UPnPServer, dialAppModel: DialAppModel)
}

object DialDevicesCache : DeviceObserver {

    private val TAG = DialDevicesCache::class.simpleName

    private const val SECOND_CHAR_POSITION = 1
    private const val DEVICE_SEPARATOR = ","
    private const val KEY_SEPARATOR = "_"

    private var dialConfig = DialConfig.getConfig() ?: DialParameter()
    private val mDiscoveryResult = LinkedHashMap<String, DiscoveryResult>()
    private var mExpiredTime = TimeUnit.MINUTES.toMillis(dialConfig.expireTimeInMinutes)

    init {
        initFromDiskData()
    }

    fun isValidDataFull(): Boolean {
        clearExpiredDevices()
        return mDiscoveryResult.size >= dialConfig.maxDeviceNumber
    }

    fun reset() {
        mDiscoveryResult.clear()
    }

    fun getDialDeviceInfo(uPnpServer: UPnPServer): DiscoveryResult? {
        val key = "${uPnpServer.ssid}$KEY_SEPARATOR${uPnpServer.location}"
        return mDiscoveryResult[key]
    }

    fun getData(): ArrayList<DiscoveryResult> {
        val deviceList = ArrayList<DiscoveryResult>()
        for (item in mDiscoveryResult.values) {
            deviceList.add(item)
        }
        return deviceList
    }

    /**
     * return true if device info is invalid, otherwise return false
     * */
    override fun onDeviceFound(uPnpServer: UPnPServer): Boolean {
        clearExpiredDevices()
        val key = "${uPnpServer.ssid}$KEY_SEPARATOR${uPnpServer.location}"
        return if (mDiscoveryResult[key] == null) {
            mDiscoveryResult.size < dialConfig.maxDeviceNumber
        } else {
            false
        }
    }

    override fun onDeviceDescriptionReady(
        uPnpServer: UPnPServer,
        dialDeviceDescription: DialDeviceDescription
    ) {
        val key = "${uPnpServer.ssid}$KEY_SEPARATOR${uPnpServer.location}"
        if (mDiscoveryResult[key] == null) {
            mDiscoveryResult[key] = DiscoveryResult(uPnpServer)
        }
        mDiscoveryResult[key]?.setDeviceDescription(dialDeviceDescription)

        updateDiskData()
    }

    override fun onQueryFinished(uPnPServer: UPnPServer, dialAppModel: DialAppModel) {
        val key = "${uPnPServer.ssid}$KEY_SEPARATOR${uPnPServer.location}"
        val discoveryResult = mDiscoveryResult[key] ?: return
        discoveryResult.setDialAppModel(dialAppModel)

        updateDiskData()
    }

    private fun initFromDiskData() {
        mDiscoveryResult.clear()
        val diskData = PreferenceHelper.getString(PreferenceHelper.DIAL_DEVICES, null) ?: return
        val deviceStringArray = diskData.split(DEVICE_SEPARATOR)
        for (deviceString in deviceStringArray) {
            val tempData = Base64.decode(deviceString)
            val discoveryResult =
                JsonUtils.jsonStringToClass(tempData, DiscoveryResult::class.java) ?: return
            val key =
                discoveryResult.getUPnPServer().ssid + KEY_SEPARATOR + discoveryResult.getUPnPServer().location
            mDiscoveryResult[key] = discoveryResult
        }
        clearExpiredDevices()
    }

    private fun updateDiskData() {
        val stringBuilder = StringBuilder()
        for (item in mDiscoveryResult.values) {
            val tempData = Base64.encode(JsonUtils.toJsonString(item))
            stringBuilder.append(DEVICE_SEPARATOR).append(tempData)
        }
        PreferenceHelper.set(
            PreferenceHelper.DIAL_DEVICES,
            stringBuilder.substring(SECOND_CHAR_POSITION)
        )
    }

    private fun clearExpiredDevices() {
        val toRemovedKeys = mutableListOf<String>()
        for (key in mDiscoveryResult.keys) {
            val value = mDiscoveryResult[key]
            if (value == null || value.isExpired(mExpiredTime)) {
                toRemovedKeys.add(key)
            }
        }
        for (key in toRemovedKeys) {
            mDiscoveryResult.remove(key)
        }
    }
}