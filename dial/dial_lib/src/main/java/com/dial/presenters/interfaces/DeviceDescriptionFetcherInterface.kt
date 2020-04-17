package com.dial.presenters.interfaces

import com.dial.models.DialDeviceDescription
import com.dial.models.UPnPServer
import io.reactivex.Observable

interface DeviceDescriptionFetcherInterface {
    fun requestDeviceDescription(uPnPServer: UPnPServer): Observable<DialDeviceDescription>
}