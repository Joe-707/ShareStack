//package com.sharestack.ui.screens
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.sharestack.ui.theme.ShareStackTheme
//
//@Composable
//fun HomeDashboardScreen(onNavigateToProposal: () -> Unit = {}) {
//    // Dummy data for your investment groups
//    val investmentGroups = listOf(
//        "Tech Giants Pool",
//        "Real Estate Fund",
//        "Green Energy Co-op"
//    )
//
//    // Scaffold provides the standard app screen structure
//    Scaffold(
//        topBar = {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text(
//                    text = "Welcome back, Austin!",
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = MaterialTheme.colorScheme.primary
//                )
//            }
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//                .padding(horizontal = 16.dp)
//        ) {
//            // Total Portfolio Value Card
//            Card(
//                modifier = Modifier.fillMaxWidth(),
//                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
//            ) {
//                Column(modifier = Modifier.padding(24.dp)) {
//                    Text("Total Stack Value", color = MaterialTheme.colorScheme.onPrimaryContainer)
//                    Text(
//                        text = "Ksh 145,000.0",
//                        fontSize = 32.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = MaterialTheme.colorScheme.onPrimaryContainer
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(32.dp))
//
//            Text(
//                text = "Your Active Stacks",
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.padding(bottom = 12.dp)
//            )
//
//            // LazyColumn is Android's hyper-efficient way to display scrolling lists
//            LazyColumn(
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                items(investmentGroups) { groupName ->
//                    Card(
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Column(modifier = Modifier.padding(16.dp)) {
//                            Text(groupName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
//                            Text("Members: 3 | Active Proposals: 1", color = MaterialTheme.colorScheme.secondary)
//
//                            Spacer(modifier = Modifier.height(12.dp))
//
//                            // This button will route us to the specific Nvidia proposal we built earlier!
//                            Button(onClick = onNavigateToProposal) {
//                                Text("View Active Proposal")
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun PreviewHomeDashboard() {
//    ShareStackTheme {
//        HomeDashboardScreen()
//    }
//}
//
//package com.sharestack.ui.screens
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.sharestack.models.InvestmentGroup
//import com.sharestack.models.Proposal
//import com.sharestack.models.User
//import com.sharestack.ui.theme.ShareStackTheme
//
//@Composable
//fun HomeDashboardScreen(onNavigateToProposal: () -> Unit = {}) {
//    // 1. Instantiate our new Data Classes with dummy data
//    val currentUser = User(id = "u1", name = "Austin", totalPortfolioValue = 145000.0)
//
//    val dummyProposal = Proposal(
//        id = "p1",
//        stockTarget = "Nvidia (NVDA)",
//        targetAmount = 17000.0,
//        activeMembers = listOf("Joe", "Austin", "Sarah")
//    )
//
//    val dummyGroups = listOf(
//        InvestmentGroup(id = "g1", name = "Tech Giants Pool", memberCount = 3, activeProposals = listOf(dummyProposal)),
//        InvestmentGroup(id = "g2", name = "Real Estate Fund", memberCount = 5, activeProposals = emptyList()),
//        InvestmentGroup(id = "g3", name = "Green Energy Co-op", memberCount = 8, activeProposals = emptyList())
//    )
//
//    Scaffold(
//        topBar = {
//            Row(modifier = Modifier.fillMaxWidth().systemBarsPadding() .padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
//                // 2. Read dynamically from the User object
//                Text("Welcome back, ${currentUser.name}!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
//            }
//        }
//    ) { paddingValues ->
//        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)) {
//            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
//                Column(modifier = Modifier.padding(24.dp)) {
//                    Text("Total Stack Value", color = MaterialTheme.colorScheme.onPrimaryContainer)
//                    // 3. Read dynamically from the User object
//                    Text("Ksh ${currentUser.totalPortfolioValue}", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
//                }
//            }
//
//            Spacer(modifier = Modifier.height(32.dp))
//            Text("Your Active Stacks", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
//
//            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//                // 4. Loop through the Group objects dynamically
//                items(dummyGroups) { group ->
//                    Card(modifier = Modifier.fillMaxWidth()) {
//                        Column(modifier = Modifier.padding(16.dp)) {
//                            Text(group.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
//                            Text("Members: ${group.memberCount} | Active Proposals: ${group.activeProposals.size}", color = MaterialTheme.colorScheme.secondary)
//
//                            Spacer(modifier = Modifier.height(12.dp))
//
//                            // Only show the button if this specific group actually has an active proposal
//                            if (group.activeProposals.isNotEmpty()) {
//                                Button(onClick = onNavigateToProposal) {
//                                    Text("View Active Proposal")
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun PreviewHomeDashboard() {
//    ShareStackTheme { HomeDashboardScreen() }
//}

package com.sharestack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sharestack.models.InvestmentGroup
import com.sharestack.models.Proposal
import com.sharestack.ui.theme.ShareStackTheme
import com.sharestack.viewModel.ShareStackViewModel
import java.util.Calendar

// Helper function to determine time of day
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
    onNavigateToProposal: () -> Unit = {},
    onNavigateToGroupHub: () -> Unit = {}
) {
    // Observe state from ViewModel
    val currentUser by viewModel.currentUser.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val investmentGroups by viewModel.investmentGroups.collectAsState()
    val currentPrices by viewModel.currentPrices.collectAsState()

    // Create dummy proposal for display
    val dummyProposal = Proposal(
        id = "p1",
        stockTarget = "Nvidia (NVDA)",
        targetAmount = 17000.0,
        activeMembers = listOf("Joe", "Austin", "Sarah")
    )

    // Create dummy groups if real ones empty
    val displayGroups = if (investmentGroups.isEmpty()) {
        listOf(
            InvestmentGroup(
                id = "g1",
                name = "Tech Giants Pool",
                memberCount = 3,
                activeProposals = listOf(dummyProposal)
            ),
            InvestmentGroup(
                id = "g2",
                name = "Real Estate Fund",
                memberCount = 5,
                activeProposals = emptyList()
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${getGreetingMessage()}, ${currentUser.name}!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Portfolio Value Card
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
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Ksh ${String.format("%.2f", balance)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
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
                        onClick = { onNavigateToGroupHub() }
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

@Preview(showBackground = true)
@Composable
fun PreviewHomeDashboard() {
    ShareStackTheme {
        HomeDashboardScreen()
    }
}
