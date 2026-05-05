package com.openclassrooms.eventorias.data.model

/**
 * Modèle de données représentant un événement.
 * Toutes les propriétés ont des valeurs par défaut vides car Firestore
 * utilise la réflexion Java pour désérialiser les documents en objets Kotlin.
 * Sans valeurs par défaut, la désérialisation échouerait.
 */

data class Event(
    /** Identifiant unique du document Firestore (assigné lors de la récupération) */
    val id: String = "",

    /** Titre de l'événement */
    val title: String = "",

    /** Date de l'événement au format string (ex: "June 15, 2024") */
    val date: String = "",

    /** Description de l'événement */
    val description: String = "",

    /** URL de l'image stockée dans Firebase Storage */
    val imageUrl: String = "",

    /** Heure de l'événement au format string (ex: "10:00 AM") */
    val time: String = "",

    /** URL de la photo de l'auteur de l'événement */
    val authorPhotoUrl: String = "",

    /** Catégorie utilisée pour le filtrage (ex: "Music", "Art") */
    val category: String = "",

    /** Adresse complète utilisée pour l'affichage Maps */
    val location: String = ""
)