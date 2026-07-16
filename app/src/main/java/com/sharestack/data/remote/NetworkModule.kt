package com.sharestack.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://finnhub.io/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: FinnhubApiService by lazy {
        retrofit.create(FinnhubApiService::class.java)
    }
}