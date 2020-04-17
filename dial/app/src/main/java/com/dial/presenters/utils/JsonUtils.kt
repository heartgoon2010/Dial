package com.dial.presenters.utils

import com.dial.models.EMPTY
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.Reader
import java.lang.reflect.Type

class JsonUtils {

    companion object {

        fun <T> jsonStringToClass(sourceString: String, classOfT: Class<T>): T? {
            return try {
                Gson().fromJson(sourceString, classOfT)
            } catch (e: Exception) {
                null
            }
        }

        fun <T> readerToClass(reader: Reader, classOfT: Class<T>): T? {
            return try {
                Gson().fromJson(reader, classOfT)
            } catch (e: Exception) {
                null
            }
        }

        fun <T> jsonStringToType(sourceString: String, type: Type): T? {
            return try {
                GsonBuilder().create().fromJson(sourceString, type)
            } catch (e: Exception) {
                null
            }
        }

        fun toJsonString(obj: Any): String {
            return try {
                Gson().toJson(obj)
            } catch (e: Exception) {
                String.EMPTY
            }
        }
    }

}
