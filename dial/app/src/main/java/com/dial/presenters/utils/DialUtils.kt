package com.dial.presenters.utils

import com.dial.models.DialAppModel
import com.dial.models.DialDeviceDescription
import io.reactivex.Observable
import io.reactivex.Observer

class DialUtils {

    companion object {
        private const val ROKU_TAG = "roku"

        fun isRokuDevice(dialDeviceDescription: DialDeviceDescription): Boolean {
            return dialDeviceDescription.manufacture.toLowerCase().indexOf(ROKU_TAG) != -1
        }

        fun emptyQuery(): Observable<DialAppModel> {
            return object : Observable<DialAppModel>() {
                override fun subscribeActual(observer: Observer<in DialAppModel>?) {
                }
            }
        }
    }
}