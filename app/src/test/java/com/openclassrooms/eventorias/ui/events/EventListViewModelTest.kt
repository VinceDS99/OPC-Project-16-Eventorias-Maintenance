package com.openclassrooms.eventorias.ui.events

import com.openclassrooms.eventorias.data.model.Event
import com.openclassrooms.eventorias.data.repository.EventRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests unitaires pour EventListViewModel.
 * Utilise MockK pour simuler le repository sans appels Firebase réels.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EventListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    /** Repository simulé, aucune connexion Firebase réelle */
    private lateinit var repository: EventRepository
    private lateinit var viewModel: EventListViewModel

    /** Données de test représentant 3 événements avec des catégories différentes */
    private val fakeEvents = listOf(
        Event(id = "1", title = "Music festival", category = "Music", date = "April 01, 2026"),
        Event(id = "2", title = "Art exhibition", category = "Art", date = "July 20, 2024"),
        Event(id = "3", title = "Tech conference", category = "Tech", date = "August 5, 2024")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        every { repository.getEvents() } returns flowOf(fakeEvents)
        viewModel = EventListViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Vérifie que le ViewModel passe bien en état Success après réception des données.
     */
    @Test
    fun `initial state is Loading`() {
        every { repository.getEvents() } returns flowOf(fakeEvents)
        val freshViewModel = EventListViewModel(repository)
        assertTrue(freshViewModel.uiState.value is EventListUiState.Success)
    }

    /** Vérifie que les 3 événements de test sont bien reçus */
    @Test
    fun `events are loaded successfully`() {
        val state = viewModel.uiState.value
        assertTrue(state is EventListUiState.Success)
        assertEquals(3, (state as EventListUiState.Success).events.size)
    }

    /** Vérifie que la recherche par titre (insensible à la casse) fonctionne */
    @Test
    fun `search filters events by title`() = runTest {
        viewModel.onSearchQueryChange("music")
        val state = viewModel.uiState.value as EventListUiState.Success
        assertEquals(1, state.events.size)
        assertEquals("Music festival", state.events[0].title)
    }

    /** Vérifie que la recherche par catégorie fonctionne */
    @Test
    fun `search filters events by category`() = runTest {
        viewModel.onSearchQueryChange("Tech")
        val state = viewModel.uiState.value as EventListUiState.Success
        assertEquals(1, state.events.size)
        assertEquals("Tech conference", state.events[0].title)
    }

    /** Vérifie que vider la recherche retourne tous les événements */
    @Test
    fun `empty search returns all events`() = runTest {
        viewModel.onSearchQueryChange("music")
        viewModel.onSearchQueryChange("")
        val state = viewModel.uiState.value as EventListUiState.Success
        assertEquals(3, state.events.size)
    }

    /** Vérifie que le ViewModel passe en état Error quand le repository lance une exception. */
    @Test
    fun `error state when repository throws`() = runTest {
        every { repository.getEvents() } returns flow {
            throw RuntimeException("Firestore error")
        }
        val errorViewModel = EventListViewModel(repository)
        assertTrue(errorViewModel.uiState.value is EventListUiState.Error)
    }
}