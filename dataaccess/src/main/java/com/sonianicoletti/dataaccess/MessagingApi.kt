package com.sonianicoletti.dataaccess

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface MessagingApi {

    @POST("fcm/send")
    suspend fun send(
        @Header("Authorization") authHeader: String,
        @Body body: MessageRequest
    ): Any
}
