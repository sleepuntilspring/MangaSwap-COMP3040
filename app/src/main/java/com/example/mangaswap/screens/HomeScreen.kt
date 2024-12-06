package com.example.mangaswap.screens

import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.mangaswap.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.os.Parcelable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import kotlinx.parcelize.Parcelize
import android.Manifest
import com.example.mangaswap.location.calculateDistance

@Parcelize
data class Manga(
    var mangaId: String = "", // Unique ID for the manga
    val title: String = "", // Title of the manga
    val volume: Int = 0, // Volume number of the manga
    val author: String = "", // Author of the manga
    val condition: Int = 0, // Condition rating (1-5)
    val imageUrl: String = "", // URL for the manga's image
    val owner: String = "", // Owner's UID
    var ownerName: String = "", // Owner's username
    val latitude: Double = 0.0, // Add latitude of manga's location
    val longitude: Double = 0.0 // Add longitude of manga's location
) : Parcelable

@Composable
fun HomeScreen(
    onRequestsClick: () -> Unit, // Callback for navigating to the Requests screen
    onProfileClick: () -> Unit, // Callback for navigating to the Profile screen
    onChatClick: () -> Unit, // Callback for navigating to the Chat screen
    navController: NavHostController // Navigation controller
) {
    val firestore = FirebaseFirestore.getInstance() // Firestore instance for database operations
    var mangas by remember { mutableStateOf<List<Manga>>(emptyList()) } // List of all mangas
    var users by remember { mutableStateOf<Map<String, String>>(emptyMap()) } // Map of user IDs to usernames
    var isLoading by remember { mutableStateOf(true) } // Loading state for the UI
    var userLatitude by remember { mutableStateOf(0.0) } // Current user's latitude
    var userLongitude by remember { mutableStateOf(0.0) } // Current user's longitude
    val context = LocalContext.current // Local context for accessing resources
    var searchQuery by remember { mutableStateOf("") } // Search query entered by the user

    // Fetch the current user's location
    LaunchedEffect(context) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLatitude = it.latitude
                    userLongitude = it.longitude
                }
            }
        }
    }

    // Fetch user data from Firestore (UID to Username mapping)
    LaunchedEffect(Unit) {
        try {
            val userQuery = firestore.collection("users").get().await()
            users = userQuery.documents.associate { doc ->
                doc.id to (doc.getString("name") ?: "Unknown")
            }
        } catch (e: Exception) {
            println("Error fetching users: ${e.message}")
        }
    }

    // Fetch manga data from Firestore
    LaunchedEffect(users) {
        try {
            val querySnapshot = firestore.collection("mangas").get().await()
            mangas = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Manga::class.java)?.apply {
                    ownerName = users[owner] ?: "Unknown" // Map the owner's UID to their username
                }
            }
        } catch (e: Exception) {
            println("Error fetching mangas: ${e.message}")
        } finally {
            isLoading = false // Data loading is complete
        }
    }

    // Filter and sort mangas based on the search query and distance
    val filteredMangas = if (searchQuery.isBlank()) {
        mangas
    } else {
        mangas.filter { it.title.contains(searchQuery, ignoreCase = true) } // Filter by title
    }.map { manga ->
        val distance = calculateDistance(
            userLatitude,
            userLongitude,
            manga.latitude,
            manga.longitude
        ) // Calculate distance from user
        manga to distance
    }.sortedBy { it.second } // Sort by distance

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp)
    ) {
        // Top Bar with search functionality
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .background(Color(0xFF73A1D7)) // Blue shade for the top bar
                .align(Alignment.TopStart)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it }, // Update search query
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .padding(horizontal = 8.dp), // Adjust padding inside the text field
                    placeholder = {
                        Text(
                            text = "Search for manga",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            fontFamily = sourceSans3,
                            fontWeight = FontWeight.Normal
                        )
                    },
                    singleLine = true, // Single-line input
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.cross), // Clear search icon
                                    contentDescription = "Clear Search",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                )
            }
        }

        // Display a list of mangas or an empty state
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 66.dp) // Start below the top bar
                .background(Color.Black)
        ) {
            if (filteredMangas.isEmpty() && searchQuery.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No mangas available at the moment",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontFamily = sourceSans3,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (filteredMangas.isEmpty() && searchQuery.isNotEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No results found.",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontFamily = sourceSans3,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 100.dp) // Add bottom padding
                ) {
                    items(filteredMangas) { (manga, distance) ->
                        MangaItem(
                            manga = manga,
                            distance = "%.2f km".format(distance), // Format distance
                            onClick = {
                                navController.navigate(
                                    "details/${Uri.encode(manga.title)}/${Uri.encode(distance.toString())}"
                                ) // Navigate to details screen
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
                .background(Color(0xFF73A1D7))
                .align(Alignment.BottomStart)
                .padding(bottom = 50.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(onClick = { /* Handle Home Navigation */ }) {
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

@Composable
fun MangaItem(manga: Manga, distance: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color.Gray, shape = RoundedCornerShape(12.dp))
            .padding(12.dp)
            .clickable { onClick() } // Navigate on click
    ) {
        AsyncImage(
            model = manga.imageUrl, // Load manga image
            contentDescription = manga.title,
            modifier = Modifier
                .width(100.dp)
                .height(150.dp)
                .clip(RoundedCornerShape(15.dp)),
            contentScale = ContentScale.Crop // Crop to fit
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Text(
                text = manga.title,
                fontSize = 18.sp,
                color = Color.White,
                fontFamily = sourceSans3,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Volume: ${manga.volume}",
                fontSize = 16.sp,
                color = Color.White,
                fontFamily = sourceSans3,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = "Author: ${manga.author}",
                fontSize = 16.sp,
                color = Color.White,
                fontFamily = sourceSans3,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = "Condition: ${manga.condition}/5",
                fontSize = 16.sp,
                color = Color.White,
                fontFamily = sourceSans3,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = "Distance: $distance",
                fontSize = 16.sp,
                color = Color.White,
                fontFamily = sourceSans3,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = "Owner: ${manga.ownerName}",
                fontSize = 16.sp,
                color = Color.White,
                fontFamily = sourceSans3,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
