package com.openclassrooms.eventorias.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.openclassrooms.eventorias.notification.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * ViewModel de l'écran profil utilisateur.
 * Gère :
 * - Le chargement des informations utilisateur (Firebase Auth + Firestore)
 * - La mise à jour de la photo de profil (Storage + Auth)
 * - Le toggle des notifications (Firestore + FCM token)
 */

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    /** Indique si un upload de photo est en cours (affiche un spinner sur l'avatar) */
    private val _isUploadingPhoto = MutableStateFlow(false)
    val isUploadingPhoto: StateFlow<Boolean> = _isUploadingPhoto

    init {
        loadProfile()
    }

    /**
     * Charge le profil utilisateur en combinant :
     * - Les données depuis Firebase Auth (nom, email, photo)
     * - Les préférences depuis Firestore (notifications activées ou non)
     */
    private fun loadProfile() {

        val user = auth.currentUser
        if (user == null) {
            _uiState.value = ProfileUiState.Error("Utilisateur non connecté")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = ProfileUiState.Loading

                // Récupère le document utilisateur dans Firestore
                val doc = firestore.collection("users")
                    .document(user.uid)
                    .get()
                    .await()

                val notificationsEnabled = doc.getBoolean("notificationsEnabled") ?: false

                _uiState.value = ProfileUiState.Success(
                    displayName = user.displayName ?: "Nom inconnu",
                    email = user.email ?: "Email inconnu",
                    photoUrl = user.photoUrl?.toString() ?: "",
                    notificationsEnabled = notificationsEnabled
                )
            } catch (e: Exception) {
                // Si Firestore échoue (ex: hors ligne), on affiche quand même
                // les données Auth sans les préférences
                val user2 = auth.currentUser
                _uiState.value = ProfileUiState.Success(
                    displayName = user2?.displayName ?: "Nom inconnu",
                    email = user2?.email ?: "Email inconnu",
                    photoUrl = user2?.photoUrl?.toString() ?: "",
                    notificationsEnabled = false
                )
            }
        }
    }

    /**
     * Upload la nouvelle photo de profil dans Firebase Storage
     * puis met à jour le profil Firebase Auth avec la nouvelle URL.
     */
    fun onPhotoSelected(uri: Uri) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                _isUploadingPhoto.value = true

                // Référence fixe par utilisateur pour écraser l'ancienne photo
                val ref = storage.reference.child("profiles/${user.uid}/avatar.jpg")
                ref.putFile(uri).await()
                val downloadUrl = ref.downloadUrl.await().toString()

                // Met à jour le profil Firebase Auth pour persister l'URL
                val profileUpdates = userProfileChangeRequest {
                    photoUri = Uri.parse(downloadUrl)
                }
                user.updateProfile(profileUpdates).await()

                // Met à jour l'état local immédiatement (pas besoin de recharger)
                val current = _uiState.value as? ProfileUiState.Success ?: return@launch
                _uiState.value = current.copy(photoUrl = downloadUrl)

            } catch (e: Exception) {
                // La photo actuelle reste affichée en cas d'erreur
            } finally {
                _isUploadingPhoto.value = false
            }
        }
    }

    /**
     * Active ou désactive les notifications pour cet utilisateur.
     * - Activation : sauvegarde le token FCM dans Firestore
     * - Désactivation : supprime le token pour arrêter les notifications ciblées
     */
    fun onNotificationsToggle(enabled: Boolean) {
        val current = _uiState.value as? ProfileUiState.Success ?: return
        // Mise à jour locale immédiate pour une UI réactive
        _uiState.value = current.copy(notificationsEnabled = enabled)

        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                if (enabled) {
                    TokenRepository.refreshAndSaveToken()
                    firestore.collection("users").document(uid)
                        .update("notificationsEnabled", true).await()
                } else {
                    // Supprime le token FCM pour ne plus recevoir de notifications
                    firestore.collection("users").document(uid)
                        .update(mapOf(
                            "notificationsEnabled" to false,
                            "fcmToken" to null
                        )).await()
                }
            } catch (e: Exception) {
                // L'état local est déjà mis à jour
            }
        }
    }

    fun retry() { loadProfile() }
}