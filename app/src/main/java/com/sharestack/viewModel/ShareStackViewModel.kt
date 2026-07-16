package com.sharestack.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharestack.data.ShareStackRepository
import com.sharestack.data.MockPriceService
import com.sharestack.data.MockAuthService
import com.sharestack.models.Proposal
import com.sharestack.models.Stack
import com.sharestack.models.User
import com.sharestack.models.InvestmentGroup
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.content.Context
import com.sharestack.data.local.DatabaseHelper

class ShareStackViewModel : ViewModel() {

    // ✅ Initialize repository with a dummy context
    // The DatabaseHelper will handle this properly
    private val repository = ShareStackRepository()
    private val priceService = MockPriceService()

    // ========== EXPOSED STATE ==========

    // User state
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _balance = MutableStateFlow(500.0)
    val balance: StateFlow<Double> = _balance.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Stack data
    val stacks: StateFlow<List<Stack>> = repository.stacks
    val currentPrices: StateFlow<Map<String, Double>> = repository.currentPrices

    // ========== UI-FRIENDLY DERIVED STATE ==========

    // Convert Stacks to InvestmentGroups for Austin's UI
    // NEW CODE - With real-time prices
    val investmentGroups: StateFlow<List<InvestmentGroup>> = combine(
        stacks,
        currentPrices
    ) { stacksList, prices ->
        stacksList.map { stack ->
            val currentPrice = prices[stack.stockSymbol] ?: stack.purchasePrice
            val totalValue = currentPrice * stack.sharesOwned
            val totalCost = stack.purchasePrice * stack.sharesOwned
            val profitLoss = totalValue - totalCost

            InvestmentGroup(
                id = stack.id,
                name = stack.name,
                memberCount = stack.members.size,
                activeProposals = stack.activeProposals,
                stockSymbol = stack.stockSymbol,
                sharesOwned = stack.sharesOwned,
                purchasePrice = stack.purchasePrice,
                currentPrice = currentPrice,
                totalValue = totalValue,
                profitLoss = profitLoss
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    //Total portfolio value
    val totalPortfolioValue: StateFlow<Double> = investmentGroups.map { groups ->
        groups.sumOf { it.totalValue }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = 0.0
    )

    // Get a single stack by ID
    fun getStackById(id: String): Stack? {
        return stacks.value.find { it.id == id }
    }

    // Get active proposals for a group
    fun getActiveProposalsForGroup(groupId: String): List<Proposal> {
        val stack = getStackById(groupId)
        return stack?.activeProposals ?: emptyList()
    }

    // Get a specific proposal by its ID
    fun getProposalById(proposalId: String): Proposal? {
        stacks.value.forEach { stack ->
            stack.activeProposals.find { it.id == proposalId }?.let { return it }
        }
        return null
    }

    // ========== ACTIONS ==========

    // Buy a stock (deduct from balance)
    fun buyStock(stackId: String): Boolean {
        val stack = getStackById(stackId)
        stack?.let {
            val price = currentPrices.value[it.stockSymbol] ?: 0.0
            val cost = price * it.sharesOwned
            return repository.buyStock(stackId, cost)
        }
        return false
    }

    // Fund wallet
    fun fundWallet(amount: Double) {
        repository.fundWallet(amount)
    }

    // ========== AUTHENTICATION ==========

    // ✅ Returns User? (null if not found)
    fun login(email: String, password: String): Boolean {
        val user = repository.login(email, password)
        return if (user != null) {
            _currentUser.value = user
            _isLoggedIn.value = true
            true
        } else {
            false
        }
    }

    fun register(name: String, email: String, password: String): Boolean {
        val success = repository.register(email, password, name)
        if (success) {
            _currentUser.value = User(email, name, 0.0, email)
            _isLoggedIn.value = true
        }
        return success
    }


    fun logout() {
        _currentUser.value = null
        _isLoggedIn.value = false
    }


    // ========== PROPOSAL ==========
    fun createProposal(stackId: String, stockTicker: String, targetAmount: Double) {
        // 1. Create the new proposal object using what the user typed
        val newPitch = Proposal(
            id = "p_${System.currentTimeMillis()}", // Generates a unique random ID
            stockTarget = stockTicker,
            targetAmount = targetAmount,
            activeMembers = listOf("Austin", "Joe") // The current active members
        )

        // 2. Send it to the repository so the UI instantly redraws!
        repository.addProposalToStack(stackId, newPitch)
    }


    fun setDemoUser() {
        _currentUser.value = User("u1", "Demo User", 500.0)
        _isLoggedIn.value = true
        _balance.value = 500.0
    }

    // ========== REDISTRIBUTION ==========

    fun redistributeFunds(proposalId: String, newSplit: Map<String, Double>) {
        println("New split for proposal $proposalId: $newSplit")
    }
}