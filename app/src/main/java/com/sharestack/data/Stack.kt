package com.sharestack.data

data class Stack(
    val id: String,
    val name: String,
    val members: List<StackMember>,
    val stockSymbol: String,
    val sharesOwned: Double,
    val purchasePrice: Double
)