package com.openclassrooms.eventorias

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Point d'entrée de l'application.
 * L'annotation @HiltAndroidApp déclenche la génération du code Hilt
 */

@HiltAndroidApp
class EventoriasApplication : Application()