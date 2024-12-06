package com.example.mangaswap.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mangaswap.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun RequestScreen(
    onHomeClick: () -> Unit, // Callback for navigating to the home screen
    onRequestsClick: () -> Unit, // Callback for navigating to the requests screen
    onChatClick: () -> Unit, // Callback for navigating to the chat screen
    onProfileClick: () -> Unit // Callback for navigating to the profile screen
) {
    val firestore = FirebaseFirestore.getInstance() // Instance of Firestore
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid // Current logged-in user's ID
    var incomingRequests by remember { mutableStateOf<List<RequestItem>>(emptyList()) } // State for incoming requests
    var outgoingRequests by remember { mutableStateOf<List<RequestItem>>(emptyList()) } // State for outgoing requests
    var isLoading by remember { mutableStateOf(true) } // State to manage loading indicator
    var users by remember { mutableStateOf<Map<String, String>>(emptyMap()) } // Mapping of user IDs to their names

    // Fetch user data to display names in requests
    LaunchedEffect(Unit) {
        try {
            val userQuery = firestore.collection("users").get().await()
            users = userQuery.documents.associate { doc ->
                doc.id to (doc.getString("name") ?: "Unknown") // Map user IDs to their names
            }
        } catch (e: Exception) {
            println("Error fetching users: ${e.message}") // Log error in fetching users
        }
    }

    // Fetch incoming and outgoing requests for the current user
    LaunchedEffect(Unit) {
        if (currentUserId != null) {
            try {
                val incomingQuery = firestore.collection("requests")
                    .whereEqualTo("requestedFrom", currentUserId) // Query incoming requests
                    .get()
                    .await()
                incomingRequests = incomingQuery.documents.mapNotNull { doc ->
                    doc.toObject(RequestItem::class.java)?.copy(id = doc.id) // Map request data and add document ID
                }

                val outgoingQuery = firestore.collection("requests")
                    .whereEqualTo("requestedBy", currentUserId) // Query outgoing requests
                    .get()
                    .await()
                outgoingRequests = outgoingQuery.documents.mapNotNull { doc ->
                    doc.toObject(RequestItem::class.java)?.copy(id = doc.id) // Map request data and add document ID
                }
            } catch (e: Exception) {
                println("Error fetching requests: ${e.message}") // Log error in fetching requests
            } finally {
                isLoading = false // Set loading state to false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize() // Fill the entire screen
            .padding(top = 20.dp) // Padding for the top content
    ) {
        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth() // Full width of the screen
                .height(65.dp) // Fixed height for the top bar
                .background(Color(0xFF73A1D7)) // Background color
                .align(Alignment.TopStart) // Align the box at the top
        ) {
            Box(
                modifier = Modifier.fillMaxSize(), // Fill the top bar space
                contentAlignment = Alignment.Center // Center the text
            ) {
                Text(
                    text = "Requests", // Title text
                    fontSize = 30.sp, // Font size
                    color = Color.White, // Text color
                    fontFamily = sourceSans3, // Custom font family
                    fontWeight = FontWeight.Bold // Bold font weight
                )
            }
        }

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize() // Fill the entire column space
                .padding(top = 65.dp) // Padding below the top bar
                .background(Color.Black) // Black background for the main content
        ) {
            val selectedTab = remember { mutableStateOf(0) } // State for selected tab (incoming or outgoing)

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab.value, // Selected tab index
                modifier = Modifier.fillMaxWidth(), // Full width for the tab row
                containerColor = Color(0xFF73A1D7), // Background color of the tab row
                contentColor = Color.White // Text color for the tabs
            ) {
                Tab(
                    selected = selectedTab.value == 0, // Check if the tab is selected
                    onClick = { selectedTab.value = 0 }, // Set selected tab to incoming
                    modifier = Modifier.padding(vertical = 8.dp) // Add padding to the tab
                ) {
                    Text(
                        text = "Incoming (${incomingRequests.size})", // Display incoming requests count
                        fontSize = 20.sp, // Font size
                        fontFamily = sourceSans3, // Custom font family
                        fontWeight = FontWeight.SemiBold // Semi-bold font weight
                    )
                }
                Tab(
                    selected = selectedTab.value == 1, // Check if the tab is selected
                    onClick = { selectedTab.value = 1 }, // Set selected tab to outgoing
                    modifier = Modifier.padding(vertical = 8.dp) // Add padding to the tab
                ) {
                    Text(
                        text = "Outgoing (${outgoingRequests.size})", // Display outgoing requests count
                        fontSize = 20.sp, // Font size
                        fontFamily = sourceSans3, // Custom font family
                        fontWeight = FontWeight.SemiBold // Semi-bold font weight
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp)) // Add spacing below the tabs

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally), // Center the progress indicator
                    color = Color.White // Color of the progress indicator
                )
            } else {
                val requests = if (selectedTab.value == 0) incomingRequests else outgoingRequests // Determine which requests to display

                if (requests.isEmpty()) {
                    // Display a message if there are no requests
                    Box(
                        modifier = Modifier
                            .fillMaxSize(), // Fill the screen
                        contentAlignment = Alignment.Center // Center the text
                    ) {
                        Text(
                            text = "No requests available", // Message text
                            color = Color.White, // Text color
                            fontSize = 20.sp, // Font size
                            fontFamily = sourceSans3, // Custom font family
                            fontWeight = FontWeight.Bold, // Bold font weight
                            modifier = Modifier.padding(bottom = 150.dp) // Add bottom padding
                        )
                    }
                } else {
                    // Display the list of requests
                    LazyColumn(modifier = Modifier.fillMaxSize()) { // Lazy column for efficient scrolling
                        items(requests) { request -> // Iterate through the requests
                            RequestCard(
                                request = request, // Request item data
                                users = users, // Mapping of user IDs to names
                                isOutgoing = selectedTab.value == 1, // Check if outgoing requests are being displayed
                                onRequestDeleted = { requestId -> // Callback for request deletion
                                    if (selectedTab.value == 0) {
                                        incomingRequests = incomingRequests.filter { it.id != requestId } // Remove from incoming requests
                                    } else {
                                        outgoingRequests = outgoingRequests.filter { it.id != requestId } // Remove from outgoing requests
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Bottom Navigation Bar
        Box(
            modifier = Modifier
                .fillMaxWidth() // Full width for the navigation bar
                .height(100.dp) // Fixed height
                .background(Color(0xFF73A1D7)) // Background color
                .align(Alignment.BottomStart) // Align at the bottom
                .padding(bottom = 50.dp) // Add padding for navigation
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth() // Full width
                    .padding(horizontal = 8.dp), // Horizontal padding for navigation buttons
                verticalAlignment = Alignment.CenterVertically, // Align buttons vertically at the center
                horizontalArrangement = Arrangement.SpaceAround // Space buttons evenly
            ) {
                IconButton(onClick = onHomeClick) { // Home button action
                    Icon(
                        painter = painterResource(id = R.drawable.home), // Home icon
                        contentDescription = "Home", // Accessibility description
                        modifier = Modifier.size(43.dp), // Icon size
                        tint = Color.White // Icon color
                    )
                }
                IconButton(onClick = onRequestsClick) { // Requests button action
                    Icon(
                        painter = painterResource(id = R.drawable.requests), // Requests icon
                        contentDescription = "Requests", // Accessibility description
                        modifier = Modifier.size(36.dp), // Icon size
                        tint = Color.White // Icon color
                    )
                }
                IconButton(onClick = onChatClick) { // Chat button action
                    Icon(
                        painter = painterResource(id = R.drawable.chat), // Chat icon
                        contentDescription = "Chat", // Accessibility description
                        modifier = Modifier.size(38.dp), // Icon size
                        tint = Color.White // Icon color
                    )
                }
                IconButton(onClick = onProfileClick) { // Profile button action
                    Icon(
                        painter = painterResource(id = R.drawable.profile), // Profile icon
                        contentDescription = "Profile", // Accessibility description
                        modifier = Modifier.size(43.dp), // Icon size
                        tint = Color.White // Icon color
                    )
                }
            }
        }
    }
}
@Composable
fun RequestCard(
    request: RequestItem, // Represents the request item data to be displayed
    users: Map<String, String>, // Mapping of user IDs to their names
    isOutgoing: Boolean, // Indicates whether the request is outgoing or incoming
    onRequestDeleted: (String) -> Unit // Callback to handle the deletion of a request
) {
    val firestore = FirebaseFirestore.getInstance() // Instance of Firestore for database operations
    val context = androidx.compose.ui.platform.LocalContext.current // Provides access to the current context
    var mangaDetails by remember { mutableStateOf<Pair<String, String>?>(null) } // Holds manga details like title and image URL

    // Fetch Manga Details when the composable is loaded
    LaunchedEffect(request.mangaId) {
        try {
            val mangaDoc = firestore.collection("mangas").document(request.mangaId).get().await() // Fetch manga document
            val title = mangaDoc.getString("title") ?: "Unknown" // Get manga title, default to "Unknown" if null
            val imageUrl = mangaDoc.getString("imageUrl") ?: "" // Get manga image URL
            mangaDetails = title to imageUrl // Store the title and image URL as a pair
        } catch (e: Exception) {
            println("Error fetching manga details: ${e.message}") // Log the error if fetching fails
            mangaDetails = "Unknown" to "" // Set default values in case of failure
        }
    }

    // Row layout to display request details
    Row(
        modifier = Modifier
            .fillMaxWidth() // Take full width of the screen
            .padding(8.dp) // Add padding around the row
            .background(Color.Gray, shape = RoundedCornerShape(12.dp)) // Gray background with rounded corners
            .padding(16.dp), // Inner padding for the row content
        verticalAlignment = Alignment.CenterVertically // Align content vertically in the center
    ) {
        // Display the manga cover image
        AsyncImage(
            model = mangaDetails?.second, // Use the manga image URL
            contentDescription = mangaDetails?.first, // Use the manga title as content description
            modifier = Modifier.size(100.dp) // Set fixed size for the image
        )

        Spacer(modifier = Modifier.width(12.dp)) // Add spacing between the image and text content

        // Column for text details
        Column(
            modifier = Modifier.weight(1f) // Allow text to take remaining space
        ) {
            // Display manga title
            Text(
                text = mangaDetails?.first ?: "Loading...", // Show "Loading..." while manga details are fetched
                color = Color.White, // White text color
                fontSize = 16.sp, // Font size
                fontWeight = FontWeight.Bold // Bold font weight
            )

            // Display who requested the manga or who it was requested from
            Text(
                text = if (isOutgoing) {
                    "Requested From: ${users[request.requestedFrom] ?: "Unknown"}" // Show "Requested From" for outgoing requests
                } else {
                    "Requested By: ${users[request.requestedBy] ?: "Unknown"}" // Show "Requested By" for incoming requests
                },
                color = Color.White, // White text color
                fontSize = 14.sp // Font size
            )

            // Display the request status (e.g., pending)
            Text(
                text = "Status: ${request.status}", // Show the status of the request
                color = Color.LightGray, // Light gray text color
                fontSize = 14.sp // Font size
            )
        }

        Spacer(modifier = Modifier.width(8.dp)) // Add spacing between text and action buttons

        // Row for action buttons (Accept/Reject)
        Row(
            verticalAlignment = Alignment.CenterVertically // Align buttons vertically in the center
        ) {
            if (!isOutgoing) {
                // Accept button for incoming requests
                IconButton(onClick = {
                    // Check if the chat already exists between participants for the given manga
                    firestore.collection("chats")
                        .whereArrayContains("participants", request.requestedBy) // Query chats with participants
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            val existingChat = querySnapshot.documents.firstOrNull { doc ->
                                val participants = doc.get("participants") as? List<String> // Get list of participants
                                val mangaId = doc.getString("mangaId") // Get manga ID
                                participants != null &&
                                        participants.contains(request.requestedFrom) && // Check if requestedFrom is a participant
                                        mangaId == request.mangaId // Match the manga ID
                            }

                            if (existingChat == null) {
                                // If no existing chat, create a new one
                                val chat = hashMapOf(
                                    "participants" to listOf(request.requestedBy, request.requestedFrom), // Add participants
                                    "mangaId" to request.mangaId, // Add manga ID
                                    "mangaTitle" to mangaDetails?.first, // Add manga title
                                    "mangaVolume" to "Volume ${request.mangaImageUrl}" // Add manga volume
                                )
                                firestore.collection("chats").add(chat)
                                    .addOnSuccessListener {
                                        println("Chat created successfully.") // Log success

                                        android.widget.Toast.makeText(
                                            context,
                                            "Request accepted, please go to chat", // Toast message
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()

                                        // Remove the request from Firebase
                                        firestore.collection("requests").document(request.id)
                                            .delete()
                                            .addOnSuccessListener {
                                                println("Request deleted after acceptance.") // Log success
                                                onRequestDeleted(request.id) // Remove from UI
                                            }
                                            .addOnFailureListener { e ->
                                                println("Error deleting request after acceptance: ${e.message}") // Log error
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        println("Error creating chat: ${e.message}") // Log error
                                    }
                            } else {
                                // Chat already exists
                                android.widget.Toast.makeText(
                                    context,
                                    "Chat already exists for this manga.", // Toast message
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            println("Error checking for existing chat: ${e.message}") // Log error
                        }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.tick), // Tick icon
                        contentDescription = "Accept", // Accessibility description
                        tint = Color.Green, // Green color for accept
                        modifier = Modifier.size(28.dp) // Icon size
                    )
                }
            }

            // Reject button
            IconButton(onClick = {
                firestore.collection("requests").document(request.id)
                    .delete()
                    .addOnSuccessListener {
                        android.widget.Toast.makeText(
                            context,
                            "Request deleted", // Toast message
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        onRequestDeleted(request.id) // Remove from UI
                    }
                    .addOnFailureListener { e ->
                        println("Error rejecting request: ${e.message}") // Log error
                    }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.cross), // Cross icon
                    contentDescription = "Reject", // Accessibility description
                    tint = Color.Red, // Red color for reject
                    modifier = Modifier.size(40.dp) // Icon size
                )
            }
        }
    }
}

// Data class for a request item
data class RequestItem(
    val id: String = "", // Unique ID of the request
    val mangaId: String = "", // Manga ID associated with the request
    val requestedBy: String = "", // User ID of the person who made the request
    val requestedFrom: String = "", // User ID of the person who owns the manga
    val mangaImageUrl: String = "", // URL of the manga's image
    val title: String = "", // Title of the manga
    val status: String = "", // Current status of the request (e.g., pending, accepted, rejected)
)
