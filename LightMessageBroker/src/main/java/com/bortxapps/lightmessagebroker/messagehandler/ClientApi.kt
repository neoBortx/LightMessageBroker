package com.bortxapps.lightmessagebroker.messagehandler

import com.bortxapps.lightmessagebroker.manager.MessageQueueManager


/**
 * Add the current handler to the list of handlers that are listening messages
 * This handler will have a list of keywords used to filter messages at send time
 * A handler with filter keyword only will send messages to handlers that contains at least
 * one similar keyword
 */
fun attachMessageClient(
    clientId: Long,
    supportedCategories: List<Long> = listOf(),
    onMessageReceived: (msgKey: Long, msgCategory: Long, payload: Any) -> Unit,
) {
    MessageQueueManager.attachHandler(
        clientId = clientId,
        supportedCategories = supportedCategories,
        onMessageReceived = onMessageReceived
    )
}

/**
 * Send the given data using the message key identifier
 * and the data
 *
 */
suspend fun sendMessage(clientId: Long, messageKey: Long, categoryKey: Long, payload: Any) {
    MessageQueueManager.sendMessageToManager(clientId, messageKey, categoryKey, payload)
}


@Suppress("unused")
fun removeHandler(clientId: Long) {
    MessageQueueManager.removeHandler(clientId)
}

fun clearAllHandlers() {
    MessageQueueManager.clearHandlers()
}