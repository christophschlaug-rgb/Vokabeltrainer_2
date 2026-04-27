package com.example.vokabeltrainer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(words: List<Word>)

    @Query("SELECT * FROM words WHERE id = :id LIMIT 1")
    suspend fun byId(id: String): Word?

    @Query("SELECT COUNT(*) FROM words")
    suspend fun count(): Int

    /**
     * Alphabetisch sortiert nach Englisch (mit "to " vorne weggefiltert für saubere Sortierung).
     * Live-Suche: leerer Query -> alles.
     */
    @Query("""
        SELECT * FROM words
        WHERE (:q = '' 
               OR LOWER(en) LIKE '%' || LOWER(:q) || '%'
               OR LOWER(deList) LIKE '%' || LOWER(:q) || '%')
        ORDER BY 
          CASE WHEN LOWER(en) LIKE 'to %' THEN SUBSTR(LOWER(en), 4) ELSE LOWER(en) END
        ASC
    """)
    fun searchFlow(q: String): Flow<List<Word>>
}
