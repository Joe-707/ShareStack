package com.sharestack.data

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockPriceService {

    // Holds current prices for each stock symbol
    private val _prices = MutableStateFlow<Map<String, Double>>(emptyMap())
    val prices: StateFlow<Map<String, Double>> = _prices.asStateFlow()

    init {
        startPriceUpdates()
    }

    private fun startPriceUpdates() {
        // Initialize with base prices
        val tickers = listOf("NVDA", "AMZN", "AAPL", "GOOGL")
        val initialPrices = tickers.associateWith { getBasePrice(it) }
        _prices.value = initialPrices

        // Launch a background coroutine to update prices every 3 seconds
        CoroutineScope(Dispatchers.IO).launch {
            var currentPrices = _prices.value
            while (true) {
                delay(3000)  // Update every 3 seconds

                // Simulate price movement: -3% to +3% random change
                currentPrices = currentPrices.mapValues { (symbol, price) ->
                    val changePercent = (Math.random() * 6 - 3)  // -3 to +3
                    val newPrice = price * (1 + changePercent / 100)
                    // Don't let price drop below $1
                    newPrice.coerceAtLeast(1.0)
                }

                _prices.value = currentPrices
            }
        }
    }

    private fun getBasePrice(symbol: String): Double = when (symbol) {
        "NVDA" -> 1050.0
        "AMZN" -> 360.0
        "AAPL" -> 180.0
        "GOOGL" -> 140.0
        "TSLA" -> 220.0
        else -> 100.0
    }
}