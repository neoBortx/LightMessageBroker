/*
 * Copyright 2022 Borja Villarroya Rodriguez
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.bortxapps.lightmessagebrokerexample.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bortxapps.lightmessagebroker.messagehandler.sendBroadcastMessage
import com.bortxapps.lightmessagebroker.messagehandler.sendMessageToClient
import com.bortxapps.lightmessagebrokerexample.workers.WorkerConstants.NUMBER_CONSUMERS
import com.bortxapps.lightmessagebrokerexample.workers.WorkerConstants.NUMBER_MESSAGES
import com.bortxapps.lightmessagebrokerexample.workers.WorkerConstants.SEND_TO_ALL_CLIENTS_ONE_BY_ONE
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
        val sendToAllClientsOneByOne = inputData.getBoolean(SEND_TO_ALL_CLIENTS_ONE_BY_ONE, true)


        repeat(numberOfMessages) { messageData ->
            if (sendToAllClientsOneByOne) {
                repeat(numberCostumers) { clientID ->
                    sendMessageToClient(
                        targetClientId = clientID.toLong(),
                        messageKey = messageKey,
                        payload = messageData
                    )
                }
            } else {
                sendBroadcastMessage(
                    senderId = clientID,
                    messageKey = messageKey,
                    categoryKey = Random.nextLong(numberCostumers.toLong()),
                    payload = messageData
                )
            }
        }
        return Result.success()
    }
}