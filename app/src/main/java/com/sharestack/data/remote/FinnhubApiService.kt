package com.sharestack.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface FinnhubApiService {
    @GET("quote")
    suspend fun getQuote(
        @Query("symbol") symbol: String,
        @Query("token") token: String
    ): FinnhubQuoteResponse
}