package com.sharestack.data.remote

data class FinnhubQuoteResponse(
    val c: Double,   // Current price
    val pc: Double   // Previous close price
)