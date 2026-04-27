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
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Zentrale Datenverwaltung: Asset-Bootstrap, optionaler Online-Refresh,
 * Validierung, Speicherung in Room.
 *
 * Wichtig: [refreshFromAsset] und [refreshFromNetwork] arbeiten INKREMENTELL.
 * - Bestehende Wörter werden NICHT überschrieben (Lernfortschritt bleibt).
 * - Soft-deleted Wörter werden NICHT wieder eingespielt.
 * - Wörter, deren Übersetzungen vom Nutzer angepasst wurden (customTranslations),
 *   werden ebenfalls nicht überschrieben.
 * - Nur wirklich neue IDs werden hinzugefügt.
 */
class VocabRepository(
    private val context: Context,
    private val db: AppDatabase
) {

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
        .build()

    private val api: VocabApi = Retrofit.Builder()
        .baseUrl("https://example.invalid/")
        .client(okHttp)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(VocabApi::class.java)

    /**
     * Beim allerersten Start (DB komplett leer): Vokabeln aus dem Asset laden.
     */
    suspend fun seedIfEmpty(): Unit = withContext(Dispatchers.IO) {
        if (db.wordDao().count() > 0) return@withContext
        refreshFromAsset()
        Unit
    }

    /**
     * Inkrementelles Nachladen aus dem mitgelieferten Asset.
     * Gibt die Anzahl der NEU hinzugefügten Wörter zurück.
     */
    suspend fun refreshFromAsset(): Int = withContext(Dispatchers.IO) {
        try {
            val raw = context.assets.open("c1_vocab.json")
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
            val dto = json.decodeFromString(VocabListDto.serializer(), raw)
            val valid = sanitizeAndFilter(dto.words)
            insertOnlyNew(valid)
        } catch (t: Throwable) {
            0
        }
    }

    /**
     * Online-Refresh, ebenfalls inkrementell.
     * @return Anzahl NEU hinzugefügter Wörter (nicht: Anzahl im Download)
     */
    suspend fun refreshFromNetwork(url: String = sourceUrl): Int = withContext(Dispatchers.IO) {
        require(url.startsWith("https://")) { "Nur HTTPS erlaubt." }
        var lastError: Throwable? = null
        repeat(3) { attempt ->
            try {
                val dto = api.fetch(url)
                val valid = sanitizeAndFilter(dto.words)
                return@withContext insertOnlyNew(valid)
            } catch (t: Throwable) {
                lastError = t
                delay(500L * (attempt + 1) * (attempt + 1))
            }
        }
        throw lastError ?: RuntimeException("Unbekannter Netzwerkfehler")
    }

    /**
     * Fügt nur Wörter ein, deren ID noch NICHT in der DB existiert (auch nicht als
     * soft-deleted oder mit customTranslations). Lernfortschritt bestehender Wörter
     * bleibt unangetastet.
     */
    private suspend fun insertOnlyNew(entries: List<VocabEntryDto>): Int {
        val existingIds = db.wordDao().allIds().toHashSet()
        val newEntries = entries.filter { it.id !in existingIds }
        if (newEntries.isEmpty()) return 0

        val words = newEntries.map {
            Word(
                id = it.id,
                en = it.en,
                deList = it.de,
                pos = it.pos,
                level = it.level,
                unitId = null,
                deleted = false,
                customTranslations = false
            )
        }
        db.wordDao().upsertAll(words)

        val today = SrsEngine.startOfTodayMillis()
        val states = words.map { w ->
            LearningState(wordId = w.id, level = 0, nextReviewDate = today)
        }
        db.learningDao().insertIfMissing(states)
        return words.size
    }

    /**
     * Fügt eine Vokabel zu einer Unit hinzu. Wenn ein Standard-Wörterbuch-Eintrag
     * mit gleichem Englisch+POS existiert, wird dessen Übersetzung durch die
     * Unit-Übersetzung überschrieben (Lernfortschritt bleibt erhalten).
     * Sonst wird ein neuer Word-Eintrag mit unitId angelegt.
     *
     * @return das resultierende [Word] (entweder neu oder aktualisiert)
     */
    suspend fun addUnitWord(
        unitId: String,
        en: String,
        de: List<String>,
        pos: String,
        level: String = "C1"
    ): Word = withContext(Dispatchers.IO) {
        val cleanedEn = sanitize(en).take(200)
        val cleanedDe = de.map { sanitize(it).take(200) }.filter { it.isNotBlank() }
        val cleanedPos = sanitize(pos).take(20)
        require(cleanedEn.isNotBlank()) { "Englisches Wort darf nicht leer sein." }
        require(cleanedDe.isNotEmpty()) { "Mindestens eine deutsche Übersetzung erforderlich." }

        val matchPos = if (cleanedPos.isBlank()) "" else cleanedPos
        val standardMatch = if (matchPos.isNotBlank()) {
            db.wordDao().findStandardByEnAndPos(cleanedEn, matchPos)
        } else null

        if (standardMatch != null) {
            // Standard-Eintrag aktualisieren: neue Übersetzungen, Unit-Zuordnung,
            // Marker setzen. Lernzustand und ID bleiben unverändert.
            val updated = standardMatch.copy(
                deList = cleanedDe,
                unitId = unitId,
                customTranslations = true
            )
            db.wordDao().update(updated)
            updated
        } else {
            // Neuer Eintrag mit eigener ID
            val newId = "unit_${unitId}_${makeIdFragment(cleanedEn)}_${UUID.randomUUID().toString().take(6)}"
            val word = Word(
                id = newId,
                en = cleanedEn,
                deList = cleanedDe,
                pos = cleanedPos,
                level = sanitize(level).take(10),
                unitId = unitId,
                deleted = false,
                customTranslations = false
            )
            db.wordDao().upsertAll(listOf(word))
            db.learningDao().insertIfMissing(
                listOf(
                    LearningState(
                        wordId = word.id,
                        level = 0,
                        nextReviewDate = SrsEngine.startOfTodayMillis()
                    )
                )
            )
            word
        }
    }

    /**
     * Markiert ein Wort als gelöscht (Soft-Delete). Es wird nicht mehr abgefragt.
     * Beim "Wortschatz aktualisieren" wird es nicht wiederbelebt.
     * Lernzustand bleibt erhalten (kann durch erneutes Aktivieren reaktiviert werden,
     * was wir aber nicht explizit als Feature anbieten).
     */
    suspend fun deleteWord(wordId: String) = withContext(Dispatchers.IO) {
        val word = db.wordDao().byId(wordId) ?: return@withContext
        if (word.unitId != null && !word.customTranslations) {
            // Reine Unit-Vokabel: hart löschen, Lernzustand auch weg
            db.wordDao().hardDelete(wordId)
            db.learningDao().deleteByWord(wordId)
        } else {
            // Standard-Vokabel oder Standard mit überschriebenen Translations: soft
            db.wordDao().softDelete(wordId)
        }
    }

    /** Wort aus dem Papierkorb zurückholen. */
    suspend fun restoreWord(wordId: String) = withContext(Dispatchers.IO) {
        db.wordDao().restore(wordId)
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
                    de = it.de.map { d -> sanitize(d).take(200) }.filter { d -> d.isNotBlank() },
                    pos = sanitize(it.pos).take(20),
                    level = sanitize(it.level).take(10)
                )
            }
            .filter { it.de.isNotEmpty() }

    private fun sanitize(s: String): String = s.trim().filter { c -> !c.isISOControl() }

    private fun makeIdFragment(en: String): String =
        en.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_').take(40)
}
