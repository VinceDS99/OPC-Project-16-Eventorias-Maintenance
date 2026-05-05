package com.openclassrooms.eventorias.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
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
import coil.compose.AsyncImage
import com.openclassrooms.eventorias.BuildConfig
import com.openclassrooms.eventorias.data.model.Event
import com.openclassrooms.eventorias.ui.theme.DarkBackground
import com.openclassrooms.eventorias.ui.theme.White
import java.net.URLEncoder

/**
 * Écran de détail d'un événement.
 * Affiche toutes les informations de l'événement :
 * - Image principale pleine largeur
 * - Date avec icône calendrier + avatar auteur
 * - Heure avec icône horloge
 * - Description
 * - Adresse + carte Google Maps Static API côte à côte
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: Event,
    onBack: () -> Unit
) {
    // scrollState permet le défilement vertical si le contenu dépasse l'écran
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = DarkBackground,
        // contentWindowInsets(0) : évite le double padding avec le Scaffold parent
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = event.title,
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    // Flèche retour
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour à la liste des événements",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Image principale
            AsyncImage(
                model = event.imageUrl,
                contentDescription = "Image principale de l'événement ${event.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(Color.Gray) // Fond gris pendant le chargement
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Ligne date + avatar auteur
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = event.date,
                        color = White,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    // Avatar de l'auteur en haut à droite de la section date
                    AsyncImage(
                        model = event.authorPhotoUrl,
                        contentDescription = "Photo de l'auteur de l'événement",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Ligne heure
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = event.time, color = White, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description de l'événement
                Text(
                    text = event.description,
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Adresse à gauche + carte Maps Static à droite
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Adresse texte
                    Text(
                        text = event.location,
                        color = White,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    /**
                     * Construction de l'URL Google Maps Static API.
                     * L'adresse est encodée en URL pour gérer les espaces et caractères spéciaux.
                     * La clé API est lue depuis BuildConfig (injectée depuis local.properties).
                     * Paramètres :
                     * - center : centre de la carte sur l'adresse
                     * - zoom : niveau de zoom (15 = vue de quartier)
                     * - size : dimensions de l'image en pixels
                     * - markers : épingle rouge sur l'adresse
                     */
                    val encodedLocation = URLEncoder.encode(event.location, "UTF-8")
                    val mapUrl = "https://maps.googleapis.com/maps/api/staticmap" +
                            "?center=$encodedLocation" +
                            "&zoom=15" +
                            "&size=300x200" +
                            "&maptype=roadmap" +
                            "&markers=color:red%7C$encodedLocation" +
                            "&key=${BuildConfig.MAPS_API_KEY}"

                    // Carte statique chargée via Coil comme une image normale
                    AsyncImage(
                        model = mapUrl,
                        contentDescription = "Carte de l'emplacement de l'événement",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 140.dp, height = 100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Gray)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}