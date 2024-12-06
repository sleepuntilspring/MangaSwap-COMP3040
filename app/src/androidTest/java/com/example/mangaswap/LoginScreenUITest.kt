package com.example.mangaswap

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.mangaswap.screens.LoginScreen
import org.junit.Rule
import org.junit.Test

class LoginScreenUITest {

    @get:Rule
    val composeTestRule = createComposeRule() // Set up the Compose test rule

    @Test
    fun testGoogleSignInButtonIsDisplayedAndClickable() {
        // Set the content of the screen for testing
        composeTestRule.setContent {
            LoginScreen(onGoogleSignInClick = {})
        }

        // Check that the Google Sign-In button is displayed
        composeTestRule.onNodeWithText("Sign In with Google")
            .assertIsDisplayed() // Assert that the button with the text "Sign In with Google" is displayed

        // Check that the button is clickable
        composeTestRule.onNodeWithText("Sign In with Google")
            .performClick() // Perform a click action on the button


    }
}
