package com.dial.models

import java.net.InetAddress

/**
 * UPnPServer is used to describe a device which is able to get its IP address on the network,
 * announce its services to control point on the network
 * and allow control point to retrieve the device's description.
 * These are basic steps of UPnP(Universal Plug and Play) protocol
 * */
open class UPnPServer(
    val ssid: String,
    val location: String,
    val ipAddress: InetAddress,
    val port: Int
) {

    companion object {
        private const val LOOPBACK_ADDRESS = "127.0.0.1"
        val EMPTY = UPnPServer(
            String.EMPTY,
            String.EMPTY,
            InetAddress.getByName(LOOPBACK_ADDRESS),
            0
        )
    }

    override fun toString(): String {
        return "ssid=$ssid, location=$location, ipAddress=${ipAddress.hostAddress}, port=$port"
    }
}