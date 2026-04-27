package com.example.vokabeltrainer

import android.app.Application
import com.example.vokabeltrainer.data.AppDatabase
import com.example.vokabeltrainer.network.VocabRepository

/**
 * Application-Klasse.
 * Einstiegspunkt der App. Initialisiert DB und Repository einmal beim App-Start.
 */
class VokabelApp : Application() {
    val db: AppDatabase by lazy { AppDatabase.create(this) }
    val repository: VocabRepository by lazy { VocabRepository(this, db) }
}
