package com.example.mangaswap

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddBooksValidationTest {

    @Test
    fun testValidateInputs_AllFieldsValid() {
        val seriesName = "One Piece"
        val authorName = "Eiichiro Oda"
        val volumeNumber = "1"
        val selectedCondition = "5"
        val imageUri = "http://example.com/image.jpg" // Simulated image URI

        val result = validateInputs(
            seriesName = seriesName,
            authorName = authorName,
            volumeNumber = volumeNumber,
            selectedCondition = selectedCondition,
            imageUri = imageUri
        )

        assertTrue(result) // Validation should pass
    }

    @Test
    fun testValidateInputs_MissingFields() {
        // Missing series name
        var result = validateInputs(
            seriesName = "",
            authorName = "Eiichiro Oda",
            volumeNumber = "1",
            selectedCondition = "5",
            imageUri = "http://example.com/image.jpg"
        )
        assertFalse(result) // Validation should fail

        // Missing author name
        result = validateInputs(
            seriesName = "One Piece",
            authorName = "",
            volumeNumber = "1",
            selectedCondition = "5",
            imageUri = "http://example.com/image.jpg"
        )
        assertFalse(result) // Validation should fail

        // Invalid condition
        result = validateInputs(
            seriesName = "One Piece",
            authorName = "Eiichiro Oda",
            volumeNumber = "1",
            selectedCondition = "Condition",
            imageUri = "http://example.com/image.jpg"
        )
        assertFalse(result) // Validation should fail

        // Missing image
        result = validateInputs(
            seriesName = "One Piece",
            authorName = "Eiichiro Oda",
            volumeNumber = "1",
            selectedCondition = "5",
            imageUri = null
        )
        assertFalse(result) // Validation should fail
    }

    // Validation function (replicates the logic from AddBooksScreen)
    private fun validateInputs(
        seriesName: String,
        authorName: String,
        volumeNumber: String,
        selectedCondition: String,
        imageUri: String?
    ): Boolean {
        return seriesName.isNotBlank() &&
                authorName.isNotBlank() &&
                volumeNumber.isNotBlank() &&
                selectedCondition != "Condition" &&
                imageUri != null
    }
}
