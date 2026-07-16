package com.sharestack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sharestack.models.InvestmentGroup
import com.sharestack.models.Proposal
import com.sharestack.ui.theme.ShareStackTheme
import com.sharestack.viewModel.ShareStackViewModel
import java.text.NumberFormat
import java.util.*

// ========== HELPER FUNCTIONS ==========

fun getGreetingMessage(): String {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (currentHour) {
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }
}

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(amount)
}

// ========== MAIN SCREEN ==========

@Composable
fun HomeDashboardScreen(
    viewModel: ShareStackViewModel = viewModel(),
    onNavigateToGroupHub: (String) -> Unit = {},
    onNavigateToProposal: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    // ✅ Get data from ViewModel
    val currentUser by viewModel.currentUser.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val investmentGroups by viewModel.investmentGroups.collectAsState()

    val currentUserName = currentUser?.name ?: "User"

    // Create dummy proposal for display (keeps compatibility with existing UI)
    val dummyProposal = Proposal(
        id = "p1",
        stockTarget = "Nvidia (NVDA)",
        targetAmount = 17000.0,
        activeMembers = listOf("Joe", "Austin", "Sarah")
    )

    // ✅ Use real data if available, fallback to dummy data
    val displayGroups = if (investmentGroups.isEmpty()) {
        listOf(
            InvestmentGroup(
                id = "g1",
                name = "Tech Giants Pool",
                memberCount = 3,
                activeProposals = listOf(dummyProposal),
                stockSymbol = "NVDA",
                sharesOwned = 1.0,
                purchasePrice = 900.0,
                currentPrice = 1050.0,
                totalValue = 1050.0,
                profitLoss = 150.0
            ),
            InvestmentGroup(
                id = "g2",
                name = "Real Estate Fund",
                memberCount = 5,
                activeProposals = emptyList(),
                stockSymbol = "",
                sharesOwned = 0.0,
                purchasePrice = 0.0,
                currentPrice = 0.0,
                totalValue = 0.0,
                profitLoss = 0.0
            )
        )
    } else {
        investmentGroups
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .systemBarsPadding()
                    .padding(all = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${getGreetingMessage()}, $currentUserName!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // LOGOUT BUTTON
                TextButton(onClick = onLogout) {
                    Text(
                        "Logout",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // ✅ Portfolio Value Card - SAME COLORS
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Column(
                    modifier = Modifier.padding(all = 24.dp)
                ) {
                    Text(
                        text = "Total Stack Value",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = formatCurrency(balance),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Your Active Stacks",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayGroups) { group ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigateToGroupHub(group.id) }
                    ) {
                        Column(
                            modifier = Modifier.padding(all = 16.dp)
                        ) {
                            Text(
                                text = group.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Members: ${group.memberCount} | Active Proposals: ${group.activeProposals.size}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // ✅ Show current price if available
                            if (group.stockSymbol.isNotEmpty() && group.currentPrice > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                val isProfit = group.profitLoss >= 0
                                Text(
                                    text = "${group.stockSymbol}: ${formatCurrency(group.currentPrice)} " +
                                            "(${if (isProfit) "+" else ""}${formatCurrency(group.profitLoss)})",
                                    color = if (isProfit) Color.Green else Color.Red,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (group.activeProposals.isNotEmpty()) {
                                Button(
                                    onClick = onNavigateToProposal,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("View Active Proposal")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ========== PREVIEW ==========

@Preview(showBackground = true)
@Composable
fun PreviewHomeDashboard() {
    ShareStackTheme {
        HomeDashboardScreen()
    }
}