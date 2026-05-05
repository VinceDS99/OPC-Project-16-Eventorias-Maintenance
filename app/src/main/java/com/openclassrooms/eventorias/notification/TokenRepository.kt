package com.openclassrooms.eventorias.notification

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Repository responsable de la gestion du token FCM (Firebase Cloud Messaging).
 * Le token identifie de manière unique l'appareil de l'utilisateur.
 * Il est stocké dans Firestore pour permettre l'envoi de notifications ciblées.
 */
object TokenRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Récupère le token FCM actuel auprès de Firebase et le sauvegarde.
     * Appelé à chaque connexion pour s'assurer que le token est à jour.
     */
    fun refreshAndSaveToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            saveToken(token)
        }
    }

    /**
     * Sauvegarde le token FCM dans Firestore sous le document de l'utilisateur.
     * Si le document n'existe pas encore, il est créé.
     */
    fun saveToken(token: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(uid)
            .update("fcmToken", token)
            .addOnFailureListener {
                // update() échoue si le document n'existe pas encore
                // Dans ce cas on utilise set() pour créer le document
                firestore.collection("users")
                    .document(uid)
                    .set(mapOf("fcmToken" to token))
            }
    }
}