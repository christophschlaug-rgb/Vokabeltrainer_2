package com.example.vokabeltrainer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningStateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: LearningState)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(states: List<LearningState>)

    @Query("SELECT * FROM learning_states WHERE wordId = :id LIMIT 1")
    suspend fun byWord(id: String): LearningState?

    @Query("SELECT COUNT(*) FROM learning_states WHERE nextReviewDate <= :today")
    suspend fun dueCount(today: Long): Int

    @Query("SELECT COUNT(*) FROM learning_states")
    suspend fun total(): Int

    @Query("SELECT COUNT(*) FROM learning_states WHERE level = 5")
    suspend fun masteredCount(): Int

    @Query("SELECT AVG(level * 1.0) FROM learning_states")
    suspend fun averageLevel(): Double?

    /**
     * Liefert bis zu [limit] fällige Karten, älteste zuerst, dann niedrigster Level zuerst.
     */
    @Query("""
        SELECT * FROM learning_states
        WHERE nextReviewDate <= :today
        ORDER BY nextReviewDate ASC, level ASC
        LIMIT :limit
    """)
    suspend fun dueList(today: Long, limit: Int): List<LearningState>

    @Query("SELECT COUNT(*) FROM learning_states WHERE nextReviewDate <= :today")
    fun dueCountFlow(today: Long): Flow<Int>
}
