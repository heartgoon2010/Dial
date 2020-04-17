package com.dial.presenters

import android.content.Context
import android.net.Uri
import com.dial.models.EMPTY
import com.dial.models.UPnPServer
import com.dial.presenters.interfaces.DIALLog
import com.dial.presenters.utils.NetworkUtils
import io.reactivex.Observable
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * Search UPnP servers(devices) which could provide dial service by multicast
 * In the message we specify the target HOST, message type(MAN),
 * the maximum wait response time(MX) in seconds (target device will wait a random time between 0 and MX before attempting to respond),
 * and the search target(ST)
 * This process is an implementation of SSDP(Simple Service Discovery Protocol)
 * */
class DialDeviceFinder(private val mContext: Context) {

    companion object {
        private val TAG = DialDeviceFinder::class.simpleName
        private const val M_SEARCH_FORMAT = "M-SEARCH * HTTP/1.1\r\n" +
                "HOST: 239.255.255.250:1900\r\n" +
                "MAN: \"ssdp:discover\"\r\n" +
                "MX: 10\r\n" +
                "ST: %s\r\n\r\n"

        private const val BROADCAST_SERVER_PORT = 1900
        private const val BROADCAST_SERVER = "239.255.255.250"
        private const val MAX_PACKET_SIZE = 4096

        private const val NEW_LINE_SEPARATOR = "\r\n"
        private const val HEADER_LOCATION = "LOCATION"
        private const val HEADER_ST = "ST"
        private val mBroadcastAddress = InetAddress.getByName(BROADCAST_SERVER)
    }

    fun sendSearchRequest(socket: DatagramSocket, target: String): Observable<DatagramSocket> {
        val message = String.format(M_SEARCH_FORMAT, target)
        return Observable.create { emitter ->
            val buf = message.toByteArray()
            val packet =
                DatagramPacket(buf, buf.size, mBroadcastAddress, BROADCAST_SERVER_PORT)
            try {
                socket.send(packet)
                emitter.onNext(socket)
                DIALLog.d(TAG, "sendSearchRequest socket:$socket")
            } catch (e: IOException) {
                DIALLog.e(TAG, "sendSearchRequest socket:$socket, exception:${e.message}")
            }
            emitter.onComplete()
        }
    }

    fun receiveSearchResponse(socket: DatagramSocket): Observable<DatagramPacket> {
        return Observable.create { emitter ->
            val buffer = ByteArray(MAX_PACKET_SIZE)
            try {
                val packet = DatagramPacket(buffer, buffer.size)
                socket.receive(packet)
                emitter.onNext(packet)
                DIALLog.d(TAG, "receiveSearchResponse socket:$socket")
            } catch (e: IOException) {
                DIALLog.e(TAG, "receiveSearchResponse socket:$socket, exception:$e")
            }
            emitter.onComplete()
        }
    }


    /**
     * Sample Data:
     * HTTP/1.1 200 OK
     * CACHE-CONTROL: max-age=1800
     * DATE: Tue, 29 Oct 2019 08:42:57 GMT
     * EXT:
     * LOCATION: http://192.168.31.251:7678/nservice/
     * SERVER: SHP, UPnP/1.0, Samsung UPnP SDK/1.0
     * ST: urn:dial-multiscreen-org:service:dial:1
     * USN: uuid:807d439c-2d22-47b4-8c65-26d51e153582::urn:dial-multiscreen-org:service:dial:1
     * WAKEUP: MAC=84:c0:ef:6f:40:6b;Timeout=10
     * Content-Length: 0
     * */
    fun parseSearchResponsePacket(packet: DatagramPacket, target: String): UPnPServer {
        val strPacket = String(packet.data, 0, packet.length)
        val tokens = strPacket.trim().split(NEW_LINE_SEPARATOR)
        var location: String = String.EMPTY
        var found = false
        for (element in tokens) {
            val token = element.trim()
            if (token.startsWith(HEADER_LOCATION)) {
                location = token.substring(HEADER_LOCATION.length + 1).trim()
            } else if (token.startsWith(HEADER_ST)) {
                val st = token.substring(HEADER_ST.length + 1).trim()
                if (st == target) {
                    found = true
                }
            }
        }
        if (!found || location.isEmpty()) {
            DIALLog.w(TAG, "Warning response=$strPacket")
            return UPnPServer.EMPTY
        }

        try {
            val ssid = NetworkUtils.getWifiName(mContext) ?: String.EMPTY
            val uri = Uri.parse(location)
            val address = InetAddress.getByName(uri.host)
            val ottDeviceModel = UPnPServer(ssid, location, address, uri.port)
            DIALLog.d(TAG, "find device: $ottDeviceModel")
            return ottDeviceModel
        } catch (e: Exception) {
            DIALLog.e(TAG, "parseSearchResponsePacket exception:${e.message}")
        }
        return UPnPServer.EMPTY
    }
}