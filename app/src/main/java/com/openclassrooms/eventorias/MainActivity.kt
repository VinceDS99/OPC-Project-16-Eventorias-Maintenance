package com.openclassrooms.eventorias

import com.openclassrooms.eventorias.ui.auth.LoginScreen
import com.openclassrooms.eventorias.ui.theme.EventoriasTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclassrooms.eventorias.ui.auth.AuthViewModel
import com.openclassrooms.eventorias.ui.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.getValue

/**
 * Activité principale de l'application
 * Elle observe l'état de connexion et redirige vers le bon écran :
 * - Non connecté → LoginScreen
 * - Connecté → AppNavigation
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_Eventorias)

        enableEdgeToEdge()

        setContent {
            EventoriasTheme {
                // Récupère le ViewModel qui gère l'état de connexion
                val authViewModel: AuthViewModel = hiltViewModel()

                // Observe isLoggedIn : toute modification déclenche une recomposition
                val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

                if (isLoggedIn) {
                    // Utilisateur connecté : affiche la navigation principale
                    AppNavigation(
                        onSignOut = { authViewModel.onSignOut() }
                    )
                } else {
                    // Utilisateur non connecté : affiche l'écran de login
                    LoginScreen(
                        onLoginSuccess = { authViewModel.onSignInSuccess() }
                    )
                }
            }
        }
    }
}