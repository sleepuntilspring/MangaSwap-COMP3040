package com.example.mangaswap

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.mangaswap.auth.GoogleAuthClient
import com.example.mangaswap.navigation.NavHostSetup
import com.example.mangaswap.ui.theme.MangaSwapTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // Lazy initialization of GoogleAuthClient to manage authentication
    private val googleAuthClient by lazy { GoogleAuthClient(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enables edge-to-edge display for better UI

        setContent {
            MangaSwapTheme {
                // Creates a navigation controller to manage app navigation
                val navController = rememberNavController()

                // Keeps track of the user's sign-in state (saved during configuration changes)
                var isSignedIn by rememberSaveable { mutableStateOf(googleAuthClient.isSignedIn()) }

                // Sets up the navigation host with routes and actions
                NavHostSetup(
                    navController = navController,
                    isSignedIn = isSignedIn, // Pass the sign-in state to determine start destination
                    onGoogleSignIn = {
                        // Handles Google Sign-In when invoked
                        lifecycleScope.launch {
                            val success = googleAuthClient.signIn() // Attempts to sign in
                            isSignedIn = success // Updates the sign-in state
                            if (success) {
                                // Navigates to the home screen if sign-in succeeds
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true } // Removes login from the back stack
                                }
                            }
                        }
                    },
                    onSignOut = {
                        // Handles user sign-out
                        signOut(navController, isSignedInSetter = { isSignedIn = false })
                    },
                    onDeleteAccount = {
                        // Handles account deletion
                        deleteAccount(navController, isSignedInSetter = { isSignedIn = false })
                    }
                )
            }
        }
    }

    /**
     * Handles user sign-out and navigates back to the login screen.
     * @param navController The navigation controller to handle screen transitions.
     * @param isSignedInSetter A function to update the signed-in state.
     */
    private fun signOut(navController: NavHostController, isSignedInSetter: (Boolean) -> Unit) {
        lifecycleScope.launch {
            googleAuthClient.signOut() // Signs out the user from Google
            isSignedInSetter(false) // Updates the sign-in state

            // Displays a Toast message to confirm sign-out
            Toast.makeText(this@MainActivity, "You have successfully logged out.", Toast.LENGTH_SHORT).show()

            // Navigates to the login screen and removes the home screen from the back stack
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    /**
     * Handles account deletion and navigates back to the login screen.
     * @param navController The navigation controller to handle screen transitions.
     * @param isSignedInSetter A function to update the signed-in state.
     */
    private fun deleteAccount(navController: NavHostController, isSignedInSetter: (Boolean) -> Unit) {
        lifecycleScope.launch {
            try {
                googleAuthClient.deleteAccount() // Deletes the user's account from Google
                isSignedInSetter(false) // Updates the sign-in state

                // Displays a Toast message to confirm account deletion
                Toast.makeText(this@MainActivity, "Your account has been deleted successfully.", Toast.LENGTH_SHORT).show()

                // Navigates to the login screen and removes the home screen from the back stack
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            } catch (e: Exception) {
                e.printStackTrace() // Logs any exceptions that occur during account deletion
            }
        }
    }
}