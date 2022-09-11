/*
 * Copyright (c) 2020. Borja Villarroya Rodriguez, All rights reserved
 */

package com.bortxapps.lightmessagebroker.messagehandler

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.bortxapps.lightmessagebroker.exceptions.LightMessageBrokerException
import com.bortxapps.lightmessagebroker.messages.MessageBundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/**
 * This class receive a message from the android message queue
 *
 * The reason to make the message reception in this way is to avoid memory leaks:
 * This Handler class should be static or leaks might occur (anonymous android.os.Handler)
 * https://stackoverflow.com/questions/52025220/how-to-use-handler-and-handlemessage-in-kotlin
 * https://developer.android.com/reference/java/lang/ref/WeakReference
 */
internal class MessageHandler(
    val clientId: Long,
    val supportedCategories: List<Long>,
    val lifecycle: Lifecycle,
    private val onMessageReceived: (Long, Any) -> Unit,
    private val onListenerDestroyed: (Long) -> Unit,
    queueLength: Int = 100
) : DefaultLifecycleObserver {

    private var disposed = false
    private val sharedFlow = MutableSharedFlow<MessageBundle>(extraBufferCapacity = queueLength)
    private val scope = CoroutineScope(Dispatchers.Default)
    private val job = getConsumingJob()

    init {
        lifecycle.addObserver(this)
    }

    //region public functions
    fun dispose() {
        if (!disposed) {
            job.cancel()
            disposed = true
        }
    }

    fun startConsuming() {
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
        try {
            if (disposed) return
            onMessageReceived(msg.messageKey, msg.messageData)
        } catch (ex: Exception) {
            throw LightMessageBrokerException(
                "Client ID: $clientId, Unable to handle message",
                ex
            )
        }
    }
    //endregion

    //region LifecycleObserver
    /**
     * @remarks If the client that is using this handlers is destroyed, the resources associated
     * to it will be cleared
     */
    override fun onDestroy(owner: LifecycleOwner) {
        try {
            super.onDestroy(owner)
            dispose()
            onListenerDestroyed(clientId)
        } catch (ex: Exception) {
            throw LightMessageBrokerException(
                "Client ID: $clientId, Unable to free resources",
                ex
            )
        }
    }

    private fun getConsumingJob(): Job {
        return scope.launch {
            sharedFlow.collect {
                handleMessage(it)
            }
        }
    }
    //endregion

}