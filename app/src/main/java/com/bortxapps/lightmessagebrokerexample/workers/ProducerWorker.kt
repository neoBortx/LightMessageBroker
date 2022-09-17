package com.bortxapps.lightmessagebrokerexample.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bortxapps.lightmessagebroker.messagehandler.sendMessage
import com.bortxapps.lightmessagebrokerexample.workers.WorkerConstants.NUMBER_CONSUMERS
import com.bortxapps.lightmessagebrokerexample.workers.WorkerConstants.NUMBER_MESSAGES
import java.util.*
import kotlin.random.Random

class ProducerWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val messageKey = 68723673678L
    private val clientID: Long = UUID.randomUUID().mostSignificantBits

    /**
     * Each consumer has its own category so send messages to all consumer in a random way
     */
    override suspend fun doWork(): Result {

        val numberOfMessages = inputData.getInt(NUMBER_MESSAGES, 0)
        val numberCostumers = inputData.getInt(NUMBER_CONSUMERS, 0)

        repeat(numberOfMessages) {
            sendMessage(clientID, messageKey, Random.nextLong(numberCostumers.toLong()), it)
        }
        return Result.success()
    }
}