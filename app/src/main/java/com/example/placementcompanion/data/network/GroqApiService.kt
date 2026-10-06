package com.example.placementcompanion.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GroqApiService {
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: GroqRequest
    ): Response<GroqChatResponse>
}
