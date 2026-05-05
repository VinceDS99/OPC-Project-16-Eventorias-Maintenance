package com.openclassrooms.eventorias.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.openclassrooms.eventorias.MainActivity
import com.openclassrooms.eventorias.R

/**
 * Service FCM qui gère la réception des notifications push.
 * Doit être déclaré dans AndroidManifest.xml avec le filtre MESSAGING_EVENT.
 */
class EventoriasMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "eventorias_channel"
        const val CHANNEL_NAME = "Eventorias Events"
    }

    /**
     * Appelé par Firebase quand un nouveau token FCM est généré.
     * Cela arrive à la première installation ou quand Firebase invalide l'ancien token.
     * On sauvegarde immédiatement le nouveau token dans Firestore.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        TokenRepository.saveToken(token)
    }

    /**
     * Appelé quand une notification est reçue alors que l'app est en foreground.
     * En background, Android gère l'affichage automatiquement.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: "Eventorias"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        showNotification(title, body)
    }

    /**
     * Crée et affiche une notification système.
     * Le canal est créé si nécessaire (obligatoire sur Android 8+).
     * Un clic sur la notification ouvre MainActivity.
     */
    private fun showNotification(title: String, body: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crée le canal de notification (ignoré si déjà existant)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        // Intent qui ouvre l'app au clic sur la notification
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true) // Ferme la notification au clic
            .setContentIntent(pendingIntent)
            .build()

        // ID unique basé sur le timestamp pour ne pas écraser les notifications précédentes
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}