package com.dial.presenters.utils

import android.util.Base64

class Base64 {

    companion object {
        fun encode(input: String): String {
            return Base64.encodeToString(input.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        }

        fun decode(input: String): String {
            val data = Base64.decode(input, Base64.NO_WRAP)
            return String(data, Charsets.UTF_8)
        }
    }
}