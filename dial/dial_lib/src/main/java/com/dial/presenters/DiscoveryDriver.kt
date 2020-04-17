package com.dial.presenters

import android.content.Context
import android.os.SystemClock
import com.dial.models.*
import com.dial.presenters.interfaces.AppInfoQueryInterface
import com.dial.presenters.interfaces.DeviceDescriptionFetcherInterface
import com.dial.presenters.interfaces.DiscoveryListener
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.DatagramSocket
import java.util.concurrent.TimeUnit

class DiscoveryDriver(
    private val mContext: Context,
    private val mTargetApp: String,
    private val mSearchTarget: String,
    private val mDialConfig: DialParameter,
    private val mDeviceDescriptionFetcher: DeviceDescriptionFetcherInterface,
    private val mAppInfoQuery: AppInfoQueryInterface,
    private val mDiscoveryListener: DiscoveryListener
) {

    companion object {
        private val TAG = DiscoveryDriver::class.simpleName

        private const val SOCKET_RECEIVE_TIMEOUT_MS = 10000
        private const val SOCKET_READ_TIME = 120000L
    }

    private val mDialDeviceFinder = DialDeviceFinder(mContext)
    private val mSocket = DatagramSocket()
    private var mSendDisposable: Disposable? = null
    private var mReceiveDisposable: Disposable? = null

    init {
        mSocket.soTimeout = SOCKET_RECEIVE_TIMEOUT_MS
    }

    fun start() {
        val searchTimes = mDialConfig.searchTimes
        val searchPeriodInSeconds = mDialConfig.searchPeriodInSeconds
        val startTime = SystemClock.elapsedRealtime()

        //As M-SEARCH request is based on unreliable UDP transportation, so we send multiple requests
        //to increase the chance of success
        mSendDisposable =
            Observable.intervalRange(0, searchTimes, 0, searchPeriodInSeconds, TimeUnit.SECONDS)
                .flatMap { mDialDeviceFinder.sendSearchRequest(mSocket, mSearchTarget) }
                .subscribeOn(Schedulers.io())
                .subscribe()

        mReceiveDisposable =
            mDialDeviceFinder.receiveSearchResponse(mSocket)
                .repeatUntil { SystemClock.elapsedRealtime() - startTime > SOCKET_READ_TIME }
                .subscribeOn(Schedulers.io())
                .map { packet ->
                    mDialDeviceFinder.parseSearchResponsePacket(packet, mSearchTarget)
                }.filter { uPnPServer ->
                    mDiscoveryListener.onFindUpnpServer(uPnPServer)
                }.flatMap { uPnPServer ->
                    mDeviceDescriptionFetcher.requestDeviceDescription(uPnPServer)
                        .filter { dialDeviceDescription ->
                            //filter invalid response
                            dialDeviceDescription != DialDeviceDescription.EMPTY
                        }.map { dialDeviceDescription ->
                            dialDeviceDescription.uPnPServer = uPnPServer
                            dialDeviceDescription
                        }.filter { deviceDescription ->
                            mDiscoveryListener.onDescriptionReceived(uPnPServer, deviceDescription)
                        }
                }.flatMap { dialDeviceDescription ->
                    mAppInfoQuery.queryAppInfo(dialDeviceDescription, mTargetApp)
                        .filter { appModel ->
                            dialDeviceDescription.uPnPServer?.let { upnpServer ->
                                mDiscoveryListener.onAppInfoReceived(upnpServer, appModel)
                            }
                            true
                        }
                }.subscribe()
    }

    fun stop() {
        mSendDisposable?.dispose()
        mReceiveDisposable?.dispose()
    }

    fun restart() {
        stop()
        start()
    }
}