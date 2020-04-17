package com.dial.presenters.interfaces

import com.dial.models.DialAppModel
import com.dial.models.DialDeviceDescription
import com.dial.models.UPnPServer

interface DiscoveryListener {
    fun onFindUpnpServer(uPnPServer: UPnPServer): Boolean

    fun onDescriptionReceived(uPnPServer: UPnPServer, description: DialDeviceDescription): Boolean

    fun onAppInfoReceived(uPnPServer: UPnPServer, appModel: DialAppModel)
}