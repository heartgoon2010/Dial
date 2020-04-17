package com.dial.models

data class DialAppModel(val name: String, val state: String, val lastUrl: String) {

    companion object {
        val EMPTY = DialAppModel(String.EMPTY, String.EMPTY, String.EMPTY)
    }

    override fun toString(): String {
        return "DialAppModel: name=$name, state=$state, lastUrl=$lastUrl"
    }
}