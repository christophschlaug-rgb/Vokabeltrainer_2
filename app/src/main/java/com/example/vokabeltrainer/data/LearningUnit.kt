package com.example.vokabeltrainer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Eine Lerneinheit (z.B. ein Kapitel aus einem Englischbuch).
 * Vokabeln, die zu einer Unit gehören, haben in [Word.unitId] diese ID.
 *
 * Eine Unit-Vokabel mit derselben en+pos-Kombination wie ein Standard-Eintrag
 * ersetzt diesen logisch: die Standard-Übersetzung wird durch die Unit-Übersetzung
 * überschrieben. Lernfortschritt bleibt erhalten.
 *
 * Bewusst NICHT "Unit" genannt, weil das Kotlins eingebauten Unit-Typ überschatten würde.
 */
@Entity(tableName = "units")
data class LearningUnit(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: Long
)
