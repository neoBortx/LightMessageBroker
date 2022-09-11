package com.bortxapps.lightmessagebrokerexample.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.bortxapps.lightmessagebroker.messagehandler.attachMessageClient
import com.bortxapps.lightmessagebrokerexample.workers.ProducerWorker
import com.bortxapps.lightmessagebrokerexample.workers.ProducerWorker.Companion.NUMBER_MESSAGES
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    //region constants
    private val clientId = 1L
    private val categoryKey = 45124L
    //endregion

    //region private vars
    private var startTime = 0L
    private var numberMessages: Int = 0
    private var counter: Int = 0
    private var higherNumber: Int = 0
    //endregion

    //region state
    var uiState by mutableStateOf(ActivityState.getInitial())
        private set
    //endregion

    //region private functions
    private fun processMessage(data: Any) {
        counter++
        val num = data as Int
        if (higherNumber < num) higherNumber = num
        if (counter >= (numberMessages - 1) && higherNumber == (numberMessages - 1)) {
            val endTime = System.currentTimeMillis()
            uiState = uiState.copy(isRunning = false, showResult = true, elapsedTime = (endTime - startTime).toString())
        }
    }
    //endregion

    //region public functions
    fun register(lifecycle: Lifecycle) {
        uiState = uiState.copy(isRunning = false, showResult = false)

        attachMessageClient(
            clientId = clientId,
            supportedCategories = listOf(categoryKey),
            lifecycle = lifecycle
        ) { _, data -> processMessage(data) }
    }

    fun start() {
        if (uiState.numberMessages.isNotBlank() && uiState.numberMessages.toInt() > 0) {
            counter = 0
            higherNumber = 0
            numberMessages = uiState.numberMessages.toInt()
            uiState = uiState.copy(isRunning = true, showResult = false)
            val data = workDataOf(NUMBER_MESSAGES to numberMessages)

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
    //endregion

}