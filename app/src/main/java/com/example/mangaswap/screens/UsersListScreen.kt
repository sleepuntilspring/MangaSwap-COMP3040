package com.example.mangaswap.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.platform.LocalContext
import com.example.mangaswap.R

@Composable
fun UsersListScreen(
    navController: NavHostController, // Navigation controller to navigate between screens
    onHomeClick: () -> Unit, // Callback for Home button click
    onRequestsClick: () -> Unit, // Callback for Requests button click
    onChatClick: () -> Unit, // Callback for Chat button click
    onProfileClick: () -> Unit // Callback for Profile button click
) {
    val firestore = FirebaseFirestore.getInstance() // Firestore instance
    var usersList by remember { mutableStateOf<List<User>>(emptyList()) } // State to hold the list of users
    var isLoading by remember { mutableStateOf(true) } // State to track loading status
    val context = LocalContext.current // Access the current context for showing Toast messages
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid // Get the current user's ID

    // Fetch users and chats associated with the current user
    LaunchedEffect(Unit) {
        try {
            val chatQuerySnapshot = firestore.collection("chats").get().await() // Fetch all chats
            val chats = chatQuerySnapshot.documents

            val fetchedUsers = chats.mapNotNull { chat ->
                val participants = chat.get("participants") as? List<String> ?: return@mapNotNull null
                val mangaId = chat.getString("mangaId") ?: return@mapNotNull null

                // Ensure the chat has two participants and the current user is one of them
                if (participants.size == 2 && currentUserId != null) {
                    val borrowedUserId = participants.find { it != currentUserId } // Get the other participant's ID

                    borrowedUserId?.let { userId ->
                        // Fetch user and manga details
                        val userDoc = firestore.collection("users").document(userId).get().await()
                        val username = userDoc.getString("name") ?: "Unknown User"
                        val profilePicture = userDoc.getString("profilePicture") ?: ""

                        val mangaDoc = firestore.collection("mangas").document(mangaId).get().await()
                        val mangaTitle = mangaDoc.getString("title") ?: "Unknown Manga"
                        val mangaVolume = mangaDoc.getLong("volume")?.toString() ?: "Unknown Volume"

                        // Return user details with manga and chat information
                        User(
                            username = username,
                            manga = "$mangaTitle - Volume $mangaVolume",
                            profilePicture = profilePicture,
                            mangaId = mangaId,
                            chatId = chat.id
                        )
                    }
                } else {
                    null
                }
            }

            usersList = fetchedUsers // Update the state with fetched users
        } catch (e: Exception) {
            println("Error fetching users: ${e.message}")
        } finally {
            isLoading = false // Update loading status
        }
    }

    // Screen layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp)
            .background(Color.Black) // Black background for the screen
    ) {
        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
                .background(Color(0xFF73A1D7)), // Blue top bar
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Chats", // Title of the screen
                fontSize = 30.sp,
                color = Color.White,
                fontFamily = sourceSans3,
                fontWeight = FontWeight.Bold
            )
        }

        // Main Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(top = 66.dp, bottom = 80.dp) // Padding to adjust content
        ) {
            if (isLoading) {
                // Show loading text while fetching data
                Text(
                    text = "Loading...",
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (usersList.isEmpty()) {
                // Show message if no chats are available
                Text(
                    text = "No chats available.",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = sourceSans3,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // Display the list of users in a scrollable list
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp) // Spacing between items
                ) {
                    items(usersList.size) { index ->
                        val user = usersList[index]
                        // Render each user item with the UserItem composable
                        UserItem(
                            username = user.username,
                            manga = user.manga,
                            mangaId = user.mangaId,
                            profilePicture = user.profilePicture,
                            context = context,
                            chatId = user.chatId,
                            onClick = {
                                // Navigate to chat screen with user details
                                navController.navigate(
                                    "chatScreen/${Uri.encode(user.username)}/${Uri.encode(user.profilePicture)}/${Uri.encode(user.mangaId)}/${Uri.encode(user.chatId)}"
                                )
                            },
                            onChatDeleted = {
                                // Remove the deleted chat from the list and show a Toast message
                                usersList = usersList.filter { it.chatId != user.chatId }
                                Toast.makeText(context, "Chat deleted successfully.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }

        // Bottom Navigation Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color(0xFF73A1D7)) // Blue navigation bar
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp)
        ) {
            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(onClick = onHomeClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.home),
                        contentDescription = "Home",
                        modifier = Modifier.size(43.dp),
                        tint = Color.White
                    )
                }
                IconButton(onClick = onRequestsClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.requests),
                        contentDescription = "Requests",
                        modifier = Modifier.size(36.dp),
                        tint = Color.White
                    )
                }
                IconButton(onClick = onChatClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.chat),
                        contentDescription = "Chat",
                        modifier = Modifier.size(38.dp),
                        tint = Color.White
                    )
                }
                IconButton(onClick = onProfileClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.profile),
                        contentDescription = "Profile",
                        modifier = Modifier.size(43.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserItem(
    username: String, // Display name of the user
    manga: String, // Manga details (title and volume)
    profilePicture: String, // URL to the user's profile picture
    mangaId: String, // Manga ID associated with the chat
    chatId: String, // Chat ID to identify the conversation
    context: android.content.Context, // Context for showing Toast messages
    onClick: () -> Unit, // Callback function for navigating to the chat screen
    onChatDeleted: () -> Unit // Callback function to handle the deletion of the chat
) {
    val firestore = FirebaseFirestore.getInstance() // Firebase Firestore instance
    var showDialog by remember { mutableStateOf(false) } // State to manage the visibility of the confirmation dialog

    // Main Row to display the user's information and manga details
    Row(
        modifier = Modifier
            .fillMaxWidth() // Row spans the full width of the screen
            .padding(horizontal = 16.dp, vertical = 8.dp) // Padding around the Row
            .combinedClickable(
                onClick = { onClick() }, // Navigate to the chat screen on click
                onLongClick = { showDialog = true } // Show the delete confirmation dialog on long press
            )
            .background(Color.Black), // Black background for the Row
        verticalAlignment = Alignment.CenterVertically // Align items vertically in the center
    ) {
        // Profile Picture
        AsyncImage(
            model = profilePicture, // Load the profile picture from the provided URL
            contentDescription = null, // No specific description for the image
            modifier = Modifier
                .size(65.dp) // Set the size of the profile picture
                .clip(CircleShape) // Crop the image into a circular shape
        )

        Spacer(modifier = Modifier.width(12.dp)) // Add spacing between the profile picture and text

        // Column to display the user's name and manga details
        Column {
            // Display the username
            Text(
                text = username,
                fontSize = 18.sp, // Font size for the username
                color = Color.White, // White text color
                fontFamily = sourceSans3, // Custom font family
                fontWeight = FontWeight.Medium // Medium weight for the text
            )
            // Display the manga details
            Text(
                text = manga,
                fontSize = 15.sp, // Font size for the manga details
                color = Color.Gray, // Gray text color
                fontFamily = sourceSans3, // Custom font family
                fontWeight = FontWeight.Normal // Normal weight for the text
            )
        }
    }

    // Confirmation dialog for deleting the chat and associated manga
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false }, // Dismiss the dialog when clicked outside
            title = { Text(text = "Delete Chat", fontWeight = FontWeight.Bold) }, // Title of the dialog
            text = { Text(text = "Are you sure you want to delete this chat and associated manga?") }, // Message in the dialog
            confirmButton = {
                Text(
                    text = "Yes", // Text for the confirm button
                    modifier = Modifier
                        .padding(8.dp) // Padding around the button
                        .clickable {
                            // Delete chat, its messages, and associated manga
                            firestore.collection("messages")
                                .whereEqualTo("chatId", chatId) // Query for messages in the chat
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    val batch = firestore.batch() // Batch to delete multiple items at once
                                    querySnapshot.documents.forEach { document ->
                                        batch.delete(document.reference) // Delete each message
                                    }
                                    batch.delete(firestore.collection("chats").document(chatId)) // Delete the chat document
                                    batch.commit() // Commit the batch operation
                                        .addOnSuccessListener {
                                            // Delete the manga after the chat is deleted
                                            firestore.collection("mangas").document(mangaId).delete()
                                                .addOnSuccessListener {
                                                    // Delete all requests associated with the manga
                                                    firestore.collection("requests")
                                                        .whereEqualTo("mangaId", mangaId) // Query for requests related to the manga
                                                        .get()
                                                        .addOnSuccessListener { requestSnapshot ->
                                                            val requestBatch = firestore.batch() // Batch for requests
                                                            requestSnapshot.documents.forEach { request ->
                                                                requestBatch.delete(request.reference) // Delete each request
                                                            }
                                                            requestBatch.commit() // Commit the batch operation
                                                                .addOnSuccessListener {
                                                                    Toast.makeText(
                                                                        context,
                                                                        "Chat, manga, and associated requests deleted successfully.",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show() // Show success message
                                                                    onChatDeleted() // Notify that the chat was deleted
                                                                    showDialog = false // Dismiss the dialog
                                                                }
                                                                .addOnFailureListener { e ->
                                                                    println("Error deleting requests: ${e.message}")
                                                                    Toast.makeText(
                                                                        context,
                                                                        "Error deleting requests: ${e.message}",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show() // Show error message
                                                                    showDialog = false
                                                                }
                                                        }
                                                        .addOnFailureListener { e ->
                                                            println("Error fetching requests: ${e.message}")
                                                            showDialog = false
                                                        }
                                                }
                                                .addOnFailureListener { e ->
                                                    println("Error deleting manga: ${e.message}")
                                                    Toast.makeText(
                                                        context,
                                                        "Error deleting manga: ${e.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show() // Show error message
                                                    showDialog = false
                                                }
                                        }
                                        .addOnFailureListener { e ->
                                            println("Error deleting chat and messages: ${e.message}")
                                            Toast.makeText(
                                                context,
                                                "Error deleting chat and messages: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show() // Show error message
                                            showDialog = false
                                        }
                                }
                                .addOnFailureListener { e ->
                                    println("Error retrieving messages: ${e.message}")
                                    Toast.makeText(
                                        context,
                                        "Error retrieving messages: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show() // Show error message
                                    showDialog = false
                                }
                        },
                    color = Color.Black // Text color for the confirm button
                )
            },
            dismissButton = {
                Text(
                    text = "No", // Text for the dismiss button
                    modifier = Modifier
                        .padding(8.dp) // Padding around the button
                        .clickable { showDialog = false }, // Dismiss the dialog on click
                    color = Color.Black // Text color for the dismiss button
                )
            }
        )
    }
}

// Data class to represent a user
data class User(
    val username: String, // User's name
    val manga: String, // Manga details
    val profilePicture: String, // URL to the profile picture
    val mangaId: String, // ID of the associated manga
    val chatId: String // ID of the chat
)