package com.bortxapps.lightmessagebrokerexample

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import com.bortxapps.lightmessagebrokerexample.activity.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun send_messages_to_categories() {
        // Context of the app under test.
        val device = UiDevice.getInstance(getInstrumentation())
        device.findObject(By.res("tfNumberMessages"))

        composeTestRule.onNodeWithTag("tfNumberMessages").performTextInput("10")
        composeTestRule.onNodeWithTag("tfNumberConsumers").performTextInput("3")
        composeTestRule.onNodeWithTag("btnStart").performClick()

        composeTestRule.onNodeWithTag("TextResult").assertIsDisplayed()
        composeTestRule.onNodeWithTag("lcConsumersList").assertIsDisplayed()
        composeTestRule.onNodeWithTag("lcConsumersList").onChildren().assertCountEquals(3)
    }

    @Test
    fun send_messages_to_client_ids() {
        // Context of the app under test.
        val device = UiDevice.getInstance(getInstrumentation())
        device.findObject(By.res("tfNumberMessages"))

        composeTestRule.onNodeWithTag("tfNumberMessages").performTextInput("10")
        composeTestRule.onNodeWithTag("tfNumberConsumers").performTextInput("3")
        composeTestRule.onNodeWithTag("cbSendOneByOne").performClick()
        composeTestRule.onNodeWithTag("btnStart").performClick()

        composeTestRule.onNodeWithTag("TextResult").assertIsDisplayed()
        composeTestRule.onNodeWithTag("lcConsumersList").assertIsDisplayed()
        composeTestRule.onNodeWithTag("lcConsumersList").onChildren().assertCountEquals(3)
    }
}