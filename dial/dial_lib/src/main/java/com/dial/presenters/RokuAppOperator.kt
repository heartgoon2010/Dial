package com.dial.presenters

import com.dial.models.*
import com.dial.presenters.interfaces.AppInfoQueryInterface
import com.dial.presenters.interfaces.DIALLog
import com.dial.presenters.utils.DialUtils
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import retrofit2.Response
import java.io.IOException
import java.io.StringReader

/***
 * reference: https://developer.roku.com/docs/developer-program/debugging/external-control-api.md
 *
 */
class RokuAppOperator : AppInfoQueryInterface {

    companion object {

        private val TAG = RokuAppOperator::class.simpleName

        private var sDialConfig = DialConfig.getConfig() ?: DialParameter()

        private const val SEPARATOR_SLASH = '/'
        private const val COMMAND_QUERY_APPS = "query/apps"
        private const val COMMAND_ACTIVE_APP = "query/active-app"
        private const val KEY_APP_LIST_ITEM = "app"
        private const val KEY_ACTIVE_APP = "app"
        private const val ATTRIBUTE_APP_ID = "id"
    }

    override fun queryAppInfo(dialDeviceDescription: DialDeviceDescription, appName: String): Observable<DialAppModel> {
        if (!DialUtils.isRokuDevice(dialDeviceDescription)) {
            return DialUtils.emptyQuery()
        }

        DIALLog.d(TAG, "dialDeviceDescription=$dialDeviceDescription")
        return Observable.zip<List<String>, String, DialAppModel>(
            queryAppsInfo(dialDeviceDescription)
                .map { response ->
                    parseAppsInfo(response)
                },
            queryActiveApp(dialDeviceDescription)
                .map { response ->
                    parseActiveApp(response)
                },
            BiFunction { appList, activeApp ->
                //TODO improve
                DialAppModel(appName, String.EMPTY, String.EMPTY)
            })
    }

    private fun queryAppsInfo(dialDeviceDescription: DialDeviceDescription): Observable<Response<String>> {
        val location = dialDeviceDescription.uPnPServer?.location ?: dialDeviceDescription.appUrl
        val command = if (location.endsWith(SEPARATOR_SLASH)) {
            "$location$COMMAND_QUERY_APPS"
        } else {
            "$location$SEPARATOR_SLASH$COMMAND_QUERY_APPS"
        }
        val emptyHeader = HashMap<String, String>()
        return ApiRequester.getStringRequestApi().getRequest(emptyHeader, command)
    }

    /**
    Example:
    <?xml version="1.0" encoding="UTF-8" ?>
    <apps>
    <app id="31012" type="menu" version="1.9.32">FandangoNOW Movies &amp; TV</app>
    <app id="41468" subtype="rsga" type="appl" version="2.11.9">Tubi - Free Movies &amp; TV</app>
    <app id="151908" subtype="rsga" type="appl" version="2.5.19">The Roku Channel</app>
    <app id="552944" subtype="rsga" type="appl" version="1.2.50">Roku Tips &amp; Tricks</app>
    </apps>
     * */
    private fun parseAppsInfo(response: Response<String>): List<String> {
        val result = mutableListOf<String>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(StringReader(response.body()))
            var eventType = parser.eventType
            var lastTagName: String? = null
            var appId: String? = null
            var appName: String? = null
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_DOCUMENT -> {
                    }
                    XmlPullParser.START_TAG -> {
                        lastTagName = parser.name
                        if (lastTagName == KEY_APP_LIST_ITEM) {
                            appId = parser.getAttributeValue(null, ATTRIBUTE_APP_ID)
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (lastTagName == KEY_APP_LIST_ITEM) {
                            appName = parser.text
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (lastTagName == KEY_APP_LIST_ITEM && appId != null) {
                            result.add(appId)
                            DIALLog.d(TAG, "appId=$appId, appName=$appName")
                        }
                        lastTagName = null
                        appId = null
                    }
                }
                eventType = parser.next()
            }
        } catch (e: XmlPullParserException) {
            DIALLog.e(TAG, "parseXml ${e.message}")
        } catch (e: IOException) {
            DIALLog.e(TAG, "parseXml ${e.message}")
        }
        return result
    }

    private fun queryActiveApp(dialDeviceDescription: DialDeviceDescription): Observable<Response<String>> {
        val location = dialDeviceDescription.uPnPServer?.location ?: dialDeviceDescription.appUrl
        val command = if (location.endsWith(SEPARATOR_SLASH)) {
            "$location$COMMAND_ACTIVE_APP"
        } else {
            "$location$SEPARATOR_SLASH$COMMAND_ACTIVE_APP"
        }
        DIALLog.d(TAG, "queryActiveApp $command")
        val emptyHeader = HashMap<String, String>()
        return ApiRequester.getStringRequestApi().getRequest(emptyHeader, command)
    }

    /**
    Example:
    <?xml version="1.0" encoding="UTF-8" ?>
    <active-app>
    <app id="250045" subtype="rsga" type="appl" version="2.5.75">Roku Featured Free Prod</app>
    </active-app>
     * */
    fun parseActiveApp(response: Response<String>): String {
        DIALLog.d(TAG, "parseActiveApp")
        var result = String.EMPTY
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(StringReader(response.body()))
            var eventType = parser.eventType
            var lastTagName: String? = null
            var appId: String? = null
            var appName: String? = null
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_DOCUMENT -> {
                    }
                    XmlPullParser.START_TAG -> {
                        lastTagName = parser.name
                        if (lastTagName == KEY_ACTIVE_APP) {
                            appId = parser.getAttributeValue(null, ATTRIBUTE_APP_ID)
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (lastTagName == KEY_ACTIVE_APP) {
                            appName = parser.text
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (lastTagName == KEY_ACTIVE_APP && appId != null) {
                            result = appId
                            DIALLog.d(TAG, "appId=$appId, appName=$appName")
                        }
                        lastTagName = null
                        appId = null
                    }
                }
                eventType = parser.next()
            }
        } catch (e: XmlPullParserException) {
            DIALLog.e(TAG, "parseXml ${e.message}")
        } catch (e: IOException) {
            DIALLog.e(TAG, "parseXml ${e.message}")
        }
        return result
    }

}