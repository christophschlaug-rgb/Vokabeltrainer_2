package com.example.vokabeltrainer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Aggregat: wieviele Lernzustände pro SRS-Level. */
data class LevelCount(val level: Int, val count: Int)

@Dao
interface LearningStateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: LearningState)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(states: List<LearningState>)

    @Query("SELECT * FROM learning_states WHERE wordId = :id LIMIT 1")
    suspend fun byWord(id: String): LearningState?

    /**
     * Anzahl heute fälliger, NICHT gelöschter Karten.
     */
    @Query("""
        SELECT COUNT(*) FROM learning_states ls
        INNER JOIN words w ON w.id = ls.wordId
        WHERE ls.nextReviewDate <= :today AND w.deleted = 0
    """)
    suspend fun dueCount(today: Long): Int

    /**
     * Heute fällige Karten optional eingeschränkt auf eine Unit (oder alle = null).
     */
    @Query("""
        SELECT COUNT(*) FROM learning_states ls
        INNER JOIN words w ON w.id = ls.wordId
        WHERE ls.nextReviewDate <= :today
          AND w.deleted = 0
          AND (:unitId IS NULL OR w.unitId = :unitId)
    """)
    suspend fun dueCountFiltered(today: Long, unitId: String?): Int

    @Query("SELECT COUNT(*) FROM learning_states ls INNER JOIN words w ON w.id = ls.wordId WHERE w.deleted = 0")
    suspend fun total(): Int

    @Query("SELECT COUNT(*) FROM learning_states ls INNER JOIN words w ON w.id = ls.wordId WHERE ls.level = 5 AND w.deleted = 0")
    suspend fun masteredCount(): Int

    @Query("SELECT AVG(ls.level * 1.0) FROM learning_states ls INNER JOIN words w ON w.id = ls.wordId WHERE w.deleted = 0")
    suspend fun averageLevel(): Double?

    /**
     * Liefert bis zu [limit] fällige, NICHT gelöschte Karten.
     * Wenn unitId != null: nur Karten dieser Unit.
     */
    @Query("""
        SELECT ls.* FROM learning_states ls
        INNER JOIN words w ON w.id = ls.wordId
        WHERE ls.nextReviewDate <= :today
          AND w.deleted = 0
          AND (:unitId IS NULL OR w.unitId = :unitId)
        ORDER BY ls.nextReviewDate ASC, ls.level ASC
        LIMIT :limit
    """)
    suspend fun dueListFiltered(today: Long, limit: Int, unitId: String?): List<LearningState>

    @Query("""
        SELECT COUNT(*) FROM learning_states ls
        INNER JOIN words w ON w.id = ls.wordId
        WHERE ls.nextReviewDate <= :today AND w.deleted = 0
    """)
    fun dueCountFlow(today: Long): Flow<Int>

    /**
     * Verteilung aller AKTIVEN Lernzustände nach SRS-Level (0..5).
     */
    @Query("""
        SELECT ls.level AS level, COUNT(*) AS count
        FROM learning_states ls
        INNER JOIN words w ON w.id = ls.wordId
        WHERE w.deleted = 0
        GROUP BY ls.level ORDER BY ls.level
    """)
    suspend fun countByLevel(): List<LevelCount>

    /** Lernzustand zu einem Wort entfernen (z.B. wenn Unit-Wort hard-deleted wird). */
    @Query("DELETE FROM learning_states WHERE wordId = :wordId")
    suspend fun deleteByWord(wordId: String)
}
