package com.example.vokabeltrainer.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Lernstatus pro Vokabel. 1:1-Beziehung zu [Word] per wordId.
 * level 0..5, nextReviewDate als Epoch-Millis zum Tagesanfang (UTC-lokal).
 */
@Entity(
    tableName = "learning_states",
    foreignKeys = [
        ForeignKey(
            entity = Word::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("nextReviewDate")]
)
data class LearningState(
    @PrimaryKey val wordId: String,
    val level: Int = 0,
    val nextReviewDate: Long = 0L,
    val lastResult: String? = null,
    val lastReviewedAt: Long? = null,
    val timesSeen: Int = 0,
    val timesCorrect: Int = 0
)
