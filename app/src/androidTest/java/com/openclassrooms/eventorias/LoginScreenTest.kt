package com.openclassrooms.eventorias

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tests UI de l'écran de connexion.
 */
@HiltAndroidTest
class LoginScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        /**
         * Déconnecte l'utilisateur avant chaque test.
         */
        FirebaseAuth.getInstance().signOut()
    }

    /**
     * Vérifie que le bouton "Sign in with Google" est affiché sur l'écran de connexion.
     * waitUntil() attend que le composant apparaisse
     */
    @Test
    fun loginScreen_displaysGoogleButton() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("Sign in with Google")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule
            .onNodeWithText("Sign in with Google")
            .assertIsDisplayed()
    }

    /** Vérifie que le bouton "Sign in with email" est affiché */
    @Test
    fun loginScreen_displaysEmailButton() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("Sign in with email")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule
            .onNodeWithText("Sign in with email")
            .assertIsDisplayed()
    }
}