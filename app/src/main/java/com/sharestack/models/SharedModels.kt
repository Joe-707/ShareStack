package com.sharestack.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String,
    val totalPortfolioValue: Double = 0.0,
    val email: String = ""
)

@Serializable
data class StackMember(
    val name: String,
    val ownershipPercentage: Int
)

@Serializable
data class Proposal(
    val id: String,
    val stockTarget: String,
    val targetAmount: Double,
    val activeMembers: List<String>
)

@Serializable
data class LedgerEntry(
    val id: String,
    val description: String,
    val totalAmount: Double,
    val contributions: Map<String, Double>,
    val timestamp: Long
)

@Serializable
data class Stack(
    val id: String,
    val name: String,
    val members: List<StackMember>,
    val stockSymbol: String,
    val sharesOwned: Double,
    val purchasePrice: Double,
    val activeProposals: List<Proposal> = emptyList(),
    val ledger: List<LedgerEntry> = emptyList() // The history ledger is correctly placed here
)

// ========== STOCK (for price tracking) ==========
data class Stock(
    val symbol: String,
    val name: String,
    val currentPrice: Double,
    val previousPrice: Double
)

// ========== INVESTMENT GROUP (Legacy/UI Compat) ==========
data class InvestmentGroup(
    val id: String,
    val name: String,
    val memberCount: Int,
    val activeProposals: List<Proposal> = emptyList(),
    val stockSymbol: String = "",
    val sharesOwned: Double = 0.0,
    val purchasePrice: Double = 0.0,
    val currentPrice: Double = 0.0,
    val totalValue: Double = 0.0,
    val profitLoss: Double = 0.0,
    val ledger: List<LedgerEntry> = emptyList() // The history ledger is correctly placed here
)