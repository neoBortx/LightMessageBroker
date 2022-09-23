package com.bortxapps.lightmessagebroker.messages

/**
 * Represent the bundle of data to send inside of a message
 * This class make easy the action of send and receive message making
 * transparent the way of handling data of the android message handler
 */
data class MessageBundle(

    /**
     * The identifier of the message, it can be used to identify the content of the message
     */
    val messageKey: Long,

    /**
     * The category that surrounds that message, because it can be a second level of filtering
     */
    val messageCategory: Long,

    /**
     * The data to send in the message
     * It can be anything. You can send complex object or primitive types
     */
    val messageData: Any,
)