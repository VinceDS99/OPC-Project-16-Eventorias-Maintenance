package com.openclassrooms.eventorias

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.openclassrooms.eventorias.ui.events.ErrorState
import com.openclassrooms.eventorias.ui.events.EventItem
import com.openclassrooms.eventorias.data.model.Event
import com.openclassrooms.eventorias.ui.theme.EventoriasTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

/**
 * Tests UI des composants de l'écran liste des événements.
 */
@HiltAndroidTest
class EventListScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    /**
     * Vérifie qu'un EventItem affiche correctement le titre et la date de l'événement.
     */
    @Test
    fun eventItem_displaysTitleAndDate() {
        val event = Event(
            id = "1",
            title = "Music festival",
            date = "April 01, 2026"
        )
        composeTestRule.setContent {
            EventoriasTheme {
                EventItem(event = event)
            }
        }
        composeTestRule.onNodeWithText("Music festival").assertIsDisplayed()
        composeTestRule.onNodeWithText("April 01, 2026").assertIsDisplayed()
    }

    /**
     * Vérifie que le callback onClick est bien appelé quand on clique sur un EventItem.
     */
    @Test
    fun eventItem_isClickable() {
        var clicked = false
        val event = Event(id = "1", title = "Music festival", date = "April 01, 2026")

        composeTestRule.setContent {
            EventoriasTheme {
                EventItem(event = event, onClick = { clicked = true })
            }
        }
        composeTestRule.onNodeWithText("Music festival").performClick()
        assert(clicked)
    }

    /**
     * Vérifie que l'état d'erreur affiche bien "Error" et le bouton "Try again".
     */
    @Test
    fun errorState_displaysErrorMessage() {
        composeTestRule.setContent {
            EventoriasTheme {
                ErrorState(onRetry = {})
            }
        }
        composeTestRule.onNodeWithText("Error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Try again").assertIsDisplayed()
    }

    /**
     * Vérifie que le bouton "Try again" appelle bien le callback onRetry au clic.
     */
    @Test
    fun errorState_retryButtonIsClickable() {
        var retried = false
        composeTestRule.setContent {
            EventoriasTheme {
                ErrorState(onRetry = { retried = true })
            }
        }
        composeTestRule.onNodeWithText("Try again").performClick()
        assert(retried)
    }
}