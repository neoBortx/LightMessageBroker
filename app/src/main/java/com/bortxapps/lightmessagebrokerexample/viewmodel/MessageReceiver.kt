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