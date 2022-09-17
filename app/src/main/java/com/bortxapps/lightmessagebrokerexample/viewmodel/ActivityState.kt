package com.bortxapps.lightmessagebrokerexample.viewmodel

data class ActivityState(
    val isRunning: Boolean,
    val showResult: Boolean,
    val numberMessages: String,
    val numberConsumers: String,
    val elapsedTime: String,
    val result: Map<Int, Int>,
) {
    companion object {
        fun getInitial() = ActivityState(
            isRunning = false,
            showResult = false,
            numberMessages = "",
            numberConsumers = "",
            elapsedTime = "",
            result = mapOf()
        )
    }
}


