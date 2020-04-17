package com.dial.presenters.interfaces

import com.dial.models.DialAppModel
import com.dial.models.DialDeviceDescription
import io.reactivex.Observable

interface AppInfoQueryInterface {
    fun queryAppInfo(dialDeviceDescription: DialDeviceDescription, appName: String): Observable<DialAppModel>
}