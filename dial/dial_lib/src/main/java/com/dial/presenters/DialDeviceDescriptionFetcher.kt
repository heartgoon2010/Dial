package com.dial.presenters

import android.text.TextUtils
import com.dial.models.DialDeviceDescription
import com.dial.models.EMPTY
import com.dial.models.NetworkConstants
import com.dial.models.NetworkConstants.HEADER
import com.dial.models.UPnPServer
import com.dial.presenters.interfaces.DIALLog
import com.dial.presenters.interfaces.DeviceDescriptionFetcherInterface
import io.reactivex.Observable
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import retrofit2.Response
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader

/**
 * Get more information of the target devices by HTTP GET request, such as service url, friendly name,
 * manufacture, model info, uuid
 * */
class DialDeviceDescriptionFetcher : DeviceDescriptionFetcherInterface {

    companion object {
        private val TAG = DialDeviceDescriptionFetcher::class.simpleName

        private const val KEY_FRIENDLY_NAME = "friendlyName"
        private const val KEY_MANUFACTURER = "manufacturer"
        private const val KEY_MODEL_DESCRIPTION = "modelDescription"
        private const val KEY_MODEL_NAME = "modelName"
        private const val KEY_UDN = "UDN"

        private const val UDN_PREFIX = "uuid:"
    }

    private val header = HashMap<String, String>()

    init {
        header[HEADER.CONNECTION] = HEADER.CONNECTION_VALUE
        header[HEADER.ORIGIN] = HEADER.ORIGIN_VALUE
        header[HEADER.DNT] = HEADER.DNT_VALUE
        header[HEADER.ACCEPT_ENCODING] = HEADER.ACCEPT_ENCODING_VALUE
        header[HEADER.ACCEPT] = HEADER.ACCEPT_VALUE
        header[HEADER.ACCEPT_LANGUAGE] = HEADER.ACCEPT_LANGUAGE_VALUE
        header[HEADER.CONTENT_TYPE] = HEADER.CONTENT_TYPE_TEXT_VALUE
    }

    override fun requestDeviceDescription(uPnPServer: UPnPServer): Observable<DialDeviceDescription> {
        DIALLog.d(TAG, ">>>requestDeviceDescription")
        return ApiRequester.getStringRequestApi()
            .getRequest(header, uPnPServer.location)
            .map { response ->
                parseDeviceDescriptionResponse(response)
            }
    }

    private fun parseDeviceDescriptionResponse(response: Response<String>): DialDeviceDescription {
        if (response.code() == NetworkConstants.Response.CODE_200 && response.isSuccessful) {
            val appUrl = response.headers().get(HEADER.APPLICATION_URL) ?: String.EMPTY
            val description = response.body()
            DIALLog.d(TAG, "description=$description")
            if (description != null) {
                return parseResponse(appUrl, description)
            }
        }
        return DialDeviceDescription.EMPTY
    }

    private fun parseResponse(appUrl: String, response: String): DialDeviceDescription {
        DIALLog.d(TAG, "response=$response")
        var friendlyName: String = String.EMPTY
        var manufacturer: String = String.EMPTY
        var description: String = String.EMPTY
        var modelName: String = String.EMPTY
        var uuid: String = String.EMPTY

        val inputStream = ByteArrayInputStream(response.toByteArray())
        val reader = BufferedReader(InputStreamReader(inputStream))
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(reader)
            var eventType = parser.eventType
            var lastTagName: String? = null
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        lastTagName = parser.name
                    }
                    XmlPullParser.TEXT -> {
                        if (!TextUtils.isEmpty(lastTagName)) {
                            when (lastTagName) {
                                KEY_FRIENDLY_NAME -> friendlyName = parser.text
                                KEY_MANUFACTURER -> manufacturer = parser.text
                                KEY_MODEL_DESCRIPTION -> description = parser.text
                                KEY_MODEL_NAME -> modelName = parser.text
                                KEY_UDN -> uuid = getUuidFromUDN(parser.text)
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        lastTagName = null
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            return DialDeviceDescription.EMPTY
        } finally {
            inputStream.close()
            reader.close()
        }

        return DialDeviceDescription(
            appUrl,
            friendlyName,
            uuid,
            manufacturer,
            modelName,
            description
        )

    }

    private fun getUuidFromUDN(udn: String): String {
        if (udn.startsWith(UDN_PREFIX)) {
            return udn.replace(UDN_PREFIX, String.EMPTY).trim()
        }
        return udn
    }
}