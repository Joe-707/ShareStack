package com.sharestack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sharestack.ui.theme.ShareStackTheme
import com.sharestack.viewmodel.ShareStackViewModel
import java.util.*

fun getGreetingMessage(): String {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (currentHour) {
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }
}

@Composable
fun HomeDashboardScreen(
    viewModel: ShareStackViewModel = viewModel(),
    onNavigateToGroupHub: (String) -> Unit = {},
    onNavigateToProposal: () -> Unit = {},
    onNavigateToCreateGroup: () -> Unit ={},
    onLogout: () -> Unit = {}
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val portfolioValue by viewModel.totalPortfolioValue.collectAsState()
    val investmentGroups by viewModel.investmentGroups.collectAsState()

    val currentUserName = currentUser?.name ?: "User"

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

                TextButton(onClick = onLogout) {
                    Text("Logout", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateGroup,
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("+", fontSize = 24.sp, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(all = 24.dp)) {
                    Text("Total Portfolio Value", color = MaterialTheme.colorScheme.onPrimary)
                    Text(
                        text = formatCurrency(portfolioValue),
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

            // THE PROFESSIONAL EMPTY STATE
            if (investmentGroups.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You don't have any active stacks yet.\nClick the + button to start pooling!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(investmentGroups) { group ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onNavigateToGroupHub(group.id) }
                        ) {
                            Column(modifier = Modifier.padding(all = 16.dp)) {
                                Text(text = group.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(
                                    text = "Members: ${group.memberCount} | Active Proposals: ${group.activeProposals.size}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

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
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeDashboard() {
    ShareStackTheme { HomeDashboardScreen() }
}