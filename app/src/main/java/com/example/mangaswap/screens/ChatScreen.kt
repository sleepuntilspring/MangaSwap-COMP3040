package com.example.mangaswap.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.material3.AlertDialog
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import com.example.mangaswap.R
import com.example.mangaswap.location.calculateDistance
import com.google.firebase.Timestamp

@Composable
fun ChatScreen(
    onBackClick: () -> Unit, // Callback when the user clicks the back button
    username: String, // Username of the chat participant
    profilePicture: String, // Profile picture URL of the chat participant
    mangaId: String, // ID of the manga related to the chat
    chatId: String // ID of the chat session
) {
    // State variables for manga details, loading status, distance, and messages
    var mangaDetails by remember { mutableStateOf<Manga?>(null) } // Manga details fetched from Firestore
    var isLoading by remember { mutableStateOf(true) } // Loading indicator state
    var distance by remember { mutableStateOf<String?>(null) } // Distance between users
    var messages by remember { mutableStateOf(listOf<Message>()) } // List of messages in the chat

    // Firebase Firestore and Auth instances
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser // Get the currently logged-in user
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Fetch manga details and calculate the distance
    LaunchedEffect(mangaId) {
        try {
            val document = db.collection("mangas").document(mangaId).get().await() // Fetch manga details from Firestore
            val fetchedManga = document.toObject(Manga::class.java) // Map the Firestore document to a Manga object

            if (fetchedManga != null) {
                mangaDetails = fetchedManga // Update manga details state

                // Fetch the user's location to calculate the distance
                val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
                if (androidx.core.app.ActivityCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            // Calculate the distance between users
                            distance = String.format("%.2f km", calculateDistance(
                                userLatitude = it.latitude,
                                userLongitude = it.longitude,
                                targetLatitude = fetchedManga.latitude,
                                targetLongitude = fetchedManga.longitude
                            ))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Error fetching manga details: ${e.message}") // Log the error if fetching fails
        } finally {
            isLoading = false // Hide the loading indicator
        }
    }

    // Fetch messages for the chat session in real-time
    LaunchedEffect(chatId) {
        val query = db.collection("messages")
            .whereEqualTo("chatId", chatId) // Filter messages by chatId
            .orderBy("timestamp") // Order messages by timestamp

        query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                println("Error listening for messages: ${e.message}") // Log the error if listening fails
                return@addSnapshotListener
            }

            if (snapshot != null) {
                // Map Firestore documents to Message objects
                val fetchedMessages = snapshot.documents.map { doc ->
                    Message(
                        message = doc.getString("message") ?: "",
                        senderId = doc.getString("senderId") ?: "",
                        timestamp = doc.getTimestamp("timestamp")?.toDate().toString()
                    )
                }

                println("Fetched messages: $fetchedMessages") // Log fetched messages
                messages = fetchedMessages // Update messages state
            } else {
                println("Snapshot is null.") // Log if the snapshot is null
            }
        }
    }

    if (isLoading) {
        // Show a loading indicator while data is being fetched
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White) // Display a loading spinner
        }
    } else if (mangaDetails != null) {
        // Render chat screen content if manga details are loaded
        ChatScreenContent(
            onBackClick = onBackClick,
            username = username,
            profilePicture = profilePicture,
            mangaDetails = mangaDetails!!,
            distance = distance ?: "Calculating...", // Pass calculated distance
            messages = messages,
            onSendMessage = { messageText ->
                if (messageText.isNotBlank()) {
                    scope.launch {
                        // Send the message and add it to Firestore
                        val messageData = mapOf(
                            "message" to messageText,
                            "senderId" to currentUser?.uid,
                            "timestamp" to Timestamp.now(),
                            "chatId" to chatId
                        )
                        db.collection("messages").add(messageData).addOnSuccessListener {
                            println("Message sent: $messageData") // Log success
                        }.addOnFailureListener { e ->
                            println("Error sending message: ${e.message}") // Log failure
                        }
                    }
                }
            }
        )
    } else {
        // Show error message if manga details couldn't be loaded
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("Failed to load manga details.", color = Color.White)
        }
    }
}
@Composable
fun ChatScreenContent(
    onBackClick: () -> Unit, // Callback when the back button is clicked
    username: String, // Username of the other chat participant
    profilePicture: String, // Profile picture URL of the other participant
    mangaDetails: Manga, // Details of the manga related to the chat
    distance: String, // Distance between the user and the manga's owner
    messages: List<Message>, // List of chat messages
    onSendMessage: (String) -> Unit // Callback to handle sending messages
) {
    var messageText by remember { mutableStateOf("") } // State to track the input message text

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp) // Add padding at the top for the app bar
    ) {
        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .background(Color(0xFF73A1D7)), // App bar background color
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick, // Navigate back on click
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.back), // Back icon
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
                // Display profile picture of the chat participant
                AsyncImage(
                    model = profilePicture,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape) // Round the image
                        .background(Color.Gray)
                        .padding(4.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Display username of the chat participant
                Text(
                    text = username,
                    fontSize = 20.sp,
                    fontFamily = sourceSans3,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Manga Details Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 66.dp) // Position below the app bar
                .background(Color.Gray) // Background color for manga details
                .padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Manga image
                AsyncImage(
                    model = mangaDetails.imageUrl,
                    contentDescription = mangaDetails.title,
                    modifier = Modifier
                        .width(80.dp)
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp)) // Rounded corners
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    // Display manga details like title, author, volume, condition, and distance
                    Text(
                        text = mangaDetails.title,
                        fontSize = 18.sp,
                        fontFamily = sourceSans3,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        text = "Author: ${mangaDetails.author}",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontFamily = sourceSans3,
                        fontWeight = FontWeight.Light,
                    )
                    Text(
                        text = "Volume: ${mangaDetails.volume}",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontFamily = sourceSans3,
                        fontWeight = FontWeight.Light,
                    )
                    Text(
                        text = "Condition: ${mangaDetails.condition}/5",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontFamily = sourceSans3,
                        fontWeight = FontWeight.Light,
                    )
                    Text(
                        text = "Distance: $distance",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontFamily = sourceSans3,
                        fontWeight = FontWeight.Light,
                    )
                }
            }
        }

        // Chat Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 200.dp) // Position below manga details
                .background(Color.Black) // Chat area background
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 8.dp), // Add horizontal padding
                contentPadding = PaddingValues(
                    top = 10.dp,
                    bottom = 130.dp // Avoid overlap with the input box
                )
            ) {
                // Display each message in the chat
                items(messages.size) { index ->
                    val message = messages[index]
                    MessageItem(
                        message = message,
                        isCurrentUser = message.senderId == FirebaseAuth.getInstance().currentUser?.uid // Check if the message is sent by the current user
                    )
                }
            }
        }

        // Message Input Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .align(Alignment.BottomStart) // Position at the bottom
                .background(Color(0xFF73A1D7)) // Input box background
                .padding(
                    start = 10.dp,
                    end = 10.dp,
                    top = 5.dp,
                    bottom = 50.dp // Adjust padding to avoid being blocked by navigation buttons
                )
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Text input field for entering messages
                TextField(
                    value = messageText,
                    onValueChange = { messageText = it }, // Update the message text
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") }, // Placeholder text
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Send button
                Button(
                    onClick = {
                        onSendMessage(messageText) // Send the message
                        messageText = "" // Clear the input field
                    },
                    modifier = Modifier.height(50.dp)
                ) {
                    Text("Send") // Button label
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageItem(
    message: Message, // Message data
    isCurrentUser: Boolean // Whether the message is sent by the current user
) {
    val firestore = FirebaseFirestore.getInstance() // Firestore instance
    val context = LocalContext.current // Context for displaying Toasts
    var showDialog by remember { mutableStateOf(false) } // State to track dialog visibility

    val alignment = if (isCurrentUser) Alignment.End else Alignment.Start // Align message based on sender
    val backgroundColor = if (isCurrentUser) Color.Blue else Color.Gray // Different background colors for sent/received messages

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(
                onClick = { /* Handle normal clicks if needed */ },
                onLongClick = {
                    if (isCurrentUser) { // Allow unsending only for current user's messages
                        showDialog = true
                    }
                }
            ),
        horizontalAlignment = alignment // Align message text
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp), // Rounded corners
            shadowElevation = 1.dp, // Shadow for elevation
            color = backgroundColor // Background color
        ) {
            Text(
                text = "${message.message}\n${message.timestamp}", // Display message and timestamp
                fontSize = 16.sp,
                color = Color.White,
                fontFamily = sourceSans3,
                modifier = Modifier.padding(12.dp)
            )
        }
    }

    // Confirmation dialog for unsending a message
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false }, // Dismiss dialog on request
            title = { Text(text = "Are you sure you want to unsend this message?", fontWeight = FontWeight.Bold) },
            confirmButton = {
                Text(
                    text = "Yes",
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            // Delete the message from Firestore
                            firestore.collection("messages")
                                .whereEqualTo("message", message.message)
                                .whereEqualTo("senderId", message.senderId)
                                .limit(1) // Limit to one document
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    if (!querySnapshot.isEmpty) {
                                        val document = querySnapshot.documents.first()
                                        document.reference.delete()
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    context,
                                                    "Message unsent successfully.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                showDialog = false // Dismiss dialog
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(
                                                    context,
                                                    "Failed to unsend message: ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                showDialog = false // Dismiss dialog
                                            }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Message not found.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        showDialog = false // Dismiss dialog
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        context,
                                        "Error retrieving message: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    showDialog = false // Dismiss dialog
                                }
                        },
                    color = Color.Black
                )
            },
            dismissButton = {
                Text(
                    text = "No",
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            showDialog = false // Dismiss dialog
                        },
                    color = Color.Black
                )
            },
            text = { Text(text = "This will delete the message permanently.") } // Dialog message
        )
    }
}

// Data class representing a message
data class Message(
    val message: String, // The message text
    val senderId: String, // ID of the sender
    val timestamp: String // Timestamp of the message
)
