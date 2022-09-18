package com.bortxapps.lightmessagebrokerexample.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.bortxapps.lightmessagebroker.messagehandler.attachMessageClient
import com.bortxapps.lightmessagebroker.messagehandler.clearAllHandlers
import com.bortxapps.lightmessagebrokerexample.workers.ProducerWorker
import com.bortxapps.lightmessagebrokerexample.workers.WorkerConstants.NUMBER_CONSUMERS
import com.bortxapps.lightmessagebrokerexample.workers.WorkerConstants.NUMBER_MESSAGES
import com.bortxapps.lightmessagebrokerexample.workers.WorkerConstants.SEND_TO_ALL_CLIENTS_ONE_BY_ONE
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(application: Application) : AndroidViewModel(application), DefaultLifecycleObserver {

    //region private vars
    private var startTime = 0L
    private var numberMessages: Int = 0
    private var numberConsumer: Int = 0

    /**
     * The index can be the message category or clientId depending on the chosen method to send message
     */
    private var messageReceivers = mutableMapOf<Long, MessageReceiver>()
    //endregion

    //region state
    var uiState by mutableStateOf(ActivityState.getInitial())
        private set
    //endregion

    //region private functions
    private fun processMessage(clientId: Long, category: Long, data: Any) {

        val index = if (uiState.sendByClientId) {
            clientId
        } else {
            category
        }

        val limitMessages = if (uiState.sendByClientId) {
            numberMessages * numberConsumer
        } else {
            numberMessages
        }

        messageReceivers[index]?.processMessage(data)

        if (messageReceivers.toList().sumOf { it.second.counter } > (limitMessages - 1)) {
            val endTime = System.currentTimeMillis()
            uiState = uiState.copy(isRunning = false,
                showResult = true,
                elapsedTime = (endTime - startTime).toString(),
                result = messageReceivers.mapValues { it.value.counter })
        }
    }
    //endregion

    //region public functions
    fun register(lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
        uiState = uiState.copy(isRunning = false, showResult = false)
    }

    fun start() {
        clearAllHandlers()
        repeat(uiState.numberConsumers.toInt()) {
            attachMessageClient(
                clientId = it.toLong(),
                supportedCategories = listOf(it.toLong())
            ) { clientId, _, cat, data ->
                processMessage(clientId, cat, data)
            }
        }

        if (uiState.numberMessages.isNotBlank() && uiState.numberMessages.toInt() > 0) {
            numberMessages = uiState.numberMessages.toInt()
            numberConsumer = uiState.numberConsumers.toInt()
            messageReceivers.clear()
            repeat(numberConsumer) { consumerIndex ->
                messageReceivers[consumerIndex.toLong()] = MessageReceiver()
            }

            uiState = uiState.copy(isRunning = true, showResult = false)
            val data = workDataOf(
                NUMBER_MESSAGES to numberMessages,
                NUMBER_CONSUMERS to numberConsumer,
                SEND_TO_ALL_CLIENTS_ONE_BY_ONE to uiState.sendByClientId
            )

            val producerWorker: WorkRequest = OneTimeWorkRequestBuilder<ProducerWorker>()
                .setInputData(data)
                .build()

            WorkManager.getInstance(this.getApplication<Application>().applicationContext)
                .enqueue(producerWorker)

            startTime = System.currentTimeMillis()
        }
    }

    fun setMessages(messages: String) {
        uiState = uiState.copy(numberMessages = messages)
    }

    fun setConsumers(consumers: String) {
        uiState = uiState.copy(numberConsumers = consumers)
    }

    fun getConsumerMessages(consumerId: Long): List<Int> {
        return messageReceivers[consumerId]?.messagesReceived ?: listOf()
    }

    fun onSendByClientIdChanged(value: Boolean) {
        uiState = uiState.copy(sendByClientId = value)
    }
    //endregion

    //region LifecycleObserver
    /**
     * @remarks If the client that is using this handlers is destroyed, the resources associated
     * to it will be cleared
     */
    override fun onDestroy(owner: LifecycleOwner) {
        clearAllHandlers()
        super.onDestroy(owner)
    }
    //endregion
}