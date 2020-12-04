package com.example.jetpackcomposechatapplication.notifications

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    val apiService: ApiService
        get() = Retrofit.Builder()
                .baseUrl(FirebaseMessagingService.FCM_BASE_URL)
                .client(provideClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)

    private fun provideClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder().addInterceptor(interceptor)
                .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                    val request = chain.request()
                    chain.proceed(request)
                }).build()
    }
}