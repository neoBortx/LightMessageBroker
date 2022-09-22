package com.bortxapps.lightmessagebroker

import com.bortxapps.lightmessagebroker.manager.MessageQueueManager
import com.bortxapps.lightmessagebroker.messagehandler.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class ClientApiTest {


    @Before
    fun configure() {
        mockkObject(MessageQueueManager)

        coEvery { MessageQueueManager.sendMessageToClient(any(), any(), any()) } returns Unit
    }

    @Test
    fun `test api attachMessageClient calls MessageQueueManager`() {

        every { MessageQueueManager.attachHandler(1, listOf(45L, 37L), any()) } returns Unit

        attachMessageClient(1, listOf(45L, 37L)) { _, _, _, _ ->
        }

        verify { MessageQueueManager.attachHandler(1, listOf(45L, 37L), any()) }
    }

    @Test
    fun `test api clearAllHandlers calls MessageQueueManager`() {

        every { MessageQueueManager.clearHandlers() } returns Unit

        clearAllHandlers()

        verify { MessageQueueManager.clearHandlers() }
    }

    @Test
    fun `test api removeHandler calls MessageQueueManager`() {

        every { MessageQueueManager.removeHandler(any()) } returns Unit

        removeHandler(1L)

        verify { MessageQueueManager.removeHandler(1L) }
    }

    @Test
    fun `test api sendBroadcastMessage calls MessageQueueManager`() {

        coEvery { MessageQueueManager.sendBroadcastMessage(any(), any(), any(), any()) } returns Unit

        runBlocking {
            sendBroadcastMessage(1L, 2L, 3L, 4)
        }

        coVerify { MessageQueueManager.sendBroadcastMessage(1L, 2L, 3L, 4) }
    }

    @Test
    fun `test api sendMessageToClient calls MessageQueueManager`() {

        coEvery { MessageQueueManager.sendMessageToClient(any(), any(), any()) } returns Unit

        runBlocking {
            sendMessageToClient(1L, 2L, 3)
        }

        coVerify { MessageQueueManager.sendMessageToClient(1L, 2L, 3) }
    }
}