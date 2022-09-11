package com.bortxapps.lightmessagebrokerexample.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bortxapps.lightmessagebroker.messagehandler.sendMessage

class ProducerWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val categoryKey = 45124L
    private val messageKey = 68723673678L

    companion object {
        const val NUMBER_MESSAGES = "NUMBER_MESSAGES"
    }


    override suspend fun doWork(): Result {
        Log.i("Test", "Starting sending messages")
        val numberOfMessages = inputData.getInt(NUMBER_MESSAGES, 0)


        repeat(numberOfMessages) {
            sendMessage(2L, messageKey, categoryKey, it)
        }


        return Result.success()
    }
}