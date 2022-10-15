/*
 * Copyright 2022 Borja Villarroya Rodriguez
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.bortxapps.lightmessagebroker

import android.util.Log
import com.bortxapps.lightmessagebroker.constants.Constants
import com.bortxapps.lightmessagebroker.exceptions.LightMessageBrokerException
import com.bortxapps.lightmessagebroker.manager.MessageQueueManager
import com.bortxapps.lightmessagebroker.messagehandler.MessageHandler
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MessageQueueManagerTest {

    @Before
    fun setup() {
        mockkObject(MessageQueueManager)
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
    }

    @After
    fun dispose() {
        MessageQueueManager.clearHandlers()
        unmockkObject(MessageQueueManager)
        unmockkStatic(Log::class)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onMessageReceived(clientId: Long, msgKey: Long, msgCategory: Long, payload: Any) {

    }

    @Test
    fun `attachHandler new handler`() {

        val handler = mockk<MessageHandler>(relaxed = true)
        every { handler.startConsuming() } returns Unit
        every { handler.supportedCategories } returns listOf(56, 23, 5)
        every { handler.clientId } returns 27L
        every { MessageQueueManager.generateMessageHandler(27L, listOf(56, 23, 5), ::onMessageReceived) } returns handler

        MessageQueueManager.attachHandler(27L, listOf(56, 23, 5), ::onMessageReceived)


        verify { MessageQueueManager.insertMessageHandler(handler) }
        verify { handler.startConsuming() }

        assertTrue(MessageQueueManager.getListOfHandlersById().contains(27L))
        assertTrue(MessageQueueManager.getHandlersOfCategory(56).any { it.clientId == 27L })
        assertTrue(MessageQueueManager.getHandlersOfCategory(23).any { it.clientId == 27L })
        assertTrue(MessageQueueManager.getHandlersOfCategory(5).any { it.clientId == 27L })
    }

    @Test
    fun `remove handler`() {

        MessageQueueManager.attachHandler(42L, listOf(56, 23, 5), ::onMessageReceived)
        MessageQueueManager.removeHandler(42L)

        assertTrue(MessageQueueManager.getListOfHandlersById().isEmpty())
        assertTrue(MessageQueueManager.getHandlersOfCategory(56).isEmpty())
        assertTrue(MessageQueueManager.getHandlersOfCategory(23).isEmpty())
        assertTrue(MessageQueueManager.getHandlersOfCategory(5).isEmpty())
    }

    @Test
    fun `remove unknown handler no exception expected`() {
        MessageQueueManager.removeHandler(52L)
    }

    @Test
    fun `several handlers of different categories and send broadcast message to one category expect message filtered to category`() {

        val receivers = mutableListOf<Long>()
        var receivedKeyValue = 0L
        var receivedMessageCategory = 0L
        var receivedPayload = ""

        repeat(5) {
            MessageQueueManager.attachHandler(it.toLong(), listOf(5)) { clientId, msgKey, msgCategory, payload ->
                receivers.add(clientId)
                receivedKeyValue = msgKey
                receivedMessageCategory = msgCategory
                receivedPayload = payload as String
            }
        }
        for (i in 6..30) {
            MessageQueueManager.attachHandler(i.toLong(), listOf(6), ::onMessageReceived)
        }

        runBlocking {
            MessageQueueManager.sendBroadcastMessage(9655L, 556L, 5L, "payload")

            withContext(Dispatchers.IO) {
                coVerify { MessageQueueManager.sendMessagesWithCategory(9655L, any(), 5L) }
            }
        }

        assertEquals(5, receivers.count())
        assertEquals(5L, receivedMessageCategory)
        assertEquals("payload", receivedPayload)
        assertEquals(556L, receivedKeyValue)
    }

    @Test
    fun `several handlers of different categories and send broadcast message without category expect message send to all of them`() {

        val receivers = mutableListOf<Long>()
        var receivedKeyValue = 0L
        var receivedMessageCategory = 0L
        var receivedPayload = ""

        repeat(30) {
            MessageQueueManager.attachHandler(it.toLong(), listOf(it.toLong())) { clientId, msgKey, msgCategory, payload ->
                receivers.add(clientId)
                receivedKeyValue = msgKey
                receivedMessageCategory = msgCategory
                receivedPayload = payload as String
            }
        }

        runBlocking {
            MessageQueueManager.sendBroadcastMessage(
                senderId = 9655L,
                messageKey = 42L,
                categoryKey = Constants.NO_CATEGORY,
                payload = "payload"
            )
            coVerify { MessageQueueManager.sendMessagesWithoutCategory(9655L, any()) }
        }

        assertEquals(30, receivers.count())
        assertEquals(Constants.NO_CATEGORY, receivedMessageCategory)
        assertEquals("payload", receivedPayload)
        assertEquals(42L, receivedKeyValue)
    }

    @Test
    fun `send broadcast message with category with no handlers expect exception`() {

        var exceptionHandled = false

        runBlocking {
            try {
                MessageQueueManager.sendBroadcastMessage(9655L, 556L, 5L, "payload")
            } catch (ex: LightMessageBrokerException) {
                exceptionHandled = true
            }

            assertTrue(exceptionHandled)
        }
    }

    @Test
    fun `send broadcast message without category with no handlers expect exception`() {
        var exceptionHandled = false

        runBlocking {
            try {
                MessageQueueManager.sendBroadcastMessage(
                    senderId = 9655L,
                    messageKey = 42L,
                    categoryKey = Constants.NO_CATEGORY,
                    payload = "payload"
                )
            } catch (ex: LightMessageBrokerException) {
                exceptionHandled = true
            }

            assertTrue(exceptionHandled)
        }
    }

    @Test
    fun `send broadcast message to wrong category with no handlers expect exception`() {
        var exceptionHandled = false

        MessageQueueManager.attachHandler(1L, listOf(5)) { _, _, _, _ -> }

        runBlocking {
            try {
                MessageQueueManager.sendBroadcastMessage(
                    senderId = 9655L,
                    messageKey = 42L,
                    categoryKey = 89854L,
                    payload = "payload"
                )
            } catch (ex: LightMessageBrokerException) {
                exceptionHandled = true
            }

            assertTrue(exceptionHandled)
        }
    }


    @Test
    fun `One handlers send message to one of them category expect message send to just one of them`() {

        val receivers = mutableListOf<Long>()
        var receivedKeyValue = 0L
        var receivedMessageCategory = 0L
        var receivedPayload = ""

        MessageQueueManager.attachHandler(1L, listOf(5)) { clientId, msgKey, msgCategory, payload ->
            receivers.add(clientId)
            receivedKeyValue = msgKey
            receivedMessageCategory = msgCategory
            receivedPayload = payload as String
        }

        runBlocking {
            MessageQueueManager.sendMessageToClient(1L, 565L, "payload")
            coVerify { MessageQueueManager.sendMessageToOneClient(1L, any()) }
        }

        assertEquals(1, receivers.count())
        assertEquals(Constants.NO_CATEGORY, receivedMessageCategory)
        assertEquals("payload", receivedPayload)
        assertEquals(565L, receivedKeyValue)

    }

    @Test
    fun `Several handlers with same category and send message to one of them category expect message send to just one of them`() {

        val receivers = mutableListOf<Long>()

        repeat(10) {
            MessageQueueManager.attachHandler(it.toLong(), listOf(5)) { clientId, _, _, _ ->
                receivers.add(clientId)
            }
        }

        runBlocking {
            MessageQueueManager.sendMessageToClient(7L, 565L, "payload")
            coVerify { MessageQueueManager.sendMessageToOneClient(7L, any()) }
        }

        assertEquals(1, receivers.count())
        assertEquals(7L, receivers[0])
    }

    @Test
    fun `Several handlers with same category and send message to all of them of them category expect message send to just one of them`() {

        val receivers = mutableListOf<Long>()
        val expectedResult = mutableListOf<Long>()

        repeat(30) {
            expectedResult.add(it.toLong())
            MessageQueueManager.attachHandler(it.toLong(), listOf(5)) { clientId, _, _, _ ->
                receivers.add(clientId)
            }
        }

        runBlocking {
            repeat(30) {
                MessageQueueManager.sendMessageToClient(it.toLong(), 565L, "payload")
            }
            coVerify { MessageQueueManager.sendMessageToOneClient(7L, any()) }
        }

        assertEquals(expectedResult.count(), receivers.count())
    }

    @Test
    fun `send one message with no handlers expect exception`() {
        var exceptionHandled = false

        runBlocking {
            try {
                MessageQueueManager.sendMessageToClient(10L, 565L, "payload")
            } catch (ex: LightMessageBrokerException) {
                exceptionHandled = true
            }

            assertTrue(exceptionHandled)
        }
    }

    @Test
    fun `send one message to wrong client expect exception`() {
        var exceptionHandled = false

        MessageQueueManager.attachHandler(1L, listOf(5)) { _, _, _, _ -> }

        runBlocking {
            try {
                MessageQueueManager.sendMessageToClient(10L, 565L, "payload")
            } catch (ex: LightMessageBrokerException) {
                exceptionHandled = true
            }

            assertTrue(exceptionHandled)
        }
    }
}