package com.openclassrooms.eventorias.ui.events

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.eventorias.data.model.Event
import com.openclassrooms.eventorias.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * États possibles de l'écran de création d'événement.
 * Idle : formulaire vide, prêt à être rempli
 * Loading : création en cours (upload image + écriture Firestore)
 * Success : événement créé, navigation vers la liste
 * Error : erreur avec message descriptif
 */
sealed class CreateEventUiState {
    object Idle : CreateEventUiState()
    object Loading : CreateEventUiState()
    object Success : CreateEventUiState()
    data class Error(val message: String) : CreateEventUiState()
}

/**
 * ViewModel de l'écran de création d'événement.
 * Expose les champs du formulaire comme des StateFlow individuels
 * pour que chaque champ soit indépendant et réactif.
 *
 * La validation est faite dans createEvent() avant l'appel au repository.
 */
@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val repository: EventRepository
) : ViewModel() {

    /** État global de l'écran (idle par défaut) */
    private val _uiState = MutableStateFlow<CreateEventUiState>(CreateEventUiState.Idle)
    val uiState: StateFlow<CreateEventUiState> = _uiState

    // Champs du formulaire exposés comme MutableStateFlow
    // Le composable les observe et les modifie directement
    val title = MutableStateFlow("")
    val description = MutableStateFlow("")
    val date = MutableStateFlow("")
    val time = MutableStateFlow("")
    val address = MutableStateFlow("")

    /** URI de l'image sélectionnée (caméra ou galerie), null si non sélectionnée */
    val selectedImageUri = MutableStateFlow<Uri?>(null)

    /**
     * Appelé quand l'utilisateur sélectionne une image (galerie ou caméra).
     * Met à jour l'URI qui sera utilisée lors de la création.
     */
    fun onImageSelected(uri: Uri) {
        selectedImageUri.value = uri
    }

    /**
     * Valide le formulaire et crée l'événement si tout est correct.
     * Ordre des opérations :
     * 1. Validation locale des champs
     * 2. Upload de l'image dans Firebase Storage
     * 3. Géocodage de l'adresse
     * 4. Écriture dans Firestore
     *
     * @param context Nécessaire pour le Geocoder
     */
    fun createEvent(context: Context) {
        val imageUri = selectedImageUri.value

        // Validation : image obligatoire
        if (imageUri == null) {
            _uiState.value = CreateEventUiState.Error("Veuillez sélectionner une image")
            return
        }

        // Validation : tous les champs texte obligatoires
        if (title.value.isBlank() || address.value.isBlank() ||
            date.value.isBlank() || time.value.isBlank()
        ) {
            _uiState.value = CreateEventUiState.Error("Tous les champs sont obligatoires")
            return
        }

        viewModelScope.launch {
            _uiState.value = CreateEventUiState.Loading

            // Construit l'objet Event sans imageUrl (sera ajouté par le repository)
            val event = Event(
                title = title.value,
                description = description.value,
                date = date.value,
                time = time.value,
                location = address.value
            )

            // Délègue la création au repository et gère le résultat
            val result = repository.createEvent(event, imageUri, context)
            _uiState.value = if (result.isSuccess) {
                CreateEventUiState.Success
            } else {
                CreateEventUiState.Error(
                    result.exceptionOrNull()?.message ?: "Erreur inconnue"
                )
            }
        }
    }
}