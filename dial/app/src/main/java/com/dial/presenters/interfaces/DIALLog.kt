package com.dial.presenters.interfaces

import android.text.TextUtils
import android.util.Log

class DIALLog {

    companion object {
        private val TAG = DIALLog::class.simpleName

        fun d(tag: String?, msg: String?): Int {
            return if (TextUtils.isEmpty(tag)) {
                Log.d(TAG, msg)
            } else {
                Log.d(tag, msg)
            }
        }

        fun e(tag: String?, msg: String?): Int {
            return if (TextUtils.isEmpty(tag)) {
                Log.e(TAG, msg)
            } else {
                Log.e(tag, msg)
            }
        }

        fun w(tag: String?, msg: String?): Int {
            return if (TextUtils.isEmpty(tag)) {
                Log.w(TAG, msg)
            } else {
                Log.w(tag, msg)
            }
        }
    }
}