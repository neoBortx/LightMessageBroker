package com.bortxapps.lightmessagebroker.manager

import android.util.Log
import androidx.lifecycle.Lifecycle
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

    private val mListOfHandlers: MutableMap<Long, MessageHandler> = mutableMapOf()

    /**
     * Add a new message handler to the mailing system
     *
     * @remarks If the handler doesn't have any specified category it will received all messages.
     */
    fun attachHandler(
        clientId: Long,
        supportedCategories: List<Long> = listOf(),
        onMessageReceived: (Long, Any) -> Unit,
        lifecycle: Lifecycle
    ) {

        val messageHandler = MessageHandler(
            clientId = clientId,
            supportedCategories = supportedCategories,
            onMessageReceived = onMessageReceived,
            lifecycle = lifecycle,
            onListenerDestroyed = MessageQueueManager::removeHandler
        )

        if (mListOfHandlers.containsKey(messageHandler.clientId)) {
            throw LightMessageBrokerException(
                "There is already a message client with with id ${messageHandler.clientId}"
            )
        }
        mListOfHandlers[messageHandler.clientId] = messageHandler
        messageHandler.startConsuming()
    }

    /**
     * Remove the given message handler to the list of handlers
     *
     * @param handlerId: Message handler to remove
     */
    private fun removeHandler(handlerId: Long) {
        try {
            if (mListOfHandlers.containsKey(handlerId)) {
                Log.d("MessageQueueManager", "Removing queue handler $handlerId")
                mListOfHandlers[handlerId]?.dispose()
                mListOfHandlers.remove(handlerId)
            }
        } catch (ex: Exception) {
            throw LightMessageBrokerException("Unable to remove handler with id $handlerId", ex)
        }
    }

    private fun buildMessage(messageKey: Long, payload: Any): MessageBundle = MessageBundle(messageKey, payload)

    /**
     * Send the given data using the message key identifier
     *
     * @remarks If the category key is empty the message will be sent to all mailboxes, this could
     * provoke an overload in the system. Try to set always a category
     */
    suspend fun sendMessageToManager(
        senderId: Long,
        messageKey: Long,
        categoryKey: Long = NO_CATEGORY,
        payload: Any
    ) {
        val message = buildMessage(messageKey, payload)
        if (categoryKey == NO_CATEGORY) {
            sendMessagesWithoutCategory(senderId, message)
        } else {
            sendMessagesWithCategory(senderId, message, categoryKey)
        }
    }

    /**
     * Send the given data using the message key identifier
     * and the data
     *
     * The message is sent to all message handlers attached to our custom message system
     *
     * @param message: the message to send
     * @param clientId: The identifier of the message handler
     */
    private suspend fun sendMessagesWithoutCategory(clientId: Long, message: MessageBundle) {
        try {
            mListOfHandlers
                .filter { it.key != clientId }
                .ifEmpty {
                    throw LightMessageBrokerException("Client Id: $clientId, There isn't any more clients available in the system")
                }
                .forEach {
                    Log.d("test", "dispatching message")
                    it.value.postMessage(message)
                }
        } catch (ex: LightMessageBrokerException) {
            throw ex
        } catch (ex: Exception) {
            throw LightMessageBrokerException("Client Id: $clientId, unable to send message ", ex)
        }
    }

    /**
     * Send the given data using the message key identifier
     * and the data
     *
     * The message is sent only to handlers that doesn't have any filter keyword or contains
     * the filter keyword in their list of filters
     */
    private suspend fun sendMessagesWithCategory(clientId: Long, message: MessageBundle, categoryId: Long) {
        try {
            mListOfHandlers
                .filter { it.key != clientId }
                .filter {
                    it.value.supportedCategories.isEmpty() || it.value.supportedCategories.contains(
                        categoryId
                    )
                }
                .ifEmpty {
                    throw LightMessageBrokerException("Client Id: $clientId, There isn't any client expecting messages for category $categoryId")
                }
                .forEach {
                    it.value.postMessage(message)
                }
        } catch (ex: LightMessageBrokerException) {
            throw ex
        } catch (ex: Exception) {
            throw LightMessageBrokerException(
                "Client Id: $clientId, unable to send message with category",
                ex
            )
        }
    }
}