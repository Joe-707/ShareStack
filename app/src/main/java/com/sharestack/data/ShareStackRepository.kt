package com.sharestack.data

import com.sharestack.BuildConfig
import com.sharestack.data.remote.SupabaseService
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
import com.sharestack.data.remote.NetworkModule
import java.security.MessageDigest

class ShareStackRepository {

    private val supabaseService = SupabaseService()

    // ========== STATE ==========

    private val _stacks = MutableStateFlow<List<Stack>>(emptyList())
    val stacks: StateFlow<List<Stack>> = _stacks.asStateFlow()

    private val _currentPrices = MutableStateFlow<Map<String, Double>>(emptyMap())
    val currentPrices: StateFlow<Map<String, Double>> = _currentPrices.asStateFlow()

    private val _balance = MutableStateFlow(500.0)
    val balance: StateFlow<Double> = _balance.asStateFlow()

    private val trackedSymbols = listOf("NVDA", "AMZN", "AAPL", "GOOGL", "TSLA")

    // ========== INIT ==========

    init {
        CoroutineScope(Dispatchers.IO).launch {
            loadStacksFromSupabase()
        }
        startPriceUpdates()
    }

    // ========== SUPABASE OPERATIONS ==========

    private suspend fun loadStacksFromSupabase() {
        try {
            val stacks = supabaseService.getAllStacks()
            _stacks.value = stacks
        } catch (e: Exception) {
            println("Error loading stacks from Supabase: ${e.message}")
            _stacks.value = emptyList()
        }
    }



    fun refreshStacks() {
        CoroutineScope(Dispatchers.IO).launch {
            loadStacksFromSupabase()
        }
    }

    // ========== AUTHENTICATION ==========
    suspend fun register(username: String, password: String, name: String): Boolean {
        if (supabaseService.userExists(username)) {
            return false
        }
        val user = User(
            id = username,
            name = name,
            totalPortfolioValue = 0.0,
            email = username
        )
        val hashedPassword = hashPassword(password)
        // ✅ Pass the actual password
        return supabaseService.insertUser(user, hashedPassword)
    }


    suspend fun login(username: String, password: String): User? {
        val hashedPassword = hashPassword(password)
        return supabaseService.getUser(username, hashedPassword)
    }

    suspend fun userExists(username: String): Boolean {
        return supabaseService.userExists(username)
    }

    // ========== STACK OPERATIONS ==========

    suspend fun createStack(name: String, stockSymbol: String, members: List<StackMember>): Boolean {
        val stackId = System.currentTimeMillis().toString()
        val stack = Stack(
            id = stackId,
            name = name,
            members = members,
            stockSymbol = stockSymbol,
            sharesOwned = 0.0,
            purchasePrice = 0.0,
            activeProposals = emptyList()
        )
        val success = supabaseService.insertStack(stack)
        if (success) {
            loadStacksFromSupabase()
        }
        return success
    }

    suspend fun createProposal(stackId: String, stockTarget: String, targetAmount: Double, members: List<String>): Boolean {
        val proposalId = System.currentTimeMillis().toString()
        val proposal = Proposal(proposalId, stockTarget, targetAmount, members)
        val success = supabaseService.insertProposal(proposal, stackId)
        if (success) {
            loadStacksFromSupabase()
        }
        return success
    }

    fun getAllStacks(): List<Stack> {
        return _stacks.value
    }

    fun getStackById(id: String): Stack? {
        return _stacks.value.find { it.id == id }
    }

    fun getProposalById(proposalId: String): Proposal? {
        for (stack in _stacks.value) {
            val proposal = stack.activeProposals.find { it.id == proposalId }
            if (proposal != null) return proposal
        }
        return null
    }

    // ========== PRICE UPDATES ==========
    private var isPriceUpdatesStarted = false

    fun startPriceUpdates() {
        if (isPriceUpdatesStarted) return
        isPriceUpdatesStarted = true

        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    fetchAllPrices()
                } catch (e: Exception) {
                    println("Error fetching prices: ${e.message}")
                }
                delay(15000)
            }
        }
    }

    private suspend fun fetchAllPrices() {
        val apiKey = BuildConfig.FINNHUB_API_KEY
        if (apiKey.isEmpty() || apiKey == "null") {
            println("⚠️ Finnhub API key is missing or invalid")
            return
        }

        val newPrices = mutableMapOf<String, Double>()
        val trackedSymbols = listOf("NVDA", "AMZN", "AAPL", "GOOGL", "TSLA")

        for (symbol in trackedSymbols) {
            try {
                val response = NetworkModule.apiService.getQuote(symbol, apiKey)
                newPrices[symbol] = response.c
                println("✅ Fetched $symbol: ${response.c}")
            } catch (e: Exception) {
                println("❌ Failed to fetch $symbol: ${e.message}")
                _currentPrices.value[symbol]?.let { newPrices[symbol] = it }
            }
        }

        if (newPrices.isNotEmpty()) {
            _currentPrices.value = newPrices
        }
    }

    // ========== PORTFOLIO CALCULATION ==========

    fun calculatePortfolioValue(): Double {
        var total = 0.0
        val currentPrices = _currentPrices.value

        for (stack in _stacks.value) {
            val price = currentPrices[stack.stockSymbol] ?: stack.purchasePrice
            total += price * stack.sharesOwned
        }
        return total
    }

    suspend fun executeProposalPurchase(stackId: String, proposalId: String, targetAmount: Double, currentPrice: Double, updatedMembers: List<StackMember>,contributions: Map<String, Double> ): Boolean {
        // Core Math: Convert the Ksh funding target into actual shares owned!
        val newShares = targetAmount / currentPrice

        // Mutate the local StateFlow so your UI instantly jumps and shows the new value
        val updatedStacks = _stacks.value.map { stack ->
            if (stack.id == stackId) {

                // Generate the permanent receipt
                val newLedgerEntry = com.sharestack.models.LedgerEntry(
                    id = System.currentTimeMillis().toString(),
                    description = "Executed purchase of ${stack.stockSymbol}",
                    totalAmount = targetAmount,
                    contributions = contributions,
                    timestamp = System.currentTimeMillis()
                )

                stack.copy(
                    sharesOwned = stack.sharesOwned + newShares,
                    purchasePrice = currentPrice,
                    members=updatedMembers,
                    // Deletes the active proposal so it vanishes from the UI
                    activeProposals = stack.activeProposals.filter { it.id != proposalId },
                    ledger = stack.ledger + newLedgerEntry
                )
            } else stack
        }
        _stacks.value = updatedStacks
        _balance.update { it - targetAmount }

        return true
    }

    // ========== MOCK OPERATIONS ==========

    fun fundWallet(amount: Double) {
        _balance.update { it + amount }
    }

    // One-way SHA-256 Hashing function
    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun buyStock(stackId: String, amount: Double): Boolean {
        return if (_balance.value >= amount) {
            _balance.update { it - amount }
            true
        } else {
            false
        }
    }
}