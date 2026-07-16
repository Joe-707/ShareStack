package com.sharestack.data

import com.sharestack.BuildConfig
import com.sharestack.ShareStackApplication
import com.sharestack.data.local.DatabaseHelper
import com.sharestack.data.remote.NetworkModule
import com.sharestack.models.Proposal
import com.sharestack.models.Stack
import com.sharestack.models.StackMember
import com.sharestack.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay


class ShareStackRepository() {

    private val dbHelper: DatabaseHelper by lazy {
        // This will be set via a setter or application class
        DatabaseHelper(ShareStackApplication.appContext)
    }

    // ========== REAL-TIME PRICES ==========

    // Holds current prices for each stock symbol
    private val _currentPrices = MutableStateFlow<Map<String, Double>>(emptyMap())
    val currentPrices: StateFlow<Map<String, Double>> = _currentPrices.asStateFlow()

    // Stock symbols to track
    private val trackedSymbols = listOf("NVDA", "AMZN", "AAPL", "GOOGL")

    init {
        // Start fetching real prices when repository is created
        startPriceUpdates()
    }

    private fun startPriceUpdates() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    fetchAllPrices()
                } catch (e: Exception) {
                    // Log error but continue
                    println("Error fetching prices: ${e.message}")
                }
                delay(15000)  // Update every 15 seconds (Finnhub free tier limit)
            }
        }
    }

    private suspend fun fetchAllPrices() {
        val apiKey = BuildConfig.FINNHUB_API_KEY
        val newPrices = mutableMapOf<String, Double>()

        for (symbol in trackedSymbols) {
            try {
                val response = NetworkModule.apiService.getQuote(symbol, apiKey)
                newPrices[symbol] = response.c  // Current price
            } catch (e: Exception) {
                // If API fails, keep old price or use fallback
                println("Failed to fetch $symbol: ${e.message}")
                // Keep existing price if available
                _currentPrices.value[symbol]?.let { newPrices[symbol] = it }
            }
        }

        if (newPrices.isNotEmpty()) {
            _currentPrices.value = newPrices
        }
    }

    // ========== AUTHENTICATION ==========

    fun login(username: String, password: String): User? {
        return dbHelper.getUser(username, password)
    }

    fun register(username: String, password: String, name: String): Boolean {
        if (dbHelper.userExists(username)) {
            return false
        }
        return dbHelper.insertUser(username, username, password, name)
    }

    fun getUser(username: String, password: String): User? {
        return dbHelper.getUser(username, password)
    }

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
        fun updateUserName(newName: String) {
        _currentUser.update { it.copy(name = newName) }
    }

    fun getStackById(id: String): Stack? {
        return _stacks.value.find { it.id == id }
    }
    // Adds a new pitch to the specific group's list
    fun addProposalToStack(stackId: String, proposal: Proposal) {
        _stacks.update { currentStacks ->
            currentStacks.map { stack ->
                if (stack.id == stackId) {
                    // Make a copy of the stack and add the new proposal to its list
                    stack.copy(activeProposals = stack.activeProposals + proposal)
                } else {
                    stack
                }
            }
        }
    }
}