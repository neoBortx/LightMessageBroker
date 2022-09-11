package com.bortxapps.lightmessagebroker.exceptions

class LightMessageBrokerException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, ex: Exception) : super(message, ex)
}