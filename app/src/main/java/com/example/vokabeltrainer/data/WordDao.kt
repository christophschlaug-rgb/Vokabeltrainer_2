package com.example.vokabeltrainer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(words: List<Word>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(words: List<Word>): List<Long>

    @Update
    suspend fun update(word: Word)

    @Query("SELECT * FROM words WHERE id = :id LIMIT 1")
    suspend fun byId(id: String): Word?

    @Query("SELECT id FROM words")
    suspend fun allIds(): List<String>

    /** Anzahl aktiver (= nicht gelöschter) Wörter. */
    @Query("SELECT COUNT(*) FROM words WHERE deleted = 0")
    suspend fun count(): Int

    /** Wörter einer bestimmten Unit, nicht gelöscht. */
    @Query("SELECT * FROM words WHERE unitId = :unitId AND deleted = 0 ORDER BY en COLLATE NOCASE")
    fun forUnitFlow(unitId: String): Flow<List<Word>>

    @Query("SELECT * FROM words WHERE unitId = :unitId AND deleted = 0")
    suspend fun forUnit(unitId: String): List<Word>

    /** Alphabetische Suche, nicht gelöschte Wörter. */
    @Query("""
        SELECT * FROM words
        WHERE deleted = 0
          AND (:q = ''
               OR LOWER(en) LIKE '%' || LOWER(:q) || '%'
               OR LOWER(deList) LIKE '%' || LOWER(:q) || '%')
        ORDER BY 
          CASE WHEN LOWER(en) LIKE 'to %' THEN SUBSTR(LOWER(en), 4) ELSE LOWER(en) END
        ASC
    """)
    fun searchFlow(q: String): Flow<List<Word>>

    /** Soft-Delete: Wort wird als gelöscht markiert, nicht aus DB entfernt. */
    @Query("UPDATE words SET deleted = 1 WHERE id = :id")
    suspend fun softDelete(id: String)

    /** Wiederherstellen aus dem Papierkorb. */
    @Query("UPDATE words SET deleted = 0 WHERE id = :id")
    suspend fun restore(id: String)

    /** Liste aller gelöschten Wörter (für den Papierkorb-Screen). */
    @Query("SELECT * FROM words WHERE deleted = 1 ORDER BY en COLLATE NOCASE")
    fun deletedFlow(): kotlinx.coroutines.flow.Flow<List<Word>>

    /** Hard-Delete: für Unit-Vokabeln, die der Nutzer wirklich loswerden will. */
    @Query("DELETE FROM words WHERE id = :id")
    suspend fun hardDelete(id: String)

    /**
     * Standard-Eintrag finden, der die gleiche en+pos-Kombination hat wie eine Unit-Vokabel.
     * Wird genutzt, um eine Unit-Vokabel mit dem Standard-Eintrag zu verschmelzen
     * (Standard-Übersetzungen werden überschrieben, Lernfortschritt bleibt).
     */
    @Query("""
        SELECT * FROM words
        WHERE unitId IS NULL
          AND deleted = 0
          AND LOWER(en) = LOWER(:en)
          AND LOWER(pos) = LOWER(:pos)
        LIMIT 1
    """)
    suspend fun findStandardByEnAndPos(en: String, pos: String): Word?
}
