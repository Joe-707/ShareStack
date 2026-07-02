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

class ShareStackViewModel : ViewModel() {

    private val repository = ShareStackRepository()
    private val priceService = MockPriceService()
    private val authService = MockAuthService()

    // ========== EXPOSED STATE ==========

    // User state
    val currentUser: StateFlow<User> = repository.currentUser
    val balance: StateFlow<Double> = repository.balance
    val isLoggedIn: StateFlow<Boolean> = authService.isLoggedIn

    // Stack data
    val stacks: StateFlow<List<Stack>> = repository.stacks
    val currentPrices: StateFlow<Map<String, Double>> = priceService.prices

    // ========== UI-FRIENDLY DERIVED STATE ==========

    // Convert Stacks to InvestmentGroups for Austin's UI
    val investmentGroups: StateFlow<List<InvestmentGroup>> = stacks
        .map { stacksList ->
            stacksList.map { stack ->
                InvestmentGroup(
                    id = stack.id,
                    name = stack.name,
                    memberCount = stack.members.size,
                    activeProposals = stack.activeProposals
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
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

    // ========== AUTH ==========

    fun login(email: String, password: String) {
        viewModelScope.launch {
            authService.login(email, password)
        }
    }

    fun signup(name: String, email: String, password: String) {
        viewModelScope.launch {
            authService.signup(name, email, password)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authService.logout()
        }
    }

    // ========== PROPOSAL ==========

    fun createProposal(stackId: String, stockTicker: String, targetAmount: Double) {
        println("New Proposal: $stockTicker @ Ksh $targetAmount for group $stackId")
    }

    // ========== REDISTRIBUTION ==========

    fun redistributeFunds(proposalId: String, newSplit: Map<String, Double>) {
        println("New split for proposal $proposalId: $newSplit")
    }
}