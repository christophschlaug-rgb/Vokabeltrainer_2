package com.example.vokabeltrainer.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Aggregat: Unit + Anzahl zugeordneter Vokabeln. */
data class UnitWithCount(
    val id: String,
    val name: String,
    val createdAt: Long,
    val wordCount: Int
)

@Dao
interface UnitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(unit: LearningUnit)

    @Delete
    suspend fun delete(unit: LearningUnit)

    @Query("SELECT * FROM units WHERE id = :id LIMIT 1")
    suspend fun byId(id: String): LearningUnit?

    @Query("SELECT * FROM units ORDER BY createdAt DESC")
    fun listFlow(): Flow<List<LearningUnit>>

    @Query("""
        SELECT u.id AS id, u.name AS name, u.createdAt AS createdAt,
               (SELECT COUNT(*) FROM words w WHERE w.unitId = u.id AND w.deleted = 0) AS wordCount
        FROM units u
        ORDER BY u.createdAt DESC
    """)
    fun listWithCountFlow(): Flow<List<UnitWithCount>>

    @Query("DELETE FROM units WHERE id = :unitId")
    suspend fun deleteById(unitId: String)
}
