package com.sharestack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharestack.data.ShareStackRepository
import com.sharestack.models.Proposal
import com.sharestack.models.Stack
import com.sharestack.models.StackMember
import com.sharestack.models.User
import com.sharestack.models.InvestmentGroup
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ShareStackViewModel : ViewModel() {

    private val repository = ShareStackRepository()

    // ========== USER STATE ==========

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _balance = MutableStateFlow(500.0)
    val balance: StateFlow<Double> = _balance.asStateFlow()

    // ========== STACK DATA ==========

    val stacks: StateFlow<List<Stack>> = repository.stacks
    val currentPrices: StateFlow<Map<String, Double>> = repository.currentPrices

    // ========== UI-FRIENDLY DERIVED STATE ==========

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

    val totalPortfolioValue: StateFlow<Double> = investmentGroups.map { groups ->
        groups.sumOf { it.totalValue }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = 0.0
    )

    // ========== AUTHENTICATION ==========

    suspend fun login(email: String, password: String): Boolean {
        println("🔐 LOGIN ATTEMPT: email=$email, password=$password")
        val user = repository.login(email, password)
        println("🔐 USER RESULT: $user")
        return if (user != null) {
            _currentUser.value = user
            _isLoggedIn.value = true
            // ✅ Start price updates after successful login
            repository.startPriceUpdates()
            println("🔐 LOGIN SUCCESS")
            true
        } else {
            println("🔐 LOGIN FAILED - user is null")
            false
        }
    }

    suspend fun register(name: String, email: String, password: String): Boolean {
        println("📝 Register: $name, $email, $password")
        val result = repository.register(email, password, name)
        if (result) {
            _currentUser.value = User(email, name, 0.0, email)
            _isLoggedIn.value = true
            // ✅ Start price updates after successful registration
            repository.startPriceUpdates()
            println("📝 Register SUCCESS")
        } else {
            println("📝 Register FAILED")
        }
        return result
    }

    fun logout() {
        _currentUser.value = null
        _isLoggedIn.value = false
        println("🔐 LOGOUT")
    }

    // ========== STACK MANAGEMENT ==========

    suspend fun createNewStack(name: String, stockSymbol: String, members: List<StackMember>): Boolean {
        return repository.createStack(name, stockSymbol, members)
    }

    suspend fun createNewProposal(stackId: String, stockTarget: String, targetAmount: Double, members: List<String>): Boolean {
        return repository.createProposal(stackId, stockTarget, targetAmount, members)
    }

    fun refreshData() {
        repository.refreshStacks()
    }

    fun getAllStacks(): List<Stack> {
        return repository.getAllStacks()
    }

    // ========== ACTIONS ==========

    fun buyStock(stackId: String): Boolean {
        val stack = getStackById(stackId)
        stack?.let {
            val price = currentPrices.value[it.stockSymbol] ?: 0.0
            val cost = price * it.sharesOwned
            return repository.buyStock(stackId, cost)
        }
        return false
    }

    fun fundWallet(amount: Double) {
        repository.fundWallet(amount)
        _balance.value = repository.balance.value
    }

    fun getStackById(id: String): Stack? {
        return stacks.value.find { it.id == id }
    }

    fun getActiveProposalsForGroup(groupId: String): List<Proposal> {
        val stack = getStackById(groupId)
        return stack?.activeProposals ?: emptyList()
    }

    fun getProposalById(proposalId: String): Proposal? {
        return repository.getProposalById(proposalId)
    }

    fun createProposal(stackId: String, stockTicker: String, targetAmount: Double) {
        val members = getStackById(stackId)?.members?.map { it.name } ?: emptyList()
        viewModelScope.launch {
            repository.createProposal(stackId, stockTicker, targetAmount, members)
        }
    }

    fun redistributeFunds(proposalId: String, newSplit: Map<String, Double>) {
        println("New split for proposal $proposalId: $newSplit")
    }

    fun setDemoUser() {
        _currentUser.value = User("u1", "Demo User", 500.0, "demo@example.com")
        _isLoggedIn.value = true
        _balance.value = 500.0
    }
}