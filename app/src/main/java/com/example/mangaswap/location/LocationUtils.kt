package com.example.mangaswap.location

/**
 * Calculates the distance in kilometers between two geographical points.
 *
 * @param userLatitude The latitude of the user's location.
 * @param userLongitude The longitude of the user's location.
 * @param targetLatitude The latitude of the target location.
 * @param targetLongitude The longitude of the target location.
 * @return The distance between the user's location and the target location in kilometers.
 */
fun calculateDistance(
    userLatitude: Double,
    userLongitude: Double,
    targetLatitude: Double,
    targetLongitude: Double
): Double {
    // Create a Location object for the user's location
    val userLocation = android.location.Location("").apply {
        latitude = userLatitude // Set the latitude of the user's location
        longitude = userLongitude // Set the longitude of the user's location
    }

    // Create a Location object for the target location
    val targetLocation = android.location.Location("").apply {
        latitude = targetLatitude // Set the latitude of the target location
        longitude = targetLongitude // Set the longitude of the target location
    }

    // Calculate the distance between the two locations in meters and convert to kilometers
    return userLocation.distanceTo(targetLocation) / 1000.0
}


