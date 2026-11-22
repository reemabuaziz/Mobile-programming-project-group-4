package com.group04.materialexchange

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ExchangeFlowTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private fun click(text: String) {
        val node=rule.onNodeWithText(text,useUnmergedTree=false)
        runCatching { node.performScrollTo() }
        node.performClick();rule.waitForIdle()
    }
    private fun shot(name: String) {
        rule.waitForIdle()
        val image=rule.onRoot().captureToImage().asAndroidBitmap()
        File(rule.activity.getExternalFilesDir(null),"$name.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG,100,it) }
    }
    private fun login(email: String) {
        rule.onNodeWithText("University Email").performTextReplacement(email)
        rule.onNodeWithText("Password",substring=false).performTextReplacement("Demo1234")
        click("Sign In")
        rule.waitUntil(10000) { rule.onAllNodesWithText("Available Materials").fetchSemanticsNodes().isNotEmpty() }
    }
    @Test fun requestThenHomeReturnsToFeed() {
        login("2240002170@iau.edu.sa")
        click("Arduino Uno R3")
        click("Request Material")
        click("Send Request")
        rule.waitUntil(5000) { rule.onAllNodesWithText("Request sent").fetchSemanticsNodes().isNotEmpty() }
        click("View Sent Requests")
        rule.onNodeWithText("Status: Pending").assertExists()
        click("Home")
        rule.waitUntil(5000) { rule.onAllNodesWithText("Available Materials").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithContentDescription("Create Listing").assertExists()
        click("Notifications")
        click("Home")
        rule.onNodeWithText("Available Materials").assertExists()
        click("Profile")
        click("Home")
        rule.onNodeWithText("Available Materials").assertExists()
    }
    @Test fun coreScreensAndCrossAccountExchange() {
        shot("01-sign-in")
        click("Create Account");shot("02-create-account")
        click("Sign In")
        login("2240002170@iau.edu.sa");shot("03-home")
        click("Arduino Uno R3");shot("04-item-details")
        click("Request Material");click("Send Request")
        rule.waitUntil(5000) { rule.onAllNodesWithText("Request sent").fetchSemanticsNodes().isNotEmpty() }
        click("View Sent Requests")
        rule.onNodeWithText("Status: Pending").assertExists()
        click("Home")
        rule.waitUntil(5000) { rule.onAllNodesWithText("Available Materials").fetchSemanticsNodes().isNotEmpty() }
        shot("03b-home-return")
        rule.onRoot().printToLog("ExchangeUiTree")
        rule.onNodeWithContentDescription("Create Listing").performClick();shot("05-create-listing")
        rule.onNodeWithContentDescription("Back").performClick()
        click("Activity");shot("06-my-activity")
        click("Notifications");shot("07-notifications")
        click("Profile");shot("08-profile")
        click("Sign Out");rule.onNodeWithText("Cancel").performClick()
        rule.onNodeWithText("Narjes Hassan").assertExists()
        click("Sign Out")
        rule.onAllNodesWithText("Sign Out").onLast().performClick()
        login("ahmed@iau.edu.sa")
        click("Activity");click("Received Requests")
        rule.onNodeWithText("Arduino Uno R3").assertExists()
        click("Accept")
        rule.onNodeWithText("Status: Accepted").assertExists()
        click("Complete");click("Confirm")
        rule.onNodeWithText("Status: Completed").assertExists()
        click("Profile")
        rule.onNodeWithText("Completed Exchanges: 1").assertExists()
    }
}
