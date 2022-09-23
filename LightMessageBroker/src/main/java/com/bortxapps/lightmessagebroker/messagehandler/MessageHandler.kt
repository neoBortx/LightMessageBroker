/*
 * Copyright (c) 2020. Borja Villarroya Rodriguez, All rights reserved
 */

package com.bortxapps.lightmessagebroker.messagehandler

import com.bortxapps.lightmessagebroker.messages.MessageBundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

internal class MessageHandler(
    val clientId: Long,
    val supportedCategories: List<Long>,
    private val onMessageReceived: (clientId: Long, msgKey: Long, msgCategory: Long, payload: Any) -> Unit,
    queueLength: Int = 100,
) {

    private var disposed = false
    private val sharedFlow = MutableSharedFlow<MessageBundle>(extraBufferCapacity = queueLength)
    private val scope = CoroutineScope(Dispatchers.Default)
    private lateinit var job: Job

    //region public functions
    fun dispose() {
        if (!disposed && ::job.isInitialized) {
            job.cancel()
            disposed = true
        }
    }

    fun startConsuming() {
        job = getConsumingJob()
        job.start()
    }

    suspend fun postMessage(message: MessageBundle) {
        sharedFlow.emit(message)
    }
    //endregion

    //region handler
    /**
     * Convert the message object of the message queue to a easy readable object form upper layers
     * forward the message to the client through a callback method
     *
     * @param msg: The message to handle
     */
    private fun handleMessage(msg: MessageBundle) {
        if (disposed) return
        onMessageReceived(clientId, msg.messageKey, msg.messageCategory, msg.messageData)
    }
    //endregion

    //region private functions
    private fun getConsumingJob(): Job {
        return scope.launch {
            sharedFlow.collect {
                handleMessage(it)
            }
        }
    }
    //endregion

}