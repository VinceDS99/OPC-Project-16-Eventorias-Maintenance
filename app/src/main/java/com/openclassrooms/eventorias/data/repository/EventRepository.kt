package com.openclassrooms.eventorias.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.openclassrooms.eventorias.data.model.Event
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context
import android.location.Geocoder
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.Locale
import java.util.UUID
import android.location.Address
import android.os.Build

/**
 * Repository central pour les opérations sur les événements.
 * Suit le pattern Repository de l'architecture MVVM :
 * les ViewModels ne connaissent pas Firebase — ils passent par ce repository.
 */

@Singleton
class EventRepository @Inject constructor() {

    // Instance Firestore pour accéder à la base de données
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Retourne un Flow qui émet la liste des événements en temps réel.
     * Chaque modification dans Firestore déclenche une nouvelle émission.
     */

    fun getEvents(): Flow<List<Event>> = callbackFlow {
        // Ajoute un listener Firestore sur la collection "events"
        val listener = firestore.collection("events")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                // Convertit chaque document Firestore en objet Event
                // et assigne l'ID du document à la propriété id
                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Event::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                // Envoie la liste mise à jour dans le Flow
                trySend(events)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Crée un nouvel événement dans Firestore après avoir uploadé son image.
     * Utilise le pattern Result pour gérer succès et erreur sans exception.
     *
     * @param event Les données de l'événement à créer
     * @param imageUri URI locale de l'image sélectionnée par l'utilisateur
     * @param context Contexte Android nécessaire pour le Geocoder
     */

    suspend fun createEvent(event: Event, imageUri: Uri, context: Context): Result<Unit> {
        return try {
            // Étape 1 : Upload l'image dans Firebase Storage et récupère son URL publique
            val imageUrl = uploadImage(imageUri)

            // Étape 2 : Géocodage — convertit l'adresse texte en coordonnées GPS
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = mutableListOf<Address>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocationName(event.location, 1) { addresses.addAll(it) }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(event.location, 1)?.let { addresses.addAll(it) }
            }

            // Étape 3 : Sauvegarde dans Firestore avec l'URL de l'image
            val eventWithImage = event.copy(imageUrl = imageUrl)
            firestore.collection("events")
                .add(eventWithImage) // Firestore génère automatiquement un ID unique
                .await()            // await() suspend la coroutine jusqu'à la fin de l'opération

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload une image dans Firebase Storage sous un nom unique (UUID).
     * Retourne l'URL de téléchargement de l'image.
     */

    private suspend fun uploadImage(uri: Uri): String {
        val storage = FirebaseStorage.getInstance()
        // Crée une référence avec un nom unique
        val ref = storage.reference.child("events/${UUID.randomUUID()}.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }
}