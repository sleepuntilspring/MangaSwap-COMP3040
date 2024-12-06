package com.example.mangaswap.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
fun MangaDetailsScreen(
    title: String, // The title of the manga to fetch details for
    distance: String, // The distance between the user and the manga's location
    onBackClick: () -> Unit, // Callback for back button click
    onHomeClick: () -> Unit, // Callback for home button click
    onRequestsClick: () -> Unit, // Callback for requests button click
    onChatClick: () -> Unit, // Callback for chat button click
    onProfileClick: () -> Unit // Callback for profile button click
) {
    val firestore = FirebaseFirestore.getInstance() // Firestore instance for data fetching
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid // Current logged-in user's ID
    var manga by remember { mutableStateOf<Manga?>(null) } // State for holding manga details
    var ownerUsername by remember { mutableStateOf("Loading...") } // State for the manga owner's username
    val context = LocalContext.current // Context for showing toasts

    // Fetch manga details and owner's username when the component is launched
    LaunchedEffect(title) {
        try {
            // Query Firestore to fetch manga details using its title
            val querySnapshot = firestore.collection("mangas")
                .whereEqualTo("title", title)
                .get()
                .await()
            val document = querySnapshot.documents.firstOrNull() // Get the first document (if any)
            manga = document?.toObject(Manga::class.java)?.apply {
                this.mangaId = document.id // Set the Firestore document ID as the manga ID
            }

            // Fetch the owner's username from Firestore if manga is available
            manga?.owner?.let { ownerId ->
                val ownerDoc = firestore.collection("users").document(ownerId).get().await()
                ownerUsername = ownerDoc.getString("name") ?: "Unknown"
            }
        } catch (e: Exception) {
            println("Error fetching manga or owner: ${e.message}")
            ownerUsername = "Unknown" // Fallback value in case of an error
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize() // Fill the entire screen
            .padding(top = 20.dp) // Add padding at the top
    ) {
        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth() // Make the bar span the full width
                .height(65.dp) // Fixed height for the bar
                .background(Color(0xFF73A1D7)) // Background color
                .align(Alignment.TopStart) // Align at the top
        ) {
            Box(
                modifier = Modifier.fillMaxSize(), // Fill the entire bar
                contentAlignment = Alignment.Center // Center-align the text
            ) {
                Text(
                    text = "Manga Details", // Title of the screen
                    fontSize = 30.sp, // Font size
                    color = Color.White, // Text color
                    fontFamily = sourceSans3, // Custom font family
                    fontWeight = FontWeight.Bold // Bold font weight
                )
            }

            // Back Button
            IconButton(
                onClick = onBackClick, // Trigger the back action
                modifier = Modifier
                    .padding(start = 8.dp) // Add padding to the left
                    .align(Alignment.CenterStart) // Align to the left
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back), // Back icon
                    contentDescription = "Back", // Accessibility description
                    tint = Color.White, // Icon color
                    modifier = Modifier.size(30.dp) // Icon size
                )
            }
        }

        // Main Content
        Box(
            modifier = Modifier
                .fillMaxWidth() // Fill the width of the screen
                .padding(top = 65.dp) // Add padding below the top bar
                .align(Alignment.TopStart) // Align content to the top
                .background(Color.Black) // Black background
        ) {
            if (manga != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize() // Fill available space
                        .padding(horizontal = 16.dp), // Add padding on the sides
                    horizontalAlignment = Alignment.CenterHorizontally // Center content horizontally
                ) {
                    Spacer(modifier = Modifier.height(12.dp)) // Add vertical space

                    // Display the manga cover image
                    AsyncImage(
                        model = manga!!.imageUrl, // URL of the image
                        contentDescription = manga!!.title, // Description for accessibility
                        modifier = Modifier
                            .size(width = 170.dp, height = 270.dp) // Image size
                            .padding(bottom = 16.dp), // Padding below the image
                    )

                    Spacer(modifier = Modifier.height(5.dp)) // Add vertical space

                    // Display manga details
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(), // Make column take full width
                        horizontalAlignment = Alignment.Start // Align content to the start
                    ) {
                        item {
                            Text(
                                text = "Name: ${manga!!.title}", // Manga title
                                fontSize = 22.sp,
                                color = Color.White,
                                fontFamily = sourceSans3,
                                fontWeight = FontWeight.Normal
                            )
                        }
                        item {
                            Text(
                                text = "Volume: ${manga!!.volume}", // Manga volume
                                fontSize = 22.sp,
                                color = Color.White,
                                fontFamily = sourceSans3,
                                fontWeight = FontWeight.Normal
                            )
                        }
                        item {
                            Text(
                                text = "Author: ${manga!!.author}", // Author's name
                                fontSize = 22.sp,
                                color = Color.White,
                                fontFamily = sourceSans3,
                                fontWeight = FontWeight.Normal
                            )
                        }
                        item {
                            Text(
                                text = "Condition: ${manga!!.condition}/5", // Manga condition
                                fontSize = 22.sp,
                                color = Color.White,
                                fontFamily = sourceSans3,
                                fontWeight = FontWeight.Normal
                            )
                        }
                        item {
                            Text(
                                text = "Distance: ${distance}km", // Distance to manga's location
                                fontSize = 22.sp,
                                color = Color.White,
                                fontFamily = sourceSans3,
                                fontWeight = FontWeight.Normal
                            )
                        }
                        item {
                            Text(
                                text = "Owner: $ownerUsername", // Owner's username
                                fontSize = 22.sp,
                                color = Color.White,
                                fontFamily = sourceSans3,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }


                    Spacer(modifier = Modifier.height(10.dp)) // Add vertical space

                    // Button to request an exchange
                    Button(
                        onClick = {
                            if (currentUserId != null) {
                                if (currentUserId == manga!!.owner) {
                                    // Prevent requesting own manga
                                    Toast.makeText(context, "You cannot request your own manga.", Toast.LENGTH_SHORT).show()
                                } else {
                                    // Check if a request already exists and create one if not
                                    firestore.collection("requests")
                                        .whereEqualTo("mangaId", manga!!.mangaId)
                                        .whereEqualTo("requestedBy", currentUserId)
                                        .get()
                                        .addOnSuccessListener { querySnapshot ->
                                            if (querySnapshot.isEmpty) {
                                                val request = hashMapOf(
                                                    "mangaId" to manga!!.mangaId, // Pass manga ID
                                                    "requestedBy" to currentUserId,
                                                    "requestedFrom" to manga!!.owner,
                                                    "status" to "pending",
                                                )
                                                firestore.collection("requests")
                                                    .add(request)
                                                    .addOnSuccessListener {
                                                        Toast.makeText(context, "Request sent successfully!", Toast.LENGTH_SHORT).show()
                                                    }
                                                    .addOnFailureListener { e ->
                                                        println("Error creating request: ${e.message}")
                                                        Toast.makeText(context, "Failed to send request.", Toast.LENGTH_SHORT).show()
                                                    }
                                            } else {
                                                Toast.makeText(context, "Request already exists for this manga.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            println("Error checking for existing requests: ${e.message}")
                                        }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.7f) // Button width
                            .height(50.dp), // Button height
                        shape = RoundedCornerShape(24.dp), // Rounded corners
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF73A1D7)) // Button color
                    ) {
                        Text(
                            text = "Request Exchange", // Button text
                            fontSize = 24.sp, // Font size
                            color = Color.White, // Text color
                            fontFamily = sourceSans3, // Font family
                            fontWeight = FontWeight.SemiBold // Semi-bold weight
                        )
                    }
                }
            } else {
                // Loading indicator
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black), // Black background
                    contentAlignment = Alignment.Center // Center-align the loading spinner
                ) {
                    CircularProgressIndicator(
                        color = Color.White // White color for the spinner
                    )
                }
            }
        }

        // Bottom Navigation Bar
        Box(
            modifier = Modifier
                .fillMaxWidth() // Fill the width of the screen
                .height(100.dp) // Fixed height
                .background(Color(0xFF73A1D7)) // Blue background
                .align(Alignment.BottomStart) // Align at the bottom
                .padding(bottom = 50.dp) // Padding for navigation bar
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp), // Padding inside the row
                verticalAlignment = Alignment.CenterVertically, // Align items vertically in the center
                horizontalArrangement = Arrangement.SpaceAround // Space items evenly
            ) {
                IconButton(onClick = onHomeClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.home), // Home icon
                        contentDescription = "Home", // Accessibility description
                        modifier = Modifier.size(43.dp), // Icon size
                        tint = Color.White // Icon color
                    )
                }
                IconButton(onClick = onRequestsClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.requests), // Requests icon
                        contentDescription = "Requests", // Accessibility description
                        modifier = Modifier.size(36.dp), // Icon size
                        tint = Color.White // Icon color
                    )
                }
                IconButton(onClick = onChatClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.chat), // Chat icon
                        contentDescription = "Chat", // Accessibility description
                        modifier = Modifier.size(38.dp), // Icon size
                        tint = Color.White // Icon color
                    )
                }
                IconButton(onClick = onProfileClick) {
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