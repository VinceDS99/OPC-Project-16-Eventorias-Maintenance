package com.openclassrooms.eventorias.ui.profile

/**
 * Représente les états possibles de l'écran profil utilisateur.
 */
sealed class ProfileUiState {

    /** Chargement en cours — récupération des données Firestore et Firebase Auth */
    object Loading : ProfileUiState()

    /** Erreur survenue — affiche un message et un bouton de retry */
    data class Error(val message: String) : ProfileUiState()

    /**
     * Données chargées avec succès.
     * Les informations d'identité viennent de Firebase Auth.
     * notificationsEnabled vient de Firestore (document users/{uid}).
     */
    data class Success(
        /** Nom complet de l'utilisateur (Firebase Auth displayName) */
        val displayName: String = "",
        /** Adresse email de l'utilisateur */
        val email: String = "",
        /** URL de la photo de profil (Storage ou Google selon le provider) */
        val photoUrl: String = "",
        /** Préférence de notification stockée dans Firestore */
        val notificationsEnabled: Boolean = false
    ) : ProfileUiState()
}