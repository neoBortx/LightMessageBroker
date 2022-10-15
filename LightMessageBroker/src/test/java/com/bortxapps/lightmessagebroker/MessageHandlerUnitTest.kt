/*
 * Copyright 2022 Borja Villarroya Rodriguez
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.bortxapps.lightmessagebroker

import androidx.lifecycle.Lifecycle
import com.bortxapps.lightmessagebroker.messagehandler.MessageHandler
import com.bortxapps.lightmessagebroker.messages.MessageBundle
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.lang.Thread.sleep

class MessageHandlerUnitTest {


    private val lifecycle = mockk<Lifecycle>(relaxed = true)

    @Before
    fun setUp() {
        every { lifecycle.addObserver(any()) } returns Unit

    }

    @Test
    fun `dispose handler first time expect disposed`() {
        val handler = spyk(MessageHandler(1L, listOf(5, 6, 7), onMessageReceived = { _, _, _, _ -> }))
        val job = mockk<Job>()

        every { job.start() } returns true
        every { job.cancel() } returns Unit
        every { handler["getConsumingJob"]() } returns job

        handler.startConsuming()
        handler.dispose()

        verify { job.cancel() }
    }

    @Test
    fun `dispose handler before init time expect no disposed`() {
        val handler = spyk(MessageHandler(1L, listOf(5, 6, 7), onMessageReceived = { _, _, _, _ -> }))
        val job = mockk<Job>()

        every { job.start() } returns true
        every { job.cancel() } returns Unit
        every { handler["getConsumingJob"]() } returns job

        handler.dispose()

        verify(exactly = 0) { job.cancel() }
    }

    @Test
    fun `startConsuming expect job created and job started`() {
        val handler = spyk(MessageHandler(1L, listOf(5, 6, 7), onMessageReceived = { _, _, _, _ -> }))
        val job = mockk<Job>()

        every { job.start() } returns true
        every { handler["getConsumingJob"]() } returns job

        handler.startConsuming()

        verify {
            job.start()
        }
    }

    @Test
    fun `postMessage expect message emitted in flow`() {

        val expectedClientId = 42L
        val expectedMessageKey = 37L
        val expectedMessageCategory = 53L
        val expectedData = 56


        var receivedClientId = 0L
        var receivedMsgKey = 0L
        var receivedMsgCategory = 0L
        var receivedPayload: Any = 0L

        val handler = spyk(MessageHandler(expectedClientId, listOf(5, 6, 7), onMessageReceived = { cId, keyId, cat, payload ->
            receivedClientId = cId
            receivedMsgKey = keyId
            receivedMsgCategory = cat
            receivedPayload = payload
        }))


        runBlocking {
            handler.startConsuming()
            handler.postMessage(MessageBundle(expectedMessageKey, expectedMessageCategory, expectedData))

            //This isn't a blocking operation and is being made in another thread so we have ti put an sleep here
            withContext(Dispatchers.IO) {
                sleep(4000)
            }
        }

        assertEquals(receivedClientId, expectedClientId)
        assertEquals(receivedMsgKey, expectedMessageKey)
        assertEquals(receivedMsgCategory, expectedMessageCategory)
        assertEquals(receivedPayload as Int, expectedData)
    }
}