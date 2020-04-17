package com.dial.models

/**
 * After UPnP server is found and its device description is got,
 * we can use DialServer to describe the dial service on it.
 * */
class DialDeviceDescription(var appUrl: String,
                            var friendlyName: String,
                            var uuid: String,
                            var manufacture: String,
                            var modelName: String,
                            var description: String) {

    var uPnPServer: UPnPServer? = null

    companion object {
        val EMPTY = DialDeviceDescription(
            String.EMPTY,
            String.EMPTY,
            String.EMPTY,
            String.EMPTY,
            String.EMPTY,
            String.EMPTY
        )
    }

    override fun toString(): String {

        return "appUrl=$appUrl, friendlyName=$friendlyName, uuid=$uuid, manufacture=$manufacture, " +
                "modelName=$modelName, description=$description"
    }
}