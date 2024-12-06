package com.example.mangaswap.navigation

import android.net.Uri
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mangaswap.screens.AddBooksScreen
import com.example.mangaswap.screens.ChatScreen
import com.example.mangaswap.screens.EditBooksScreen
import com.example.mangaswap.screens.HomeScreen
import com.example.mangaswap.screens.LoginScreen
import com.example.mangaswap.screens.MangaDetailsScreen
import com.example.mangaswap.screens.ProfileScreen
import com.example.mangaswap.screens.RequestScreen
import com.example.mangaswap.screens.SettingsScreen
import com.example.mangaswap.screens.UsersListScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavHostSetup(
    navController: NavHostController, // Navigation controller to handle screen transitions
    isSignedIn: Boolean, // Determines the start destination based on sign-in status
    onGoogleSignIn: () -> Unit, // Callback function for Google Sign-In
    onSignOut: () -> Unit, // Callback function for signing out
    onDeleteAccount: () -> Unit // Callback function for account deletion
) {
    val screens = listOf("home", "requests", "chat", "profile") // List of main screens in the app
    AnimatedNavHost(
        navController = navController,
        startDestination = if (isSignedIn) "home" else "login", // Start at login if not signed in
    ) {
        // Login Screen
        composable("login") {
            LoginScreen(
                onGoogleSignInClick = onGoogleSignIn // Handles Google Sign-In click
            )
        }

        // Home Screen
        composable(
            "home",
            enterTransition = { enterSlideTransition(screens, initialState, targetState) },
            exitTransition = { exitSlideTransition(screens, initialState, targetState) },
            popEnterTransition = { enterSlideTransition(screens, initialState, targetState, isPop = true) },
            popExitTransition = { exitSlideTransition(screens, initialState, targetState, isPop = true) }
        ) {
            HomeScreen(
                onRequestsClick = { navController.navigate("requests") }, // Navigate to Requests screen
                onProfileClick = { navController.navigate("profile") }, // Navigate to Profile screen
                onChatClick = { navController.navigate("chat") }, // Navigate to Chat screen
                navController = navController,
            )
        }

        //  Manga Details Screen
        composable(
            route = "details/{title}/{distance}",
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },  // Pass the title as an argument
                navArgument("distance") { type = NavType.StringType } // Pass the distance as an argument
            ),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) // Slide in from the right
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) // Slide out to the left
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) // Slide in from the left when popping back
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) // Slide out to the right when popping back
            }
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: "" // Extract title
            val distance = backStackEntry.arguments?.getString("distance") ?: "0.0 km" // Extract distance
            MangaDetailsScreen(
                title = title, // Pass the title to MangaDetailsScreen
                distance = distance, // Pass the distance to MangaDetailsScreen
                onBackClick = { navController.popBackStack() }, // Go back to the previous screen
                onHomeClick = { navController.navigate("home") }, // Navigate to Home screen
                onRequestsClick = { navController.navigate("requests") }, // Navigate to Requests screen
                onChatClick = { navController.navigate("chat") }, // Navigate to Chat screen
                onProfileClick = { navController.navigate("profile") } // Navigate to Profile screen
            )
        }

        // Requests Screen
        composable(
            "requests",
            enterTransition = { enterSlideTransition(screens, initialState, targetState) },
            exitTransition = { exitSlideTransition(screens, initialState, targetState) },
            popEnterTransition = { enterSlideTransition(screens, initialState, targetState, isPop = true) },
            popExitTransition = { exitSlideTransition(screens, initialState, targetState, isPop = true) }
        ) {
            RequestScreen(
                onHomeClick = { navController.navigate("home") },
                onRequestsClick = { /* Stay on this screen */ },
                onChatClick = { navController.navigate("chat") },
                onProfileClick = { navController.navigate("profile") }
            )
        }

        // Profile Screen
        composable(
            "profile",
            enterTransition = { enterSlideTransition(screens, initialState, targetState) },
            exitTransition = { exitSlideTransition(screens, initialState, targetState) },
            popEnterTransition = { enterSlideTransition(screens, initialState, targetState, isPop = true) },
            popExitTransition = { exitSlideTransition(screens, initialState, targetState, isPop = true) }
        ) {
            ProfileScreen(
                onSettingsClick = { navController.navigate("settings") }, // Navigate to Settings screen
                onHomeClick = { navController.navigate("home") }, // Navigate to Home screen
                onRequestsClick = { navController.navigate("requests") }, // Navigate to Requests screen
                onChatClick = { navController.navigate("chat") }, // Navigate to Chat screen
                onProfileClick = { /* Stay on this screen */ },
                onAddBooksClick = { navController.navigate("addBooks") }, // Navigate to Add Books screen
                onMangaClick = { mangaId ->
                    navController.navigate("editBooks/$mangaId") // Navigate to Edit Books screen with mangaId
                }
            )
        }

        // Settings Screen
        composable(
            "settings",
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) // Slide in from the right
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) // Slide out to the left
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) // Slide in from the left when popping back
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) // Slide out to the right when popping back
            }
        ) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }, // Navigate back to the Profile screen
                onSignOutClick = onSignOut,
                onDeleteAccountClick = onDeleteAccount
            )
        }

        // Add Books Screen
        composable(
            "addBooks",
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) // Slide in from the right
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) // Slide out to the left
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) // Slide in from the left when popping back
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) // Slide out to the right when popping back
            }
        ) {
            AddBooksScreen(
                onBackClick = { navController.popBackStack() }, // Navigate back to the profile screen
                onSaveSuccess = {
                    navController.popBackStack() // Navigate back to the profile screen on successful save
                }
            )
        }


        // Edit Books Screen with mangaTitle parameter
        composable(
            route = "editBooks/{mangaId}",
            arguments = listOf(navArgument("mangaId") { type = NavType.StringType }),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) // Slide in from the right
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) // Slide out to the left
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) // Slide in from the left when popping back
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) // Slide out to the right when popping back
            }
        ) { backStackEntry ->
            val mangaId = backStackEntry.arguments?.getString("mangaId") ?: ""
            EditBooksScreen(
                onBackClick = { navController.popBackStack() },
                mangaId = mangaId, // Pass the mangaId here
                onSaveSuccess = {
                    navController.popBackStack() // Navigate back after saving
                },
                onDeleteSuccess = {
                    navController.popBackStack() // Navigate back after deleting
                }
            )
        }

        // Chat (Users List) Screen
        composable(
            "chat",
            enterTransition = { enterSlideTransition(screens, initialState, targetState) },
            exitTransition = { exitSlideTransition(screens, initialState, targetState) },
            popEnterTransition = { enterSlideTransition(screens, initialState, targetState, isPop = true) },
            popExitTransition = { exitSlideTransition(screens, initialState, targetState, isPop = true) }
        ) {
            UsersListScreen(
                navController = navController, // Pass NavHostController here
                onHomeClick = { navController.navigate("home") },
                onRequestsClick = { navController.navigate("requests") },
                onChatClick = { /* Stay on this screen */ },
                onProfileClick = { navController.navigate("profile") }
            )
        }

        // Chat Screen
        composable(
            route = "chatScreen/{username}/{profilePicture}/{mangaId}/{chatId}",
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("profilePicture") { type = NavType.StringType },
                navArgument("mangaId") { type = NavType.StringType },
                navArgument("chatId") { type = NavType.StringType }
            ),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) // Slide in from the right
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) // Slide out to the left
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) // Slide in from the left when popping back
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) // Slide out to the right when popping back
            }
        ) { backStackEntry ->
            val username = Uri.decode(backStackEntry.arguments?.getString("username") ?: "Unknown User")
            val profilePicture = Uri.decode(backStackEntry.arguments?.getString("profilePicture") ?: "")
            val mangaId = Uri.decode(backStackEntry.arguments?.getString("mangaId") ?: "")
            val chatId = Uri.decode(backStackEntry.arguments?.getString("chatId") ?: "")

            ChatScreen(
                onBackClick = { navController.popBackStack() },
                username = username,
                profilePicture = profilePicture,
                mangaId = mangaId,
                chatId = chatId // Pass the chatId to the ChatScreen
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun enterSlideTransition(
    screens: List<String>, // List of screens for reference
    initialState: NavBackStackEntry, // Starting screen state
    targetState: NavBackStackEntry, // Target screen state
    isPop: Boolean = false // Determines if the transition is a pop
): EnterTransition? {
    val initialIndex = screens.indexOf(initialState.destination.route)
    val targetIndex = screens.indexOf(targetState.destination.route)

    return if (!isPop) {
        if (targetIndex > initialIndex) {
            slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) // Slide left to right
        } else {
            slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) // Slide right to left
        }
    } else {
        null // No transition for pop enter
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun exitSlideTransition(
    screens: List<String>, // List of screens for reference
    initialState: NavBackStackEntry, // Starting screen state
    targetState: NavBackStackEntry, // Target screen state
    isPop: Boolean = false // Determines if the transition is a pop
): ExitTransition? {
    val initialIndex = screens.indexOf(initialState.destination.route)
    val targetIndex = screens.indexOf(targetState.destination.route)

    return if (isPop) {
        if (targetIndex > initialIndex) {
            slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) // Slide right to left (backwards)
        } else {
            slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) // Slide left to right (backwards)
        }
    } else {
        null // No transition for forward exit
    }
}
