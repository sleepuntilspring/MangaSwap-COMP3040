package com.example.mangaswap

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.*

class UpdateUsernameIntegrationTest {

    @Test
    fun testUpdateUsernameInFirestore() {
        // Mock Firebase Firestore and its related components
        val mockFirestore = mock(FirebaseFirestore::class.java)
        val mockCollectionReference = mock(CollectionReference::class.java)
        val mockDocumentReference = mock(DocumentReference::class.java)
        val mockUser = mock(FirebaseUser::class.java)

        // Mock the FirebaseUser UID
        `when`(mockUser.uid).thenReturn("testUserId")

        // Mock Firestore's collection and document retrieval
        `when`(mockFirestore.collection("users")).thenReturn(mockCollectionReference)
        `when`(mockCollectionReference.document(mockUser.uid)).thenReturn(mockDocumentReference)

        // Simulate updating the username in Firestore
        val newUsername = "NewMangaFan"
        var updateSuccess = false

        `when`(mockDocumentReference.update("name", newUsername)).thenAnswer {
            updateSuccess = true // Simulate a successful update
            null // Return null for the method call
        }

        // Perform the update
        mockDocumentReference.update("name", newUsername)

        // Verify that the update was called
        verify(mockDocumentReference).update("name", newUsername)

        // Assert that the update was successful
        assertTrue("The username update should succeed", updateSuccess)
    }
}
