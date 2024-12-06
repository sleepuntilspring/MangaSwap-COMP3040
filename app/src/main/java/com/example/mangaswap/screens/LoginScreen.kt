package com.example.mangaswap.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mangaswap.R

// Define the font family for the app's theme, using different font weights for various text styles
val sourceSans3 = FontFamily(
    Font(R.font.sourcesans3_light, FontWeight.Light), // Light font weight
    Font(R.font.sourcesans3_medium, FontWeight.Medium), // Medium font weight
    Font(R.font.sourcesans3_regular, FontWeight.Normal), // Regular font weight
    Font(R.font.sourcesans3_bold, FontWeight.Bold), // Bold font weight
    Font(R.font.sourcesans3_semibold, FontWeight.SemiBold), // Semi-bold font weight
    Font(R.font.sourcesans3_extrabold, FontWeight.ExtraBold), // Extra-bold font weight
    Font(R.font.sourcesans3_black, FontWeight.Black) // Black font weight (heaviest)
)

@Composable
fun LoginScreen(
    onGoogleSignInClick: () -> Unit // Callback for handling Google Sign-In button click
) {
    // Background gradient for the login screen
    Box(
        modifier = Modifier
            .fillMaxSize() // Fill the entire screen
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color.Black, Color.Gray) // Gradient from black to gray
                )
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, // Center items horizontally
            modifier = Modifier
                .fillMaxSize() // Fill the available space
                .padding(horizontal = 8.dp, vertical = 32.dp) // Add padding around the column
        ) {
            // Spacer at the top to adjust vertical alignment of the content
            Spacer(modifier = Modifier.weight(0.6f))

            // App title
            Text(
                text = "MangaSwap", // App name
                fontSize = 64.sp, // Large font size for title
                fontWeight = FontWeight.Black, // Boldest font weight for emphasis
                color = Color.White, // White text color
                fontFamily = sourceSans3 // Use custom font family
            )
            Spacer(modifier = Modifier.height(8.dp)) // Small space below the title

            // App slogan
            Text(
                text = "Swap, Collect, Connect - Your Manga Journey Awaits!", // Motivational slogan
                fontSize = 38.sp, // Slightly smaller font size than the title
                fontFamily = sourceSans3, // Use custom font family
                fontWeight = FontWeight.SemiBold, // Semi-bold font weight for emphasis
                color = Color.White, // White text color
                modifier = Modifier
                    .padding(horizontal = 8.dp) // Add horizontal padding
                    .fillMaxWidth(), // Ensure text spans the full width of the screen
                lineHeight = 32.sp, // Adjust spacing between lines
                textAlign = TextAlign.Center // Center-align the text
            )

            // Spacer to push the buttons further down the screen
            Spacer(modifier = Modifier.weight(1.5f))

            // Google Sign-In Button
            Button(
                onClick = { onGoogleSignInClick() }, // Trigger the provided sign-in callback when clicked
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF73A1D7)), // Custom button color
                shape = RoundedCornerShape(24.dp), // Rounded corners for the button
                modifier = Modifier
                    .fillMaxWidth(0.8f) // Make the button 80% of the screen width
                    .height(60.dp) // Increase the button height for better accessibility
            ) {
                // Row layout for Google logo and button text
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.google), // Google logo
                        contentDescription = null, // No content description for decorative image
                        modifier = Modifier.size(32.dp) // Adjust size of the Google logo
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // Space between logo and text
                    Text(
                        text = "Sign In with Google", // Button label
                        color = Color.White, // White text color
                        fontFamily = sourceSans3, // Use custom font family
                        fontWeight = FontWeight.Normal, // Regular font weight
                        fontSize = 25.sp // Medium-large font size
                    )
                }
            }
            Spacer(modifier = Modifier.height(25.dp)) // Add space between button and bottom of screen

            // Spacer at the bottom to maintain consistent vertical alignment
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
