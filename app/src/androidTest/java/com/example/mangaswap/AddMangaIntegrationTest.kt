package com.example.mangaswap

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.*

class AddMangaIntegrationTest {

    @Test
    fun testAddMangaToFirestoreWithImage() {
        // Mock Firebase Firestore, Storage, and related components
        val mockFirestore = mock(FirebaseFirestore::class.java)
        val mockCollectionReference = mock(CollectionReference::class.java)
        val mockDocumentReference = mock(DocumentReference::class.java)
        val mockAddTask = mock(Task::class.java) as Task<DocumentReference>
        val mockStorage = mock(FirebaseStorage::class.java)
        val mockStorageReference = mock(StorageReference::class.java)
        val mockImageReference = mock(StorageReference::class.java) // Mock the specific child reference
        val mockUploadTask = mock(UploadTask::class.java)

        // Mock Firestore's collection and document retrieval
        `when`(mockFirestore.collection("mangas")).thenReturn(mockCollectionReference)
        `when`(mockCollectionReference.add(any<Map<String, Any>>())).thenReturn(mockAddTask)
        `when`(mockAddTask.isSuccessful).thenReturn(true)
        `when`(mockAddTask.result).thenReturn(mockDocumentReference)

        // Mock Firebase Storage's reference hierarchy
        `when`(mockStorage.reference).thenReturn(mockStorageReference)
        `when`(mockStorageReference.child(anyString())).thenReturn(mockImageReference) // Return a non-null child reference
        `when`(mockImageReference.putFile(any(Uri::class.java))).thenReturn(mockUploadTask)
        `when`(mockUploadTask.isSuccessful).thenReturn(true)

        // Simulate successful document addition
        var addSuccess = false
        doAnswer {
            addSuccess = true
            null // Return null for the method call
        }.`when`(mockCollectionReference).add(any<Map<String, Any>>())

        // Mock manga data
        val mangaData = mapOf(
            "title" to "Naruto",
            "author" to "Masashi Kishimoto",
            "volume" to 1,
            "condition" to 5,
            "latitude" to 35.6895,
            "longitude" to 139.6917,
            "owner" to "testUser",
            "imageUrl" to "https://fakeurl.com/testImage.jpg"
        )

        // Add the manga to Firestore
        mockCollectionReference.add(mangaData)

        // Verify that the add method was called with the correct data
        verify(mockCollectionReference).add(mangaData)

        // Assert that the manga addition was successful
        assertTrue("The manga should be added successfully", addSuccess)
    }
}
