/*
 * Copyright 2022 Borja Villarroya Rodriguez
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.bortxapps.lightmessagebrokerexample.viewmodel

class MessageReceiver {

    var counter = 0
        private set

    var messagesReceived = mutableListOf<Int>()
        private set

    fun processMessage(data: Any) {
        val num = data as Int
        messagesReceived.add(num)
        counter++
    }
}