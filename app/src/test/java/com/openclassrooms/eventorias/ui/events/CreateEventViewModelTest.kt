package com.openclassrooms.eventorias.ui.events

import android.net.Uri
import com.openclassrooms.eventorias.data.repository.EventRepository
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests unitaires pour CreateEventViewModel.
 * Valide la logique de validation du formulaire sans appels Firebase.
 * Les tests vérifient que les messages d'erreur corrects sont retournés
 * selon les champs manquants.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CreateEventViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: EventRepository
    private lateinit var viewModel: CreateEventViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = CreateEventViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Quand tous les champs texte sont remplis mais qu'aucune image n'est sélectionnée,
     * le ViewModel doit retourner une erreur spécifique à l'image.
     */
    @Test
    fun `createEvent shows error when image is missing`() {
        viewModel.title.value = "Mon événement"
        viewModel.address.value = "Paris"
        viewModel.date.value = "01/01/2026"
        viewModel.time.value = "10:00 AM"

        viewModel.createEvent(mockk())

        val state = viewModel.uiState.value
        assertTrue(state is CreateEventUiState.Error)
        assertEquals(
            "Veuillez sélectionner une image",
            (state as CreateEventUiState.Error).message
        )
    }

    /** Le titre vide doit déclencher une erreur "champs obligatoires" */
    @Test
    fun `createEvent shows error when title is blank`() {
        viewModel.selectedImageUri.value = mockk<Uri>()
        viewModel.title.value = ""
        viewModel.address.value = "Paris"
        viewModel.date.value = "01/01/2026"
        viewModel.time.value = "10:00 AM"

        viewModel.createEvent(mockk())

        val state = viewModel.uiState.value
        assertTrue(state is CreateEventUiState.Error)
        assertEquals(
            "Tous les champs sont obligatoires",
            (state as CreateEventUiState.Error).message
        )
    }

    /** La date vide doit déclencher une erreur */
    @Test
    fun `createEvent shows error when date is blank`() {
        viewModel.selectedImageUri.value = mockk<Uri>()
        viewModel.title.value = "Mon événement"
        viewModel.address.value = "Paris"
        viewModel.date.value = ""
        viewModel.time.value = "10:00 AM"

        viewModel.createEvent(mockk())

        assertTrue(viewModel.uiState.value is CreateEventUiState.Error)
    }

    /** L'heure vide doit déclencher une erreur */
    @Test
    fun `createEvent shows error when time is blank`() {
        viewModel.selectedImageUri.value = mockk<Uri>()
        viewModel.title.value = "Mon événement"
        viewModel.address.value = "Paris"
        viewModel.date.value = "01/01/2026"
        viewModel.time.value = ""

        viewModel.createEvent(mockk())

        assertTrue(viewModel.uiState.value is CreateEventUiState.Error)
    }


    /** Vérification que l'URI est bien stockée après sélection d'image */
    @Test
    fun `onImageSelected updates selectedImageUri`() {
        val uri = mockk<Uri>()
        viewModel.onImageSelected(uri)
        assertEquals(uri, viewModel.selectedImageUri.value)
    }
}