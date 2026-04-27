package com.example.vokabeltrainer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Eine Vokabel. Enthält Englisch, eine Liste deutscher Übersetzungen, POS und Niveau.
 * deList wird über [Converters] als JSON-String gespeichert.
 */
@Entity(tableName = "words")
data class Word(
    @PrimaryKey val id: String,
    val en: String,
    val deList: List<String>,
    val pos: String,
    val level: String
)
