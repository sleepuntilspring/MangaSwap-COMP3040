package com.example.mangaswap.screens

import android.content.pm.PackageManager
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import android.Manifest
import android.location.Location
import com.example.mangaswap.R

@Composable
fun EditBooksScreen(
    onBackClick: () -> Unit, // Callback to handle back button action
    mangaId: String, // ID of the manga to be edited
    onSaveSuccess: () -> Unit, // Callback when saving is successful
    onDeleteSuccess: () -> Unit // Callback when deletion is successful
) {
    val firestore = FirebaseFirestore.getInstance() // Firestore instance for database operations

    // States to manage form inputs and fetched data
    var seriesName by remember { mutableStateOf("") } // State for manga series name
    var authorName by remember { mutableStateOf("") } // State for manga author name
    var volumeNumber by remember { mutableStateOf("") } // State for manga volume number
    var selectedCondition by remember { mutableStateOf("Condition") } // State for manga condition
    var imageUri by remember { mutableStateOf<android.net.Uri?>(null) } // State for selected image URI
    var fetchedImageUrl by remember { mutableStateOf<String?>(null) } // State for fetched image URL from Firestore
    val conditions = listOf("1", "2", "3", "4", "5") // List of conditions for dropdown menu
    val context = LocalContext.current // Local context for displaying Toast messages

    // Fetch the manga data from Firestore when the composable is launched
    LaunchedEffect(mangaId) {
        firestore.collection("mangas").document(mangaId).get()
            .addOnSuccessListener { document ->
                document?.let {
                    // Populate states with fetched data
                    seriesName = it.getString("title") ?: ""
                    authorName = it.getString("author") ?: ""
                    volumeNumber = it.getLong("volume")?.toString() ?: ""
                    selectedCondition = it.getLong("condition")?.toString() ?: "Condition"
                    fetchedImageUrl = it.getString("imageUrl")
                }
            }
            .addOnFailureListener {
                println("Error fetching manga: ${it.message}") // Log any error during fetching
            }
    }

    // Launcher for selecting an image from the gallery
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                imageUri = it // Update the image URI state
            }
        }
    )

    // Validate form inputs
    fun validateInputs(): Boolean {
        return when {
            seriesName.isBlank() -> {
                Toast.makeText(context, "Series name is required.", Toast.LENGTH_SHORT).show()
                false
            }
            authorName.isBlank() -> {
                Toast.makeText(context, "Author name is required.", Toast.LENGTH_SHORT).show()
                false
            }
            volumeNumber.isBlank() -> {
                Toast.makeText(context, "Volume number is required.", Toast.LENGTH_SHORT).show()
                false
            }
            selectedCondition == "Condition" -> {
                Toast.makeText(context, "Please select a condition.", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true // All inputs are valid
        }
    }

    // Delete the manga document from Firestore
    fun deleteManga() {
        firestore.collection("mangas").document(mangaId).delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Manga deleted successfully!", Toast.LENGTH_SHORT).show()
                onDeleteSuccess() // Trigger callback on successful deletion
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to delete manga: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Save the updated manga data along with the location
    fun saveMangaWithLocation() {
        if (!validateInputs()) return // Validate inputs before proceeding
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Location permission not granted.", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the current location of the user
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val data = mutableMapOf<String, Any>(
                    "title" to seriesName,
                    "author" to authorName,
                    "volume" to (volumeNumber.toIntOrNull() ?: 0),
                    "condition" to (selectedCondition.toIntOrNull() ?: 0),
                    "latitude" to location.latitude,
                    "longitude" to location.longitude
                )

                // Check if a new image is selected
                if (imageUri != null) {
                    val storageReference = com.google.firebase.storage.FirebaseStorage.getInstance().reference
                    val fileName = "manga_images/$mangaId/${java.util.UUID.randomUUID()}.jpg"
                    val imageRef = storageReference.child(fileName)

                    // Upload the image to Firebase Storage
                    imageRef.putFile(imageUri!!)
                        .addOnSuccessListener {
                            // Get the download URL of the uploaded image
                            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                data["imageUrl"] = downloadUri.toString()
                                firestore.collection("mangas").document(mangaId).update(data)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Manga updated successfully!", Toast.LENGTH_SHORT).show()
                                        onSaveSuccess() // Trigger callback on successful save
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Failed to update manga: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to upload image: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Update Firestore document without a new image
                    firestore.collection("mangas").document(mangaId).update(data)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Manga updated successfully!", Toast.LENGTH_SHORT).show()
                            onSaveSuccess() // Trigger callback on successful save
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to update manga: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(context, "Failed to fetch location. Ensure location is enabled.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Failed to get location: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp)
    ) {
        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
                .background(Color(0xFF73A1D7))
                .align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Edit Book",
                    fontSize = 30.sp,
                    color = Color.White,
                    fontFamily = sourceSans3,
                    fontWeight = FontWeight.Bold
                )
            }
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

        // Scrollable Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 65.dp, bottom = 50.dp)
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))

                // Clickable for uploading manga picture
                Box(
                    modifier = Modifier
                        .width(170.dp)
                        .height(270.dp)
                        .background(Color.Gray, RoundedCornerShape(12.dp))
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (fetchedImageUrl != null) {
                        AsyncImage(
                            model = fetchedImageUrl,
                            contentDescription = "Fetched Image",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
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
                                text = "Upload a picture of your manga",
                                fontSize = 18.sp,
                                color = Color.White,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Text fields for series name, author, and volume
                TextField(
                    value = seriesName,
                    onValueChange = { seriesName = it },
                    label = { Text("Name of series") },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 8.dp)
                )

                TextField(
                    value = authorName,
                    onValueChange = { authorName = it },
                    label = { Text("Author") },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 8.dp)
                )

                TextField(
                    value = volumeNumber,
                    onValueChange = { newValue ->
                        // Allow only digits
                        if (newValue.all { it.isDigit() }) {
                            volumeNumber = newValue
                        }
                    },
                    label = { Text("Volume") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number // Restrict keyboard input to numbers
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Styled Dropdown for Condition
                var expanded by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 8.dp)
                        .background(Color.Gray, RoundedCornerShape(6.dp))
                        .clickable { expanded = true }
                        .padding(8.dp)
                ) {
                    Text(
                        text = selectedCondition,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        conditions.forEach { condition ->
                            DropdownMenuItem(
                                text = { Text(condition) },
                                onClick = {
                                    selectedCondition = condition
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Save Button
            item {
                Button(
                    onClick = { saveMangaWithLocation() },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(80.dp)
                        .padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF73A1D7))
                ) {
                    Text(
                        text = "Save",
                        fontSize = 32.sp,
                        color = Color.White,
                        fontFamily = sourceSans3,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Delete Button
            item {
                Button(
                    onClick = { deleteManga() },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(80.dp)
                        .padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(
                        text = "Delete",
                        fontSize = 32.sp,
                        color = Color.White,
                        fontFamily = sourceSans3,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
