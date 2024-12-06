package com.example.mangaswap.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import coil.compose.AsyncImage
import com.example.mangaswap.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileScreen(
    onSettingsClick: () -> Unit, // Callback for navigating to the settings screen
    onHomeClick: () -> Unit, // Callback for navigating to the home screen
    onRequestsClick: () -> Unit, // Callback for navigating to the requests screen
    onChatClick: () -> Unit, // Callback for navigating to the chat screen
    onProfileClick: () -> Unit, // Callback for navigating to the profile screen
    onAddBooksClick: () -> Unit, // Callback for adding a new book
    onMangaClick: (String) -> Unit // Callback for clicking on a manga
) {
    val firestore = FirebaseFirestore.getInstance() // Firestore instance for fetching data
    val currentUser = FirebaseAuth.getInstance().currentUser // Current logged-in user
    var userName by remember { mutableStateOf("Loading...") } // State for user's name
    var profilePictureUrl by remember { mutableStateOf("") } // State for user's profile picture URL
    var myBooks by remember { mutableStateOf<List<Manga>>(emptyList()) } // State for user's manga collection

    // Fetch user profile details from Firestore
    LaunchedEffect(Unit) {
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        userName = document.getString("name") ?: "Unknown" // Set the user's name
                        profilePictureUrl = document.getString("profilePicture") ?: "" // Set the profile picture URL
                    }
                }
                .addOnFailureListener {
                    userName = "Error loading name" // Fallback in case of an error
                }
        }
    }

    // Fetch user's manga collection from Firestore
    LaunchedEffect(Unit) {
        if (currentUser != null) {
            firestore.collection("mangas")
                .whereEqualTo("owner", currentUser.uid) // Query for mangas owned by the user
                .get()
                .addOnSuccessListener { snapshot ->
                    val books = snapshot.documents.mapNotNull { document ->
                        try {
                            document.toObject(Manga::class.java)?.apply {
                                mangaId = document.id // Set the Firestore document ID as mangaId
                            }
                        } catch (e: Exception) {
                            println("Failed to map document: ${e.message}")
                            null // Ignore mapping failures
                        }
                    }
                    myBooks = books // Update the state with the fetched mangas
                }
                .addOnFailureListener { e ->
                    println("Error fetching mangas: ${e.message}") // Log any errors
                }
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
                .fillMaxWidth() // Span the full width of the screen
                .height(66.dp) // Fixed height
                .background(Color(0xFF73A1D7)) // Background color
                .align(Alignment.TopCenter) // Align at the top center
        ) {
            Box(
                modifier = Modifier.fillMaxSize(), // Fill the top bar
                contentAlignment = Alignment.Center // Center-align the title
            ) {
                Text(
                    text = "Profile", // Title text
                    fontSize = 30.sp, // Font size
                    color = Color.White, // Text color
                    fontFamily = sourceSans3, // Custom font family
                    fontWeight = FontWeight.Bold // Bold font weight
                )
            }
            IconButton(
                onClick = onSettingsClick, // Trigger the settings navigation
                modifier = Modifier
                    .align(Alignment.CenterEnd) // Align the settings button to the right
                    .padding(end = 8.dp) // Add padding to the right
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.settings), // Settings icon
                    contentDescription = "Settings", // Accessibility description
                    tint = Color.White, // Icon color
                    modifier = Modifier.size(40.dp) // Icon size
                )
            }
        }

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize() // Fill the entire content area
                .padding(top = 66.dp, bottom = 80.dp) // Add padding for the top bar and bottom navigation
                .background(Color.Black), // Background color
            horizontalAlignment = Alignment.CenterHorizontally // Center-align content horizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp)) // Add vertical spacing

            // Profile Picture and Name
            if (profilePictureUrl.isNotEmpty()) {
                AsyncImage(
                    model = profilePictureUrl, // Profile picture URL
                    contentDescription = "Profile Picture", // Accessibility description
                    modifier = Modifier
                        .size(90.dp) // Image size
                        .clip(CircleShape) // Clip to a circle shape
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.profile), // Placeholder image
                    contentDescription = "Profile Placeholder", // Accessibility description
                    modifier = Modifier
                        .size(90.dp) // Image size
                        .clip(CircleShape) // Clip to a circle shape
                )
            }

            Spacer(modifier = Modifier.height(8.dp)) // Add vertical spacing

            Text(
                text = userName, // Display user's name
                fontSize = 22.sp, // Font size
                color = Color.White, // Text color
                fontFamily = sourceSans3, // Custom font family
                fontWeight = FontWeight.SemiBold // Semi-bold font weight
            )

            Spacer(modifier = Modifier.height(16.dp)) // Add vertical spacing

            // Display user's manga collection or a message if none are available
            if (myBooks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth() // Span the full width
                        .fillMaxHeight(0.75f) // Occupy 75% of the available height
                        .background(Color.LightGray, RoundedCornerShape(16.dp)) // Light gray background with rounded corners
                        .padding(8.dp), // Add padding
                    contentAlignment = Alignment.Center // Center-align the text
                ) {
                    Text(
                        text = "No mangas available", // Message when no mangas are present
                        fontSize = 30.sp, // Font size
                        fontFamily = sourceSans3, // Custom font family
                        fontWeight = FontWeight.Bold, // Bold font weight
                        color = Color.Black // Text color
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2), // Display items in 2 columns
                    modifier = Modifier
                        .fillMaxWidth() // Span the full width
                        .fillMaxHeight(0.75f) // Occupy 75% of the available height
                        .background(Color.LightGray, RoundedCornerShape(16.dp)) // Light gray background with rounded corners
                        .padding(8.dp) // Add padding
                ) {
                    items(myBooks) { book -> // Iterate through the user's mangas
                        Box(
                            modifier = Modifier
                                .padding(8.dp) // Add padding around each item
                                .clip(RoundedCornerShape(8.dp)) // Rounded corners for each box
                                .clickable { onMangaClick(book.mangaId) } // Handle item clicks
                                .background(Color(0xFF73A1D7)) // Background color for each item
                                .fillMaxWidth() // Fill the width of the column
                                .height(200.dp), // Fixed height
                            contentAlignment = Alignment.Center // Center-align content within the box
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally, // Center-align content horizontally
                                verticalArrangement = Arrangement.Center, // Center-align content vertically
                                modifier = Modifier.padding(8.dp) // Add padding inside the box
                            ) {
                                AsyncImage(
                                    model = book.imageUrl, // Manga image URL
                                    contentDescription = book.title, // Accessibility description
                                    modifier = Modifier
                                        .size(130.dp) // Image size
                                        .clip(RoundedCornerShape(8.dp)) // Rounded corners for the image
                                )
                                Spacer(modifier = Modifier.height(8.dp)) // Add vertical spacing
                                Text(
                                    text = book.title, // Manga title
                                    fontSize = 14.sp, // Font size
                                    fontFamily = sourceSans3, // Custom font family
                                    fontWeight = FontWeight.SemiBold, // Semi-bold font weight
                                    color = Color.White // Text color
                                )
                                Text(
                                    text = "Volume: ${book.volume}", // Manga volume
                                    fontSize = 12.sp, // Font size
                                    fontFamily = sourceSans3, // Custom font family
                                    fontWeight = FontWeight.Normal, // Normal font weight
                                    color = Color.White // Text color
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(5.dp)) // Add vertical spacing

            // Add A Book Button
            Button(
                onClick = onAddBooksClick, // Trigger the add books action
                modifier = Modifier
                    .fillMaxWidth(0.8f) // Button width (80% of the screen width)
                    .height(70.dp) // Button height
                    .padding(vertical = 8.dp), // Add padding around the button
                shape = RoundedCornerShape(24.dp), // Rounded corners for the button
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF73A1D7)) // Button color
            ) {
                Text(
                    text = "Add A Book!", // Button text
                    fontSize = 30.sp, // Font size
                    color = Color.White, // Text color
                    fontFamily = sourceSans3, // Custom font family
                    fontWeight = FontWeight.SemiBold // Semi-bold font weight
                )
            }
        }

        // Bottom Navigation Bar
        Box(
            modifier = Modifier
                .fillMaxWidth() // Span the full width of the screen
                .height(100.dp) // Fixed height for the navigation bar
                .background(Color(0xFF73A1D7)) // Navigation bar background color
                .align(Alignment.BottomCenter) // Align at the bottom center
                .padding(bottom = 50.dp) // Add padding for bottom navigation
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp), // Add horizontal padding inside the navigation bar
                verticalAlignment = Alignment.CenterVertically, // Center-align items vertically
                horizontalArrangement = Arrangement.SpaceAround // Space items evenly within the row
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
