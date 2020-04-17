package com.dial.models

/**
 * {
 *  "maxDeviceNumber": 10,
 *  "expireTimeInMinutes":10080,
 *  "minVersionCode":327,
 *  "searchTimes":5,
 *  "discoveryPeriodInMinutes":180L
 *  "searchPeriodInSeconds":10
 *  }
 * */
class DialParameter {
    // control the probability that the config is available to a device
    var segments = 100

    // specify how many OTT devices' information will be collected for a device
    var maxDeviceNumber = 10L

    // expire time for device information
    var expireTimeInMinutes = 10080L

    // specify from which application we will do discovery
    var minVersionCode = 327

    // specify times for M-SEARCH
    var searchTimes = 5L

    // specify period for repeated M-SEARCH
    var searchPeriodInSeconds = 6L

    // specify period for discovery
    var discoveryPeriodInMinutes = 0L

    // specify max app number sent to backend
    var maxAppNumberOnRoku = 50

    companion object {
        fun default(): DialParameter = DialParameter()
    }
}

class DialConfig {

    companion object {
        fun getConfig(): DialParameter = DialParameter.default()
    }
}