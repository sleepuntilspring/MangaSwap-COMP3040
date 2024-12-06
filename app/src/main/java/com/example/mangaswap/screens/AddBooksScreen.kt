package com.example.mangaswap.screens

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.mangaswap.R
import com.google.firebase.auth.FirebaseUser

@Composable
fun AddBooksScreen(
    onBackClick: () -> Unit, // Callback function to handle the back navigation
    onSaveSuccess: () -> Unit // Callback function to handle successful save operation
) {
    val firestore = FirebaseFirestore.getInstance() // Firestore instance to interact with the database
    val auth = FirebaseAuth.getInstance() // Firebase authentication instance
    val currentUser = auth.currentUser // Get the current authenticated user

    // Mutable states for user input fields
    var seriesName by remember { mutableStateOf("") } // Name of the manga series
    var authorName by remember { mutableStateOf("") } // Author's name
    var volumeNumber by remember { mutableStateOf("") } // Volume number of the manga
    var selectedCondition by remember { mutableStateOf("Condition") } // Condition of the manga (e.g., 1-5 scale)
    val conditions = listOf("1", "2", "3", "4", "5") // List of conditions for the dropdown menu
    var imageUri by remember { mutableStateOf<android.net.Uri?>(null) } // URI for the selected image
    var isUploading by remember { mutableStateOf(false) } // State to track if uploading is in progress
    val context = LocalContext.current // Current application context for showing Toasts

    // Image picker launcher to select an image from the device
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                imageUri = it // Update the selected image URI
            }
        }
    )

    // Location permission launcher to request location access
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // If location permission is granted, validate inputs and save the manga with location
                if (validateInputs(context, seriesName, authorName, volumeNumber, selectedCondition, imageUri)) {
                    saveMangaWithLocation(
                        context = context,
                        firestore = firestore,
                        currentUser = currentUser,
                        seriesName = seriesName,
                        authorName = authorName,
                        volumeNumber = volumeNumber,
                        selectedCondition = selectedCondition,
                        imageUri = imageUri,
                        onSaveSuccess = onSaveSuccess
                    )
                }
            } else {
                // If permission is denied, show a Toast message
                Toast.makeText(context, "Location permission is required to save manga.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Main UI layout
    Box(
        modifier = Modifier
            .fillMaxSize() // Fills the entire screen
            .padding(top = 20.dp) // Padding at the top
    ) {
        // Top bar with title and back button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
                .background(Color(0xFF73A1D7)) // Blue background color
                .align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Title text
                Text(
                    text = "Add Books",
                    fontSize = 30.sp,
                    color = Color.White,
                    fontFamily = sourceSans3, // Custom font
                    fontWeight = FontWeight.Bold
                )
            }
            // Back button
            IconButton(
                onClick = onBackClick,
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

        // Scrollable content with LazyColumn
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 65.dp, bottom = 50.dp) // Padding for the top and bottom
                .background(Color.Black), // Black background
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp)) // Spacer for vertical spacing

                // Box for uploading manga picture
                Box(
                    modifier = Modifier
                        .width(170.dp)
                        .height(270.dp)
                        .background(Color.Gray, RoundedCornerShape(12.dp)) // Gray background with rounded corners
                        .clickable { imagePickerLauncher.launch("image/*") }, // Launch image picker
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        // Display selected image
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Placeholder UI for image upload
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.plus),
                                contentDescription = "Add Picture",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Add a picture of your manga",
                                fontSize = 18.sp,
                                color = Color.White,
                                fontFamily = sourceSans3,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // TextField for series name
                TextField(
                    value = seriesName,
                    onValueChange = { seriesName = it },
                    label = { Text("Name of series") },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 8.dp)
                )

                // TextField for author name
                TextField(
                    value = authorName,
                    onValueChange = { authorName = it },
                    label = { Text("Author") },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 8.dp)
                )

                // TextField for volume number with numeric input
                TextField(
                    value = volumeNumber,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) { // Ensures only numeric input
                            volumeNumber = newValue
                        }
                    },
                    label = { Text("Volume") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dropdown menu for selecting condition
                var expanded by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 8.dp)
                        .background(Color.Gray, RoundedCornerShape(6.dp))
                        .clickable { expanded = true } // Expand dropdown on click
                        .padding(8.dp)
                ) {
                    Text(
                        text = selectedCondition,
                        color = Color.White,
                        fontFamily = sourceSans3,
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false } // Close dropdown when dismissed
                    ) {
                        conditions.forEach { condition ->
                            DropdownMenuItem(
                                text = { Text(condition) },
                                onClick = {
                                    selectedCondition = condition // Update selected condition
                                    expanded = false // Close dropdown
                                }
                            )
                        }
                    }
                }
            }

            // Save button
            item {
                Button(
                    onClick = {
                        if (validateInputs(context, seriesName, authorName, volumeNumber, selectedCondition, imageUri)) {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(80.dp)
                        .padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF73A1D7)) // Blue button
                ) {
                    Text(
                        text = if (isUploading) "Saving..." else "Save",
                        fontSize = 34.sp,
                        color = Color.White,
                        fontFamily = sourceSans3,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

fun saveMangaWithLocation(
    context: Context, // Context to display Toast messages and interact with system services
    firestore: FirebaseFirestore, // Firestore instance to save manga data
    currentUser: FirebaseUser?, // Current logged-in user; null if not authenticated
    seriesName: String, // Name of the manga series
    authorName: String, // Author of the manga
    volumeNumber: String, // Volume number of the manga
    selectedCondition: String, // Selected condition (e.g., 1-5 scale)
    imageUri: android.net.Uri?, // URI of the selected image
    onSaveSuccess: () -> Unit // Callback function to execute on successful save
) {
    // Initialize a client to retrieve the device's current location
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // Check if location permission is granted
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(context, "Location permission not granted.", Toast.LENGTH_SHORT).show()
        return // Exit the function if permission is not granted
    }

    // Fetch the last known location of the device
    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
        if (location != null) {
            // Log the latitude and longitude for debugging
            println("Saving manga with location: ${location.latitude}, ${location.longitude}")

            // Retrieve the current user's unique ID; exit if null
            val userId = currentUser?.uid ?: return@addOnSuccessListener

            // Create a data map to store the manga details
            val mangaData = mutableMapOf(
                "title" to seriesName,
                "author" to authorName,
                "volume" to (volumeNumber.toIntOrNull() ?: 0), // Convert volume to an integer; default to 0 if invalid
                "condition" to (selectedCondition.toIntOrNull() ?: 0), // Convert condition to an integer; default to 0 if invalid
                "owner" to userId, // Assign the current user's ID as the owner
                "latitude" to location.latitude, // Include the device's latitude
                "longitude" to location.longitude // Include the device's longitude
            )

            // If an image URI is provided, upload the image to Firebase Storage
            if (imageUri != null) {
                val storageReference = com.google.firebase.storage.FirebaseStorage.getInstance().reference
                val fileName = "manga_images/$userId/${java.util.UUID.randomUUID()}.jpg" // Generate a unique file name
                val imageRef = storageReference.child(fileName)

                // Upload the image file
                imageRef.putFile(imageUri).addOnSuccessListener {
                    // Retrieve the download URL of the uploaded image
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        mangaData["imageUrl"] = downloadUri.toString() // Add the image URL to the manga data

                        // Save the manga data to Firestore
                        firestore.collection("mangas").add(mangaData)
                            .addOnSuccessListener {
                                // Show a success message and invoke the callback function
                                Toast.makeText(context, "Manga saved successfully!", Toast.LENGTH_SHORT).show()
                                onSaveSuccess()
                            }
                            .addOnFailureListener { e ->
                                // Show an error message if saving fails
                                Toast.makeText(context, "Failed to save manga: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }.addOnFailureListener { e ->
                        // Show an error message if retrieving the download URL fails
                        Toast.makeText(context, "Failed to get download URL: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    // Show an error message if uploading the image fails
                    Toast.makeText(context, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Show an error message if no image is selected
                Toast.makeText(context, "Please select an image.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Show an error message if location is unavailable
            Toast.makeText(context, "Failed to fetch location. Ensure location is enabled.", Toast.LENGTH_SHORT).show()
        }
    }.addOnFailureListener { e ->
        // Show an error message if retrieving location fails
        Toast.makeText(context, "Failed to get location: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

// Function to validate user inputs before saving
fun validateInputs(
    context: Context, // Context to display Toast messages
    seriesName: String, // Name of the manga series
    authorName: String, // Author of the manga
    volumeNumber: String, // Volume number of the manga
    selectedCondition: String, // Selected condition of the manga
    imageUri: android.net.Uri? // URI of the selected image
): Boolean {
    // Validate each input field and show appropriate error messages
    return when {
        seriesName.isBlank() -> {
            Toast.makeText(context, "Series name is required.", Toast.LENGTH_SHORT).show()
            false // Return false if the series name is blank
        }
        authorName.isBlank() -> {
            Toast.makeText(context, "Author name is required.", Toast.LENGTH_SHORT).show()
            false // Return false if the author name is blank
        }
        volumeNumber.isBlank() -> {
            Toast.makeText(context, "Volume number is required.", Toast.LENGTH_SHORT).show()
            false // Return false if the volume number is blank
        }
        selectedCondition == "Condition" -> {
            Toast.makeText(context, "Please select a condition.", Toast.LENGTH_SHORT).show()
            false // Return false if no condition is selected
        }
        imageUri == null -> {
            Toast.makeText(context, "Please select an image.", Toast.LENGTH_SHORT).show()
            false // Return false if no image is selected
        }
        else -> true // Return true if all inputs are valid
    }
}

