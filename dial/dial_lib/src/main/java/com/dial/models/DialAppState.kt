package com.dial.models

enum class DialAppState(private val stateName: String) {
    RUNNING("running"),
    STOPPED("stopped"),
    ACTIVE("active"), // for roku
    UNKNOWN("");

    companion object {
        fun getStateByName(stateName: String): DialAppState {
            for (item in values()) {
                if (item.stateName == stateName) {
                    return item
                }
            }
            return UNKNOWN
        }

        fun isRunningState(stateName: String): Boolean {
            val state = getStateByName(stateName)
            return state == RUNNING || state == ACTIVE
        }
    }

    fun getStateName() = stateName
}