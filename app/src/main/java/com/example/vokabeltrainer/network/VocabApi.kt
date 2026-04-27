package com.example.vokabeltrainer.network

import retrofit2.http.GET
import retrofit2.http.Url

interface VocabApi {
    /**
     * Lädt die komplette Vokabelliste von einer beliebigen (HTTPS-)URL.
     * Die URL wird in VocabRepository konfiguriert.
     */
    @GET
    suspend fun fetch(@Url url: String): VocabListDto
}
