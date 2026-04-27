package com.example.vokabeltrainer.network

import kotlinx.serialization.Serializable

/** DTO für den gesamten Download. */
@Serializable
data class VocabListDto(
    val version: String = "",
    val license: String = "",
    val words: List<VocabEntryDto> = emptyList()
)

/** DTO für einen Vokabeleintrag. */
@Serializable
data class VocabEntryDto(
    val id: String,
    val en: String,
    val de: List<String>,
    val pos: String = "",
    val level: String = "C1"
)
