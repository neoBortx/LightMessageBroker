/*
 * Copyright 2022 Borja Villarroya Rodriguez
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.bortxapps.lightmessagebroker.manager

import android.util.Log
import com.bortxapps.lightmessagebroker.constants.Constants.NO_CATEGORY
import com.bortxapps.lightmessagebroker.exceptions.LightMessageBrokerException
import com.bortxapps.lightmessagebroker.messagehandler.MessageHandler
import com.bortxapps.lightmessagebroker.messages.MessageBundle

/**
 * This class manages the queue system built for internal app messaging. This system holds a map
 * with the message handlers of each registered client (one handler per client). You have to think
 * that each handles is like a mailbox and this class is a dispatcher system to send the message to
 * the right mailbox
 *
 * Object classes are like traditional singleton classes (but the implementation is under the hood)
 * with a live span of the application
 */
internal object MessageQueueManager {

    private val mListOfHandlersById: MutableMap<Long, MessageHandler> = mutableMapOf()

    private val mListOfHandlersBtCategory: MutableMap<Long, MutableList<MessageHandler>> = mutableMapOf()

    internal fun getListOfHandlersById() = mListOfHandlersById.toMap()
    internal fun getHandlersOfCategory(category: Long) = mListOfHandlersBtCategory[category]?.toList() ?: listOf()

    /**
     * Add a new message handler to the mailing system
     *
     * @remarks If the handler doesn't have any specified category it will received all messages.
     */
    fun attachHandler(
        clientId: Long,
        supportedCategories: List<Long> = listOf(),
        onMessageReceived: (clientId: Long, msgKey: Long, msgCategory: Long, payload: Any) -> Unit,
    ) {

        val messageHandler = generateMessageHandler(
            clientId = clientId,
            supportedCategories = supportedCategories,
            onMessageReceived = onMessageReceived
        )

        insertMessageHandler(messageHandler)
        messageHandler.startConsuming()
    }

    internal fun generateMessageHandler(
        clientId: Long,
        supportedCategories: List<Long> = listOf(),
        onMessageReceived: (clientId: Long, msgKey: Long, msgCategory: Long, payload: Any) -> Unit,
    ) =
        MessageHandler(
            clientId = clientId,
            supportedCategories = supportedCategories,
            onMessageReceived = onMessageReceived
        )

    internal fun insertMessageHandler(messageHandler: MessageHandler) {
        if (mListOfHandlersById.containsKey(messageHandler.clientId)) {
            throw LightMessageBrokerException(
                "There is already a message client with with id ${messageHandler.clientId}"
            )
        }
        mListOfHandlersById[messageHandler.clientId] = messageHandler

        messageHandler.supportedCategories.forEach {
            if (!mListOfHandlersBtCategory.containsKey(it)) {
                mListOfHandlersBtCategory[it] = mutableListOf()
            }

            if (mListOfHandlersBtCategory[it]?.none { handler -> handler.clientId == messageHandler.clientId } == true) {
                mListOfHandlersBtCategory[it]?.add(messageHandler)
            }
        }
    }

    /**
     * Remove the given message handler to the list of handlers
     *
     * @param handlerId: Message handler to remove
     */
    fun removeHandler(handlerId: Long) {
        try {
            if (mListOfHandlersById.containsKey(handlerId)) {
                Log.d("MessageQueueManager", "Removing queue handler $handlerId")
                mListOfHandlersById[handlerId]?.dispose()
                mListOfHandlersById.remove(handlerId)
            }

            mListOfHandlersBtCategory.values
                .forEach {
                    it.removeAll { handler -> handler.clientId == handlerId }
                }

            mListOfHandlersBtCategory
                .filter { it.value.isEmpty() }
                .map { it.key }
                .forEach {
                    mListOfHandlersBtCategory.remove(it)
                }

        } catch (ex: Exception) {
            throw LightMessageBrokerException("Unable to remove handler with id $handlerId", ex)
        }
    }

    fun clearHandlers() {
        //use to list to make a copy
        mListOfHandlersById.toList().forEach { removeHandler(it.first) }
        mListOfHandlersBtCategory.clear()
    }

    private fun buildMessage(messageKey: Long, category: Long, payload: Any): MessageBundle = MessageBundle(messageKey, category, payload)

    /**
     * Send the given data to all clients that are subscribe to the given category
     *
     * @remarks If the category key is empty the message will be sent to all mailboxes, this could
     * provoke an overload in the system. Try to set always a category
     */
    suspend fun sendBroadcastMessage(
        senderId: Long,
        messageKey: Long,
        categoryKey: Long = NO_CATEGORY,
        payload: Any,
    ) {
        val message = buildMessage(messageKey, categoryKey, payload)
        if (categoryKey == NO_CATEGORY) {
            sendMessagesWithoutCategory(senderId, message)
        } else {
            sendMessagesWithCategory(senderId, message, categoryKey)
        }
    }

    /**
     * Send the given data to the specified client
     *
     * @remarks If the category key is empty the message will be sent to all mailboxes, this could
     * provoke an overload in the system. Try to set always a category
     */
    suspend fun sendMessageToClient(
        targetClientId: Long,
        messageKey: Long,
        payload: Any,
    ) {
        val message = buildMessage(messageKey, NO_CATEGORY, payload)

        sendMessageToOneClient(targetClientId, message)
    }


    internal suspend fun sendMessageToOneClient(targetClientId: Long, message: MessageBundle) {
        try {
            mListOfHandlersById[targetClientId]!!.postMessage(message)
        } catch (ex: LightMessageBrokerException) {
            throw ex
        } catch (ex: Exception) {
            throw LightMessageBrokerException("TargetClientId Id: $targetClientId, unable to send message ", ex)
        }
    }

    /**
     * Send the given data using the message key identifier
     * and the data
     *
     * The message is sent to all message handlers attached to our custom message system
     */
    internal suspend fun sendMessagesWithoutCategory(senderId: Long, message: MessageBundle) {
        try {
            mListOfHandlersById
                .filter { it.key != senderId }
                .ifEmpty {
                    throw LightMessageBrokerException("Client Id: $senderId, There isn't any more clients available in the system")
                }
                .forEach {
                    it.value.postMessage(message)
                }
        } catch (ex: LightMessageBrokerException) {
            throw ex
        } catch (ex: Exception) {
            throw LightMessageBrokerException("Client Id: $senderId, unable to send message ", ex)
        }
    }

    /**
     * Send the given data using the message key identifier
     * and the data
     *
     * The message is sent only to handlers that doesn't have any filter keyword or contains
     * the filter keyword in their list of filters
     */
    internal suspend fun sendMessagesWithCategory(senderId: Long, message: MessageBundle, categoryId: Long) {
        try {
            mListOfHandlersBtCategory[message.messageCategory]!!
                .filter {
                    it.clientId != senderId
                }.ifEmpty {
                    throw LightMessageBrokerException("Client Id: $senderId, There isn't any client expecting messages for category $categoryId")
                }.forEach {
                    it.postMessage(message)
                }
        } catch (ex: LightMessageBrokerException) {
            throw ex
        } catch (ex: Exception) {
            throw LightMessageBrokerException(
                "Client Id: $senderId, unable to send message with category",
                ex
            )
        }
    }
}