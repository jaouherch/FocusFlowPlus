package com.example.focusflowplus.data.remote

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ClaudeApiService {

    @Headers(
        "content-type: application/json"
    )
    @POST("v1/messages")
    suspend fun sendMessage(
        @Body request: ClaudeRequest
    ): ClaudeResponse
}