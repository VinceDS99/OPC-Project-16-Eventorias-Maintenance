package com.openclassrooms.eventorias.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.openclassrooms.eventorias.ui.events.CreateEventScreen
import com.openclassrooms.eventorias.ui.events.EventDetailScreen
import com.openclassrooms.eventorias.ui.events.EventListScreen
import com.openclassrooms.eventorias.ui.events.EventListUiState
import com.openclassrooms.eventorias.ui.events.EventListViewModel
import com.openclassrooms.eventorias.ui.profile.ProfileScreen
import com.openclassrooms.eventorias.ui.theme.DarkBackground
import com.openclassrooms.eventorias.ui.theme.DarkSurface
import com.openclassrooms.eventorias.ui.theme.RedPrimary
import com.openclassrooms.eventorias.ui.theme.White
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState

/**
 * Définit toutes les destinations de navigation de l'application.
 */
sealed class Screen(val route: String, val label: String) {
    object Events : Screen("events", "Events")
    object Profile : Screen("profile", "Profile")

    /** Route avec argument : l'ID de l'événement est passé dans l'URL */
    object EventDetail : Screen("event_detail/{eventId}", "Detail")

    object CreateEvent : Screen("create_event", "Create")
}

/**
 * Composant de navigation principal — affiché quand l'utilisateur est connecté.
 * Gère :
 * - La bottom navigation bar
 * - Le NavHost avec toutes les destinations
 * - La transmission du callback de déconnexion
 */
@Composable
fun AppNavigation(
    onSignOut: () -> Unit = {}
) {
    val navController = rememberNavController()
    // Observe la destination actuelle pour mettre à jour la bottom bar
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            // La bottom bar n'est visible que sur les deux écrans principaux
            // Elle est masquée sur le détail, la création, etc.
            if (currentRoute == Screen.Events.route || currentRoute == Screen.Profile.route) {
                NavigationBar(containerColor = DarkSurface) {
                    NavigationBarItem(
                        selected = currentRoute == Screen.Events.route,
                        onClick = { navController.navigate(Screen.Events.route) },
                        icon = {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = "Onglet événements"
                            )
                        },
                        label = { Text(Screen.Events.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = White,
                            selectedTextColor = White,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = RedPrimary.copy(alpha = 0.3f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Profile.route,
                        onClick = { navController.navigate(Screen.Profile.route) },
                        icon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Onglet profil"
                            )
                        },
                        label = { Text(Screen.Profile.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = White,
                            selectedTextColor = White,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = RedPrimary.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Events.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Écran liste des événements
            composable(Screen.Events.route) {
                EventListScreen(
                    onEventClick = { event ->
                        // Passe l'ID dans la route pour récupérer l'événement côté détail
                        navController.navigate("event_detail/${event.id}")
                    },
                    onCreateEvent = {
                        navController.navigate(Screen.CreateEvent.route)
                    }
                )
            }

            // Écran profil — reçoit onSignOut depuis MainActivity via AppNavigation
            composable(Screen.Profile.route) {
                ProfileScreen(onSignOut = onSignOut)
            }

            // Écran détail — récupère l'événement depuis le ViewModel de la liste
            // (évite un appel Firestore supplémentaire — green code)
            composable("event_detail/{eventId}") { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                // Récupère le ViewModel de la liste (déjà instancié, données déjà chargées)
                val eventsViewModel: EventListViewModel = hiltViewModel()
                val uiState by eventsViewModel.uiState.collectAsState()
                // Trouve l'événement dans la liste déjà chargée par son ID
                val event = (uiState as? EventListUiState.Success)
                    ?.events?.find { it.id == eventId }

                if (event != null) {
                    EventDetailScreen(
                        event = event,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            // Écran création d'événement
            composable(Screen.CreateEvent.route) {
                CreateEventScreen(
                    onBack = { navController.popBackStack() },
                    onEventCreated = { navController.popBackStack() }
                )
            }
        }
    }
}