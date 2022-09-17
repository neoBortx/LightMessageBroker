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
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(application: Application) : AndroidViewModel(application), DefaultLifecycleObserver {

    //region private vars
    private var startTime = 0L
    private var numberMessages: Int = 0
    private var numberConsumer: Int = 0
    private var counters = mutableMapOf<Int, MessageReceiver>()
    //endregion

    //region state
    var uiState by mutableStateOf(ActivityState.getInitial())
        private set
    //endregion

    //region private functions
    private fun processMessage(category: Int, data: Any) {
        counters[category]?.processMessage(data)

        if (counters.toList().sumOf { it.second.counter } >= (numberMessages - 1)) {
            val endTime = System.currentTimeMillis()
            uiState = uiState.copy(isRunning = false,
                showResult = true,
                elapsedTime = (endTime - startTime).toString(),
                result = counters.mapValues { it.value.counter })
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
            ) { _, cat, data ->
                processMessage(cat.toInt(), data)
            }
        }

        if (uiState.numberMessages.isNotBlank() && uiState.numberMessages.toInt() > 0) {
            numberMessages = uiState.numberMessages.toInt()
            numberConsumer = uiState.numberConsumers.toInt()
            counters.clear()
            repeat(numberConsumer) { consumerIndex ->
                counters[consumerIndex] = MessageReceiver()
            }

            uiState = uiState.copy(isRunning = true, showResult = false)
            val data = workDataOf(NUMBER_MESSAGES to numberMessages, NUMBER_CONSUMERS to numberConsumer)

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

    fun getConsumerMessages(consumerId: Int): List<Int> {
        return counters[consumerId]?.messagesReceived ?: listOf()
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