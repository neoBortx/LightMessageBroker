/*
 * Copyright 2022 Borja Villarroya Rodriguez
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.bortxapps.lightmessagebrokerexample.viewmodel

data class ActivityState(
    val isRunning: Boolean,
    val showResult: Boolean,
    val numberMessages: String,
    val numberConsumers: String,
    val elapsedTime: String,
    val result: Map<Long, Int>,
    val sendByClientId: Boolean,
) {
    companion object {
        fun getInitial() = ActivityState(
            isRunning = false,
            showResult = false,
            numberMessages = "",
            numberConsumers = "",
            elapsedTime = "",
            result = mapOf(),
            sendByClientId = false
        )
    }
}


