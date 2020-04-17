package com.dial.presenters

import com.dial.models.DialAppModel
import com.dial.models.DialDeviceDescription
import com.dial.models.NetworkConstants
import com.dial.presenters.interfaces.AppInfoQueryInterface
import com.dial.presenters.interfaces.DIALLog
import com.dial.presenters.utils.DialUtils
import io.reactivex.Observable
import org.xmlpull.v1.XmlPullParser.*
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import retrofit2.Response
import java.io.IOException
import java.io.StringReader

/**
 * Do operations over applications on target devices, for example query application info,
 * launch and stop application by Application-URL
 */
class DialAppOperator : AppInfoQueryInterface {

    companion object {
        private val TAG = DialAppOperator::class.simpleName

        private const val SEPARATOR_SLASH = '/'
        private const val KEY_STATE = "state"
        private const val COMMAND_RUN = "run"
    }

    override fun queryAppInfo(
        dialDeviceDescription: DialDeviceDescription,
        appName: String
    ): Observable<DialAppModel> {
        if (DialUtils.isRokuDevice(dialDeviceDescription)) {
            return DialUtils.emptyQuery()
        }

        val app = if (dialDeviceDescription.appUrl.endsWith(SEPARATOR_SLASH)) {
            "${dialDeviceDescription.appUrl}$appName"
        } else {
            "${dialDeviceDescription.appUrl}$SEPARATOR_SLASH$appName"
        }
        val emptyHeader = HashMap<String, String>()
        return ApiRequester.getStringRequestApi().getRequest(emptyHeader, app)
            .map { response ->
                parseAppQueryResponse(response, app)
            }
    }

    private fun parseAppQueryResponse(response: Response<String>, appName: String): DialAppModel {
        if (response != null && response.isSuccessful && response.code() == NetworkConstants.Response.CODE_200) {
            val appInformation = response.body()
            if (appInformation != null) {

                val result = parseAppInformation(
                    response.raw().request().url().toString(),
                    appInformation,
                    appName
                )
                if (result != null) {
                    return result
                }
            }
        } else {
            DIALLog.d(TAG, "$appName is not installed")
        }
        return DialAppModel.EMPTY
    }

    fun startApp(appInfo: DialAppModel): Observable<Response<String>> {
        DIALLog.d(TAG, "startApp $appInfo")
        val header = HashMap<String, String>()
        var tempUrl = appInfo.lastUrl
        if (!tempUrl.endsWith(SEPARATOR_SLASH)) tempUrl += SEPARATOR_SLASH
        tempUrl += COMMAND_RUN
        return ApiRequester.getStringRequestApi().postRequest(header, tempUrl)

    }

    fun stopApp(appInfo: DialAppModel): Observable<Response<String>> {
        DIALLog.d(TAG, "stopApp $appInfo")
        val header = HashMap<String, String>()
        return ApiRequester.getStringRequestApi().deleteRequest(header, appInfo.lastUrl)
    }

    /**
     * response example:
     * <?xml version="1.0" encoding="UTF-8"?>
     * <service xmlns="urn:dial-multiscreen-org:schemas:dial" dialVer="1.7">
     * <name>com.tubitv.ott</name>
     * <options allowStop="true"/>
     * <state>stopped</state>
     * </service>
     * */
    private fun parseAppInformation(
        lastUrl: String,
        information: String,
        appName: String
    ): DialAppModel? {
        var state: String? = null
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(StringReader(information))
            var eventType = parser.eventType
            var lastTagName: String? = null
            while (eventType != END_DOCUMENT) {
                when (eventType) {
                    START_DOCUMENT -> {
                    }
                    START_TAG -> {
                        lastTagName = parser.name
                    }
                    TEXT -> if (lastTagName != null) {
                        when (lastTagName) {
                            KEY_STATE -> state = parser.text
                        }
                    }
                    END_TAG -> {
                        lastTagName = null
                    }
                }
                eventType = parser.next()
            }
        } catch (e: XmlPullParserException) {
            DIALLog.e(TAG, "parseXml ${e.message}")
            return null
        } catch (e: IOException) {
            DIALLog.e(TAG, "parseXml ${e.message}")
            return null
        }
        return if (state != null) {
            val dialAppModel = DialAppModel(appName, state, lastUrl)
            DIALLog.d(TAG, "$dialAppModel")
            dialAppModel
        } else {
            null
        }
    }


}