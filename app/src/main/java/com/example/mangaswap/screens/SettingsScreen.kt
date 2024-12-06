package com.example.mangaswap.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.mangaswap.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit, // Callback for the back button action
    onSignOutClick: () -> Unit, // Callback for signing out
    onDeleteAccountClick: () -> Unit // Callback for deleting the account
) {
    val firestore = FirebaseFirestore.getInstance() // Instance of Firestore
    val firebaseAuth = FirebaseAuth.getInstance() // Instance of Firebase Authentication
    val currentUser = firebaseAuth.currentUser // Get the currently logged-in user
    var username by remember { mutableStateOf("Loading...") } // State to hold the username
    var profilePictureUrl by remember { mutableStateOf("") } // State to hold the profile picture URL
    var isUploading by remember { mutableStateOf(false) } // State to track if changes are being uploaded
    val context = LocalContext.current // Access the current context

    // Image picker launcher for selecting a new profile picture
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                // Upload the selected profile picture
                uploadProfilePicture(it, currentUser, context) { newUrl ->
                    profilePictureUrl = newUrl // Update the profile picture URL state
                }
            }
        }
    )

    // Fetch the username and profile picture from Firestore when the screen is loaded
    LaunchedEffect(Unit) {
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        username = document.getString("name") ?: "Unknown" // Get username
                        profilePictureUrl = document.getString("profilePicture") ?: "" // Get profile picture URL
                    }
                }
                .addOnFailureListener {
                    username = "Error loading username" // Error loading username
                }
        }
    }

    // Layout for the settings screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp)
    ) {
        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .background(Color(0xFF73A1D7)) // Blue background for the top bar
                .align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center // Center the text
            ) {
                Text(
                    text = "Settings",
                    fontSize = 30.sp,
                    color = Color.White,
                    fontFamily = sourceSans3,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(
                onClick = onBackClick, // Trigger the back action
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 66.dp)
                .background(Color.Black), // Black background for the main content
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Profile Picture with Edit Icon
            Box(
                modifier = Modifier.size(120.dp) // Box for profile picture and edit icon
            ) {
                if (profilePictureUrl.isNotEmpty()) {
                    // Display profile picture
                    AsyncImage(
                        model = profilePictureUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                } else {
                    // Placeholder image
                    Image(
                        painter = painterResource(id = R.drawable.profile),
                        contentDescription = "Profile Placeholder",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                }
                IconButton(
                    onClick = { imagePickerLauncher.launch("image/*") }, // Launch the image picker
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(30.dp)
                        .background(Color.White, shape = CircleShape) // White background for edit icon
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.pencil),
                        contentDescription = "Edit Profile Picture",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Editable Username TextField
            TextField(
                value = username, // Bind to username state
                onValueChange = { username = it }, // Update username state
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(60.dp),
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    color = Color.White,
                    fontFamily = sourceSans3,
                    fontWeight = FontWeight.Light
                ),
                singleLine = true, // Restrict to a single line
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.White,
                )
            )

            Spacer(modifier = Modifier.height(120.dp))

            // Save Changes Button
            Button(
                onClick = {
                    if (username.isBlank()) {
                        // Show error if username is empty
                        Toast.makeText(context, "Username cannot be empty.", Toast.LENGTH_SHORT).show()
                    } else {
                        saveChanges(currentUser, username, context) // Save changes to Firestore
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(70.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp), // Rounded corners for the button
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF73A1D7))
            ) {
                Text(
                    text = if (isUploading) "Saving..." else "Save changes", // Show "Saving..." if uploading
                    fontSize = 26.sp,
                    color = Color.White,
                    fontFamily = sourceSans3,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Logout Button
            Button(
                onClick = onSignOutClick, // Trigger sign-out action
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(70.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray) // Gray button for logout
            ) {
                Text(
                    text = "Logout",
                    fontSize = 26.sp,
                    color = Color.White,
                    fontFamily = sourceSans3,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Delete Account Button
            Button(
                onClick = onDeleteAccountClick, // Trigger delete account action
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(70.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red) // Red button for account deletion
            ) {
                Text(
                    text = "Delete Account",
                    fontSize = 26.sp,
                    color = Color.White,
                    fontFamily = sourceSans3,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// Function to save the username changes to Firestore
fun saveChanges(currentUser: FirebaseUser?, username: String, context: Context) {
    if (currentUser != null) {
        FirebaseFirestore.getInstance().collection("users").document(currentUser.uid)
            .update("name", username) // Update the name field
            .addOnSuccessListener {
                Toast.makeText(context, "Username updated successfully.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to update username: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

// Function to upload a new profile picture to Firebase Storage
fun uploadProfilePicture(
    uri: android.net.Uri, // URI of the selected image
    currentUser: FirebaseUser?, // Current logged-in user
    context: Context, // Current context
    onProfilePictureUpdated: (String) -> Unit // Callback for updating the profile picture URL
) {
    if (currentUser == null) return // Exit if no user is logged in

    val storageReference = com.google.firebase.storage.FirebaseStorage.getInstance().reference // Firebase Storage reference
    val firestore = FirebaseFirestore.getInstance() // Firestore instance
    val fileName = "profile_pictures/${currentUser.uid}/${java.util.UUID.randomUUID()}.jpg" // Unique file name
    val imageRef = storageReference.child(fileName) // Storage path for the image

    // Upload the image to Firebase Storage
    imageRef.putFile(uri)
        .addOnSuccessListener {
            Toast.makeText(context, "Image uploaded to storage!", Toast.LENGTH_SHORT).show()

            // Retrieve the download URL
            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                // Update Firestore with the new profile picture URL
                firestore.collection("users").document(currentUser.uid)
                    .update("profilePicture", downloadUri.toString())
                    .addOnSuccessListener {
                        Toast.makeText(context, "Profile picture updated successfully!", Toast.LENGTH_SHORT).show()
                        onProfilePictureUpdated(downloadUri.toString()) // Update the profile picture URL
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to update Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Failed to get download URL: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}
