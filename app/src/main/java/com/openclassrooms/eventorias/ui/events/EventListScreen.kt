package com.openclassrooms.eventorias.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.openclassrooms.eventorias.data.model.Event
import com.openclassrooms.eventorias.ui.theme.DarkBackground
import com.openclassrooms.eventorias.ui.theme.DarkSurface
import com.openclassrooms.eventorias.ui.theme.RedPrimary
import com.openclassrooms.eventorias.ui.theme.White

/**
 * Écran principal affichant la liste des événements.
 * Gère trois états : Loading (spinner), Error (message + retry), Success (liste).
 * La barre de recherche filtre en temps réel via le ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    viewModel: EventListViewModel = hiltViewModel(),
    onCreateEvent: () -> Unit = {},
    onEventClick: (Event) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Contrôle la visibilité de la barre de recherche dans le TopAppBar
    var isSearchVisible by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        if (isSearchVisible) {
                            // Mode recherche : TextField remplace le titre
                            TextField(
                                value = searchQuery,
                                onValueChange = { viewModel.onSearchQueryChange(it) },
                                placeholder = { Text("Rechercher...", color = Color.Gray) },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedTextColor = White,
                                    unfocusedTextColor = White,
                                    cursorColor = White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text(
                                text = "Event list",
                                color = White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    },
                    actions = {
                        // Icône loupe — bascule entre titre et barre de recherche
                        IconButton(onClick = {
                            isSearchVisible = !isSearchVisible
                            // Vide la recherche quand on ferme la barre
                            if (!isSearchVisible) viewModel.onSearchQueryChange("")
                        }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Rechercher des événements",
                                tint = White
                            )
                        }
                        // Icône tri — non fonctionnel
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Trier les événements",
                                tint = White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
                )
            }
        },
        // FAB rouge pour créer un nouvel événement
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateEvent,
                containerColor = RedPrimary,
                contentColor = White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Créer un événement")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is EventListUiState.Loading -> {
                    // Spinner centré pendant le chargement Firestore
                    CircularProgressIndicator(color = White)
                }
                is EventListUiState.Error -> {
                    ErrorState(onRetry = { viewModel.retry() })
                }
                is EventListUiState.Success -> {
                    if (state.events.isEmpty()) {
                        Text("Aucun événement", color = Color.Gray)
                    } else {
                        // LazyColumn : équivalent Compose de RecyclerView
                        // Seuls les items visibles sont composés (performances)
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            // key = it.id optimise les recompositions lors des mises à jour
                            items(state.events, key = { it.id }) { event ->
                                EventItem(
                                    event = event,
                                    onClick = { onEventClick(event) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Item de la liste des événements.
 * Layout : avatar circulaire | titre + date | image pleine hauteur
 *
 * @param event L'événement à afficher
 * @param onClick Callback de navigation vers le détail
 */
@Composable
fun EventItem(
    event: Event,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .clickable { onClick() }
            .height(72.dp), // Hauteur fixe pour que l'image remplisse toute la hauteur
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar de l'auteur — cercle à gauche
        AsyncImage(
            model = event.authorPhotoUrl,
            contentDescription = "Photo de l'auteur de l'événement",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(start = 12.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Gray) // Placeholder pendant le chargement
        )

        // Bloc texte central — prend l'espace disponible entre avatar et image
        Column(
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
        ) {
            Text(
                text = event.title,
                color = White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = event.date,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        // Image de l'événement — pleine hauteur à droite avec coins arrondis uniquement à droite
        AsyncImage(
            model = event.imageUrl,
            contentDescription = "Image de l'événement ${event.title}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(100.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                .background(Color.Gray)
        )
    }
}

/**
 * Composant d'état d'erreur affiché quand Firestore échoue.
 * Affiche un cercle d'avertissement, un message et un bouton de retry.
 *
 * @param onRetry Callback appelé quand l'utilisateur appuie sur "Try again"
 */
@Composable
fun ErrorState(onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icône d'avertissement dans un cercle gris
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "!",
                color = White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Error", color = White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "An error has occured,\nplease try again later",
            color = Color.Gray,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.width(160.dp)
        ) {
            Text("Try again", color = White)
        }
    }
}