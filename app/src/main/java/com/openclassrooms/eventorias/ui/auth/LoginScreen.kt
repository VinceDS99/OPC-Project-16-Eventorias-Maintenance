package com.openclassrooms.eventorias.ui.auth

import com.openclassrooms.eventorias.R
import com.openclassrooms.eventorias.ui.theme.GoogleButton
import com.openclassrooms.eventorias.ui.theme.RedPrimary
import com.openclassrooms.eventorias.ui.theme.White
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract

/**
 * Écran de connexion de l'application.
 *
 * L'écran ne gère pas lui-même la logique d'auth — il délègue à AuthViewModel.
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    /**
     * Launcher FirebaseUI : enregistre un callback qui sera appelé
     * quand l'activité FirebaseUI se termine (succès, annulation ou erreur).
     * FirebaseAuthUIActivityResultContract gère la conversion du résultat.
     */
    val signInLauncher = rememberLauncherForActivityResult(
        contract = FirebaseAuthUIActivityResultContract()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Connexion réussie : sauvegarde le token FCM et navigue
            viewModel.onSignInSuccess()
            onLoginSuccess()
        }
        // Si annulé ou erreur : l'utilisateur reste sur cet écran
    }

    /**
     * Construit et lance l'intent FirebaseUI avec les providers donnés.
     * @param providers Liste des méthodes de connexion à proposer
     */
    fun launchSignIn(providers: List<AuthUI.IdpConfig>) {
        val intent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .build()
        signInLauncher.launch(intent)
    }

        // UI de l'écran : logo centré + deux boutons de connexion
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        // Logo Eventorias
        Image(
            painter = painterResource(id = R.drawable.logo_eventorias),
            contentDescription = "Logo Eventorias",
            modifier = Modifier.size(180.dp)
        )

        Spacer(modifier = Modifier.height(64.dp))

        // Bouton Google — lance le provider Google
        Button(
            onClick = {
                launchSignIn(listOf(AuthUI.IdpConfig.GoogleBuilder().build()))
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoogleButton)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = "Icône Google",
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Sign in with Google",
                color = Color.DarkGray,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bouton Email — lance le provider Email
        // setRequireName(true) : demande nom/prénom lors de la création de compte
        // setAllowNewAccounts(true) : permet la création de compte si email inconnu
        Button(
            onClick = {
                launchSignIn(
                    listOf(
                        AuthUI.IdpConfig.EmailBuilder()
                            .setRequireName(true)
                            .setAllowNewAccounts(true)
                            .build()
                    )
                )
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RedPrimary)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_email),
                contentDescription = "Icône email",
                tint = White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Sign in with email",
                color = White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}