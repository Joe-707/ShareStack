package com.sharestack.data

data class Stock(
    val symbol: String,
    val name: String,
    val currentPrice: Double,
    val previousPrice: Double
)