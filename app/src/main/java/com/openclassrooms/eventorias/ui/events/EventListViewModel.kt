package com.openclassrooms.eventorias.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.eventorias.data.model.Event
import com.openclassrooms.eventorias.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Représente les 3 états possibles de l'écran liste.
 */

sealed class EventListUiState {

    /** Chargement en cours — affiche un spinner */
    object Loading : EventListUiState()

    /** Données reçues avec succès */
    data class Success(val events: List<Event>) : EventListUiState()

    /** Erreur survenue lors du chargement */
    data class Error(val message: String) : EventListUiState()
}

/**
 * ViewModel de l'écran de liste des événements.
 * Gère le chargement Firestore en temps réel et le filtrage par recherche.
 */

@HiltViewModel
class EventListViewModel @Inject constructor(
    private val repository: EventRepository
) : ViewModel() {

    /** Requête de recherche saisie par l'utilisateur */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    /** État courant de l'écran (Loading, Success, Error) */
    private val _uiState = MutableStateFlow<EventListUiState>(EventListUiState.Loading)
    val uiState: StateFlow<EventListUiState> = _uiState

    init {
        loadEvents()
    }

    /**
     * Lance l'écoute Firestore et combine avec la recherche pour filtrer en temps réel.
     */

    private fun loadEvents() {
        viewModelScope.launch {

            // Test écran d"erreur
            //_uiState.value = EventListUiState.Error("Simulation d'erreur")
            //return@launch

            combine(
                repository.getEvents(),
                _searchQuery
            ) { events, query ->
                if (query.isBlank()) events
                else events.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.category.contains(query, ignoreCase = true)
                }
            }
                .catch { e ->
                    _uiState.value = EventListUiState.Error(e.message ?: "Erreur inconnue")
                }
                .collect { filtered ->
                    _uiState.value = EventListUiState.Success(filtered)
                }
        }
    }

    /** Met à jour la requête de recherche (déclenche un nouveau filtrage) */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /** Réinitialise et relance le chargement après une erreur */
    fun retry() {
        _uiState.value = EventListUiState.Loading
        loadEvents()
    }
}