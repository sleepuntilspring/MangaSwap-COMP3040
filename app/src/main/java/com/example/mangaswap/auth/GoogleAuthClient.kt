package com.example.mangaswap.auth

import android.app.Activity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class GoogleAuthClient(
    private val activity: Activity,
) {
    private val tag = "GoogleAuthClient: "
    private val credentialManager = CredentialManager.create(activity)
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Check if the user is already signed in
    fun isSignedIn(): Boolean {
        if (firebaseAuth.currentUser != null) {
            println(tag + "already signed in")
            return true
        }
        return false
    }

    // Handles the Google Sign-In process
    suspend fun signIn(): Boolean {
        if (isSignedIn()) {
            return true
        }

        try {
            // Build the credential request and handle the sign-in
            val result = buildCredentialRequest()
            return handleSignIn(result)
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e

            println(tag + "signIn: ${e.message}")
            return false
        }
    }

    // Processes the sign-in result and validates the credential
    private suspend fun handleSignIn(result: GetCredentialResponse): Boolean {
        val credential = result.credential

        if (
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                // Parse the Google ID token credential
                val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                println(tag + "name: ${tokenCredential.displayName}")
                println(tag + "email: ${tokenCredential.id}")
                println(tag + "image: ${tokenCredential.profilePictureUri}")

                // Create Firebase authentication credential
                val authCredential = GoogleAuthProvider.getCredential(
                    tokenCredential.idToken, null
                )

                // Sign in with the credential
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()
                val user = authResult.user

                if (user != null) {
                    // Save user details to Firestore
                    saveUserToFirestore(
                        userId = user.uid,
                        displayName = tokenCredential.displayName,
                        email = tokenCredential.id,
                        profilePictureUrl = tokenCredential.profilePictureUri.toString()
                    )
                    return true
                }
                return false

            } catch (e: Exception) {
                println(tag + "GoogleIdTokenParsingException: ${e.message}")
                return false
            }
        } else {
            println(tag + "credential is not GoogleIdTokenCredential")
            return false
        }
    }

    // Save user data to Firestore if the user doesn't already exist
    private fun saveUserToFirestore(
        userId: String,
        displayName: String?,
        email: String?,
        profilePictureUrl: String?
    ) {
        val userDocumentRef = firestore.collection("users").document(userId)

        userDocumentRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Skip saving if user document already exists
                println("$tag User already exists in Firestore. Skipping overwrite.")
            } else {
                // Create a new document with user details
                val userMap = mapOf(
                    "name" to (displayName ?: "Unknown"),
                    "email" to (email ?: "Unknown"),
                    "profilePicture" to (profilePictureUrl ?: "")
                )

                userDocumentRef.set(userMap)
                    .addOnSuccessListener {
                        println("$tag User saved successfully to Firestore.")
                    }
                    .addOnFailureListener { e ->
                        println("$tag Failed to save user: ${e.message}")
                    }
            }
        }.addOnFailureListener { e ->
            println("$tag Error fetching user document: ${e.message}")
        }
    }

    // Builds the credential request for Google Sign-In
    private suspend fun buildCredentialRequest(): GetCredentialResponse {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(
                        "614151750395-g8n42iis4eddng2434adlgugioob94jl.apps.googleusercontent.com"
                    )
                    .setAutoSelectEnabled(false)
                    .build()
            )
            .build()

        return credentialManager.getCredential(
            request = request,
            context = activity
        )
    }

    // Signs the user out from Firebase and clears the credential state
    suspend fun signOut() {
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        firebaseAuth.signOut()
    }

    // Deletes the user's account and removes their data from Firestore
    suspend fun deleteAccount() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            try {
                // Remove user document from Firestore
                firestore.collection("users").document(currentUser.uid).delete()
                    .addOnSuccessListener {
                        println("User data deleted successfully from Firestore.")
                    }
                    .addOnFailureListener { e ->
                        println("Failed to delete user data: ${e.message}")
                    }

                // Delete the Firebase user account
                currentUser.delete().await()
                println("Account deleted successfully.")
            } catch (e: Exception) {
                println("Failed to delete account: ${e.message}")
                throw e
            }
        } else {
            println("No user signed in.")
        }
    }
}
