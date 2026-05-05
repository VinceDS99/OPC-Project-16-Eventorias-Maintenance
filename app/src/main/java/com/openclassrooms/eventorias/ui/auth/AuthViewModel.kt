package com.openclassrooms.eventorias.ui.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.openclassrooms.eventorias.notification.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * ViewModel gérant l'état d'authentification de l'utilisateur.
 * Utilise un AuthStateListener Firebase pour réagir automatiquement
 * aux changements d'état (connexion/déconnexion).
 */

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    /**
     * État de connexion exposé en lecture seule aux composables.
     * MutableStateFlow est modifiable en interne, StateFlow est en lecture seule à l'extérieur.
     * Initialisé selon l'état actuel de Firebase Auth.
     */
    private val _isLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    /**
     * Listener Firebase qui réagit à tout changement d'état d'authentification.
     * Permet à MainActivity de se mettre à jour automatiquement
     */
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _isLoggedIn.value = firebaseAuth.currentUser != null
    }

    init {
        // Enregistre le listener dès la création du ViewModel
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

    /**
     * Appelé après une connexion réussie via FirebaseUI.
     * Sauvegarde le token FCM pour permettre l'envoi de notifications ciblées.
     */
    fun onSignInSuccess() {
        TokenRepository.refreshAndSaveToken()
    }

    /**
     * Déconnecte l'utilisateur de Firebase.
     * Le AuthStateListener détecte automatiquement le changement et met à jour _isLoggedIn.
     */
    fun onSignOut() {
        auth.signOut()
    }
}