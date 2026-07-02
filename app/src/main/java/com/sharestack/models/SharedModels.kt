package com.sharestack.models

// ========== USER ==========
data class User(
    val id: String = "u1",
    val name: String,
    val totalPortfolioValue: Double = 0.0
)

// ========== STACK (Investment Group) ==========
data class Stack(
    val id: String,
    val name: String,
    val members: List<StackMember>,
    val stockSymbol: String,
    val sharesOwned: Double,
    val purchasePrice: Double,
    val activeProposals: List<Proposal> = emptyList()
)

// ========== STACK MEMBER ==========
data class StackMember(
    val name: String,
    val ownershipPercentage: Int  // e.g., 40 = 40%
)

// ========== PROPOSAL ==========
data class Proposal(
    val id: String,
    val stockTarget: String,      // e.g., "Nvidia (NVDA)"
    val targetAmount: Double,     // Total Ksh to pool
    val activeMembers: List<String>  // Names of members participating
)

// ========== STOCK (for price tracking) ==========
data class Stock(
    val symbol: String,
    val name: String,
    val currentPrice: Double,
    val previousPrice: Double
)

// ========== INVESTMENT GROUP (Legacy/UI Compat) ==========
// Keep this for backward compatibility with Austin's UI
data class InvestmentGroup(
    val id: String,
    val name: String,
    val memberCount: Int,
    val activeProposals: List<Proposal> = emptyList()
)

// Extension function to convert Stack to InvestmentGroup for UI
fun Stack.toInvestmentGroup(): InvestmentGroup {
    return InvestmentGroup(
        id = this.id,
        name = this.name,
        memberCount = this.members.size,
        activeProposals = this.activeProposals
    )
}