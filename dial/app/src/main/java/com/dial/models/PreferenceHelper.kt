package com.dial.models

import android.content.Context
import android.preference.PreferenceManager

object PreferenceHelper {

    const val DIAL_DEVICES = "dial_devices"
    const val DIAL_LAST_DISCOVER_TIME = "last_discover_time"

    private lateinit var mContext: Context

    fun init(context: Context) {
        mContext = context.applicationContext
    }

    fun set(key: String, value: Any?) {
        set(mContext, key, value)
    }

    fun getString(context: Context, key: String?, defaultValue: String?): String? {
        val globalPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)
        return globalPreferences.getString(key, defaultValue)
    }

    fun getString(key: String?, defaultValue: String?): String? {
        return getString(mContext, key, defaultValue)
    }

    fun getInt(key: String?, defaultValue: Int): Int {
        val globalPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        return globalPreferences.getInt(key, defaultValue)
    }

    fun getBoolean(key: String?, defaultValue: Boolean): Boolean {
        val globalPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        return globalPreferences.getBoolean(key, defaultValue)
    }

    fun getLong(key: String?, defaultValue: Long): Long {
        val globalPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        return globalPreferences.getLong(key, defaultValue)
    }

    fun contains(key: String?): Boolean? {
        return PreferenceManager.getDefaultSharedPreferences(mContext).contains(key)
    }

    private fun set(context: Context, key: String, value: Any?) {
        val globalPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = globalPreferences.edit()
        when (value) {
            null -> {
                editor.putString(key, null)
            }
            is Int -> {
                editor.putInt(key, value)
            }
            is Float -> {
                editor.putFloat(key, value)
            }
            is Boolean -> {
                editor.putBoolean(key, value)
            }
            is Long -> {
                editor.putLong(key, value)
            }
            is String -> {
                editor.putString(key, value)
            }
        }
        editor.apply()
    }
}