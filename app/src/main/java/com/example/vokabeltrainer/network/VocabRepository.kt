package com.example.vokabeltrainer.network

import android.content.Context
import com.example.vokabeltrainer.data.AppDatabase
import com.example.vokabeltrainer.data.LearningState
import com.example.vokabeltrainer.data.Word
import com.example.vokabeltrainer.srs.SrsEngine
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Zentrale Datenverwaltung: Asset-Bootstrap, optionaler Online-Refresh,
 * Validierung, Speicherung in Room.
 */
class VocabRepository(
    private val context: Context,
    private val db: AppDatabase
) {

    /**
     * Default-Quelle für Online-Refresh. Du kannst diese URL durch deine eigene
     * GitHub-raw-URL ersetzen, wenn du die Liste später extern pflegst.
     * Beim ersten Start ist sie aber unnötig: Die App lädt automatisch aus
     * dem mitgelieferten Asset (siehe [seedIfEmpty]).
     */
    private val sourceUrl: String =
        "https://raw.githubusercontent.com/USER/REPO/main/c1_vocab.json"

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val okHttp: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        // Kein Logging-Interceptor in Release: Keine sensiblen Daten in Logs.
        .build()

    private val api: VocabApi = Retrofit.Builder()
        // Basis-URL wird nicht verwendet (wir nutzen @Url), muss aber gesetzt sein.
        .baseUrl("https://example.invalid/")
        .client(okHttp)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(VocabApi::class.java)

    /**
     * Beim ersten Start: Vokabeln aus dem mitgelieferten Asset laden.
     * Damit ist die App vollständig offline nutzbar, ohne erst aus dem Netz laden zu müssen.
     */
    suspend fun seedIfEmpty() = withContext(Dispatchers.IO) {
        if (db.wordDao().count() > 0) return@withContext
        try {
            val raw = context.assets.open("c1_vocab.json")
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
            val dto = json.decodeFromString(VocabListDto.serializer(), raw)
            val valid = sanitizeAndFilter(dto.words)
            insertAll(valid)
        } catch (t: Throwable) {
            // Falls das Asset fehlt oder beschädigt ist: lieber keine Daten als Crash.
            // Der Nutzer kann manuell aus dem Netz nachladen (Home-Screen → Aktualisieren).
        }
    }

    /**
     * Download + Retry + Validation. Wirft bei endgültigem Fehler eine Exception.
     * @param url optionale abweichende URL (für Tests/Alternative)
     * @return Anzahl importierter/aktualisierter Wörter
     */
    suspend fun refreshFromNetwork(url: String = sourceUrl): Int = withContext(Dispatchers.IO) {
        require(url.startsWith("https://")) { "Nur HTTPS erlaubt." }
        var lastError: Throwable? = null
        repeat(3) { attempt ->
            try {
                val dto = api.fetch(url)
                val valid = sanitizeAndFilter(dto.words)
                insertAll(valid)
                return@withContext valid.size
            } catch (t: Throwable) {
                lastError = t
                // Exponentielles Backoff
                delay(500L * (attempt + 1) * (attempt + 1))
            }
        }
        throw lastError ?: RuntimeException("Unbekannter Netzwerkfehler")
    }

    /**
     * Validierung und Bereinigung: limitiert Längen, entfernt Steuerzeichen,
     * verwirft Einträge ohne ID/Englisch/Übersetzungen.
     */
    private fun sanitizeAndFilter(words: List<VocabEntryDto>): List<VocabEntryDto> =
        words
            .filter { it.id.isNotBlank() && it.en.isNotBlank() && it.de.isNotEmpty() }
            .map {
                VocabEntryDto(
                    id = sanitize(it.id).take(100),
                    en = sanitize(it.en).take(200),
                    de = it.de.map { d -> sanitize(d).take(200) }
                        .filter { d -> d.isNotBlank() },
                    pos = sanitize(it.pos).take(20),
                    level = sanitize(it.level).take(10)
                )
            }
            .filter { it.de.isNotEmpty() }

    /**
     * Strips control chars + trim. Schutz gegen eingeschleuste \n, \t, unicode-tricks.
     */
    private fun sanitize(s: String): String =
        s.trim().filter { c -> !c.isISOControl() }

    private suspend fun insertAll(entries: List<VocabEntryDto>) {
        val words = entries.map {
            Word(
                id = it.id,
                en = it.en,
                deList = it.de,
                pos = it.pos,
                level = it.level
            )
        }
        db.wordDao().upsertAll(words)
        // Für neue Wörter: Lernstatus mit heute als Fälligkeit erzeugen (neue Wörter sind "fällig").
        val today = SrsEngine.startOfTodayMillis()
        val states = words.map { w ->
            LearningState(
                wordId = w.id,
                level = 0,
                nextReviewDate = today
            )
        }
        db.learningDao().insertIfMissing(states)
    }
}
