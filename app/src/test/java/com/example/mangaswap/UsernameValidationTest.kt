package com.example.mangaswap

import org.junit.Assert.*
import org.junit.Test

class UsernameValidationTest {

    @Test
    fun testValidUsername() {
        val validUsername = "MangaLover123"
        assertTrue("Username should be valid", isUsernameValid(validUsername))
    }

    @Test
    fun testInvalidUsername_Empty() {
        val emptyUsername = ""
        assertFalse("Empty username should be invalid", isUsernameValid(emptyUsername))
    }

    @Test
    fun testInvalidUsername_Whitespace() {
        val whitespaceUsername = "   "
        assertFalse("Whitespace-only username should be invalid", isUsernameValid(whitespaceUsername))
    }

    // Helper function to simulate username validation logic
    private fun isUsernameValid(username: String): Boolean {
        return username.isNotBlank() // A valid username should not be blank
    }
}