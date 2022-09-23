/*
 * Copyright 2022 Borja Villarroya Rodriguez
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

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
    onMessageReceived: (clientId: Long, msgKey: Long, msgCategory: Long, payload: Any) -> Unit,
) {
    MessageQueueManager.attachHandler(
        clientId = clientId,
        supportedCategories = supportedCategories,
        onMessageReceived = onMessageReceived
    )
}

/**
 * Send the message to all clients subscribed to the system
 */
suspend fun sendBroadcastMessage(senderId: Long, messageKey: Long, categoryKey: Long, payload: Any) {
    MessageQueueManager.sendBroadcastMessage(senderId, messageKey, categoryKey, payload)
}

/**
 * Send the message to all clients subscribed to the system
 */
suspend fun sendMessageToClient(targetClientId: Long, messageKey: Long, payload: Any) {
    MessageQueueManager.sendMessageToClient(targetClientId, messageKey, payload)
}


@Suppress("unused")
fun removeHandler(clientId: Long) {
    MessageQueueManager.removeHandler(clientId)
}

fun clearAllHandlers() {
    MessageQueueManager.clearHandlers()
}