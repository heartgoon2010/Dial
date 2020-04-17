package com.dial.models

class NetworkConstants {

    class HEADER {
        companion object {
            const val APPLICATION_URL = "Application-URL"
            const val CONTENT_TYPE = "Content-Type"
            const val CONTENT_TYPE_TEXT_VALUE = "application/xml"
            const val CONNECTION = "Connection"
            const val CONNECTION_VALUE = "keep-alive"
            const val ORIGIN = "Origin"
            const val ORIGIN_VALUE = "chrome-extension://boadgeojelhgndaghljhdicfkmllpafd"
            const val DNT = "DNT"
            const val DNT_VALUE = "1"
            const val ACCEPT_ENCODING = "Accept-Encoding"
            const val ACCEPT_ENCODING_VALUE = "gzip,deflate,sdch"
            const val ACCEPT = "Accept"
            const val ACCEPT_VALUE = "*/*"
            const val ACCEPT_LANGUAGE = "Accept-Language"
            const val ACCEPT_LANGUAGE_VALUE = "en-US,en;q=0.8"
        }
    }

    class Response {
        companion object {
            const val CODE_200 = 200
        }
    }
}