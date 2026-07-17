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

    val investmentGroups: StateFlow<List<InvestmentGroup>> = combine(
        stacks,
        currentPrices,
        currentUser
    ) { stacksList, prices, user ->
        // 1. SECURITY CHECK: If nobody is logged in, return an empty dashboard
        if (user == null) return@combine emptyList()

        // 2. ISOLATION: Only keep the stacks where this specific user is an official member
        val myStacks = stacksList.filter { stack ->
            stack.members.any { it.name.equals(user.name, ignoreCase = true) }
        }

        // 3. MATH: Calculate their specific contribution
        myStacks.map { stack ->
            val currentPrice = prices[stack.stockSymbol] ?: stack.purchasePrice

            // The TOTAL pool of money in the group
            val vaultTotalValue = currentPrice * stack.sharesOwned
            val vaultTotalCost = stack.purchasePrice * stack.sharesOwned

            // Find exactly what percentage the logged-in user owns (ignoring uppercase/lowercase typos)
            val myMemberData = stack.members.find { it.name.equals(user.name, ignoreCase = true) }
            val myPercentage = (myMemberData?.ownershipPercentage ?: 0) / 100.0

            // Multiply the vault by their percentage to get their true personal portfolio value
            val myPersonalValue = vaultTotalValue * myPercentage
            val myPersonalProfit = (vaultTotalValue - vaultTotalCost) * myPercentage

            InvestmentGroup(
                id = stack.id,
                name = stack.name,
                memberCount = stack.members.size,
                activeProposals = stack.activeProposals,
                stockSymbol = stack.stockSymbol,
                sharesOwned = stack.sharesOwned,
                purchasePrice = stack.purchasePrice,
                currentPrice = currentPrice,
                totalValue = myPersonalValue,
                profitLoss = myPersonalProfit,
                ledger = stack.ledger
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

    suspend fun login(email: String, password: String): String {
        println("🔐 LOGIN ATTEMPT: email=$email")

        // 1. Check if the user is in the database at all
        val exists = repository.userExists(email)
        if (!exists) {
            println("🔐 LOGIN FAILED - Account not found")
            return "Account not found. Please sign up."
        }

        // 2. If they exist, verify the password hash
        val user = repository.login(email, password)
        return if (user != null) {
            _currentUser.value = user
            _isLoggedIn.value = true
            repository.startPriceUpdates()
            println("🔐 LOGIN SUCCESS")
            "Success"
        } else {
            println("🔐 LOGIN FAILED - Incorrect password")
            "Incorrect password. Please try again."
        }
    }

    suspend fun register(name: String, email: String, password: String): Boolean {
        println("📝 Register: $name, $email, $password")
        val result = repository.register(email, password, name)
        if (result) {
            _currentUser.value = User(email, name, 0.0, email)
            _isLoggedIn.value = true
            // Start price updates after successful registration
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
    fun approveAndExecuteProposal(proposalId: String, contributions: Map<String, Double>) {
        viewModelScope.launch {
            val stack = stacks.value.find { s -> s.activeProposals.any { it.id == proposalId } } ?: return@launch
            val proposal = stack.activeProposals.find { it.id == proposalId } ?: return@launch

            val livePrice = currentPrices.value[stack.stockSymbol] ?: 100.0

            // === 1. DYNAMIC OWNERSHIP RECALCULATION ===
            val currentStackValue = stack.sharesOwned * livePrice
            val newTotalValue = currentStackValue + proposal.targetAmount

            var accumulatedPercentage = 0

            val updatedMembers = stack.members.mapIndexed { index, member ->
                // Calculate their existing equity plus their new contribution
                val currentEquity = currentStackValue * (member.ownershipPercentage / 100.0)
                val newContribution = contributions[member.name] ?: 0.0
                val totalPersonalEquity = currentEquity + newContribution

                // Calculate the new percentage slice
                val rawPercentage = if (newTotalValue > 0) {
                    Math.round((totalPersonalEquity / newTotalValue) * 100).toInt()
                } else {
                    member.ownershipPercentage
                }

                // The last member takes the remainder so the math always equals exactly 100%
                if (index == stack.members.size - 1) {
                    member.copy(ownershipPercentage = 100 - accumulatedPercentage)
                } else {
                    accumulatedPercentage += rawPercentage
                    member.copy(ownershipPercentage = rawPercentage)
                }
            }

            // === 2. EXECUTE PURCHASE WITH NEW PERCENTAGES ===
            repository.executeProposalPurchase(
                stackId = stack.id,
                proposalId = proposal.id,
                targetAmount = proposal.targetAmount,
                currentPrice = livePrice,
                updatedMembers = updatedMembers, // Pushing the new legal split to the database
                contributions=contributions
            )
        }
    }
    fun setDemoUser() {
        _currentUser.value = User("u1", "Demo User", 500.0, "demo@example.com")
        _isLoggedIn.value = true
        _balance.value = 500.0
    }
}