package com.example.vokabeltrainer.data

import androidx.room.TypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * Konvertiert Listen aus Strings zu JSON-Strings (Room speichert nur primitive Typen).
 */
class Converters {
    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(String.serializer())

    @TypeConverter
    fun fromList(list: List<String>?): String =
        if (list == null) "[]" else json.encodeToString(serializer, list)

    @TypeConverter
    fun toList(data: String?): List<String> =
        if (data.isNullOrEmpty()) emptyList() else json.decodeFromString(serializer, data)
}
