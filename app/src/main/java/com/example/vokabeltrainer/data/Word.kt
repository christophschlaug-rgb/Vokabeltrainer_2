package com.example.vokabeltrainer.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Eine Vokabel. Enthält Englisch, eine Liste deutscher Übersetzungen, POS und Niveau.
 * deList wird über [Converters] als JSON-String gespeichert.
 *
 * Erweiterung: Vokabeln können einer Unit zugeordnet sein (unitId != null).
 * Standard-Wörterbuch-Einträge haben unitId = null.
 *
 * Soft-Delete via [deleted]: gelöschte Wörter bleiben in der DB, werden aber
 * nicht mehr abgefragt. Das ist Voraussetzung dafür, dass beim "Wortschatz
 * aktualisieren" gelöschte Wörter nicht neu hinzugefügt werden.
 *
 * [customTranslations] markiert Standard-Einträge, deren Übersetzungen vom
 * Nutzer (über eine Unit-Vokabel mit derselben ID) überschrieben wurden.
 * Beim "Wortschatz aktualisieren" wird die deList solcher Einträge nicht
 * überschrieben.
 */
@Entity(tableName = "words")
data class Word(
    @PrimaryKey val id: String,
    val en: String,
    val deList: List<String>,
    val pos: String,
    val level: String,
    @ColumnInfo(defaultValue = "NULL") val unitId: String? = null,
    @ColumnInfo(defaultValue = "0") val deleted: Boolean = false,
    @ColumnInfo(defaultValue = "0") val customTranslations: Boolean = false
)
