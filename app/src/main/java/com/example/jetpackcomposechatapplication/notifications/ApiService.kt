package com.example.jetpackcomposechatapplication.notifications

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @Headers(
            "Authorization: key=" + FirebaseMessagingService.FCM_SERVER_KEY,
            "Content-Type: application/json"
    )
    @POST("fcm/send")
    fun sendNotification(@Body payload: JsonObject?): Call<JsonObject?>?
}