package com.bortxapps.lightmessagebrokerexample.viewmodel

data class ActivityState(val isRunning: Boolean, val showResult: Boolean, val numberMessages: String, val elapsedTime: String) {
    companion object {
        fun getInitial() = ActivityState(isRunning = false, showResult = false, numberMessages = "", elapsedTime = "")
    }
}


