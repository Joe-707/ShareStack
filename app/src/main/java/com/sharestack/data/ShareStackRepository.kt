package com.sharestack.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.sharestack.models.Proposal
import com.sharestack.models.Stack
import com.sharestack.models.StackMember
import com.sharestack.models.User

class ShareStackRepository {

    // ========== MOCK DATA ==========

    // Mock user balance (starts at $500)
    private val _balance = MutableStateFlow(500.0)
    val balance: StateFlow<Double> = _balance.asStateFlow()

    // Mock user info - ✅ FIXED to match User class
    private val _currentUser = MutableStateFlow(
        User(
            id = "u1",
            name = "Joe",
            totalPortfolioValue = 500.0
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    // Mock stacks (hardcoded groups)
    private val _stacks = MutableStateFlow(getMockStacks())
    val stacks: StateFlow<List<Stack>> = _stacks.asStateFlow()

    // ========== GENERATE MOCK DATA ==========

    private fun getMockStacks(): List<Stack> {
        return listOf(
            Stack(
                id = "1",
                name = "Weekend Traders",
                members = listOf(
                    StackMember("Joe", 40),
                    StackMember("Austin", 35),
                    StackMember("Sarah", 25)
                ),
                stockSymbol = "NVDA",
                sharesOwned = 1.0,
                purchasePrice = 900.0,
                activeProposals = listOf(
                    Proposal(
                        id = "p1",
                        stockTarget = "Nvidia (NVDA)",
                        targetAmount = 17000.0,
                        activeMembers = listOf("Joe", "Austin", "Sarah")
                    )
                )
            ),
            Stack(
                id = "2",
                name = "Tech Bros",
                members = listOf(
                    StackMember("Joe", 50),
                    StackMember("Mike", 50)
                ),
                stockSymbol = "AMZN",
                sharesOwned = 0.5,
                purchasePrice = 350.0,
                activeProposals = emptyList()
            ),
            Stack(
                id = "3",
                name = "Value Hunters",
                members = listOf(
                    StackMember("Joe", 30),
                    StackMember("Emma", 40),
                    StackMember("David", 30)
                ),
                stockSymbol = "AAPL",
                sharesOwned = 0.75,
                purchasePrice = 150.0,
                activeProposals = emptyList()
            )
        )
    }

    // ========== MOCK OPERATIONS ==========

    fun fundWallet(amount: Double) {
        _balance.update { it + amount }
    }

    fun buyStock(stackId: String, amount: Double): Boolean {
        return if (_balance.value >= amount) {
            _balance.update { it - amount }
            true
        } else {
            false
        }
    }

    fun getStackById(id: String): Stack? {
        return _stacks.value.find { it.id == id }
    }
}