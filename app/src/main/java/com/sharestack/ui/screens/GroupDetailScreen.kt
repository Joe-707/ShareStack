package com.sharestack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sharestack.models.Proposal
import com.sharestack.ui.theme.ShareStackTheme
import com.sharestack.viewModel.ShareStackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    viewModel: ShareStackViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToProposal: () -> Unit = {},
    onNavigateToCreate: () -> Unit = {}
) {
    // Get group data from ViewModel
    val stacks by viewModel.stacks.collectAsState()
    val currentPrices by viewModel.currentPrices.collectAsState()

    // Find the requested stack
    val stack = stacks.find { it.id == groupId }

    // Dummy data fallback if stack not found
    val activePitch = stack?.activeProposals?.firstOrNull() ?: Proposal(
        id = "p1",
        stockTarget = "Nvidia (NVDA)",
        targetAmount = 17000.0,
        activeMembers = listOf("Joe", "Austin", "Sarah")
    )

    val groupName = stack?.name ?: "Tech Giants Pool"
    val memberCount = stack?.members?.size ?: 3
    val memberNames = stack?.members?.map { it.name } ?: listOf("Austin", "Joe", "Sarah")
    val sharedVaultBalance = viewModel.balance.collectAsState().value
    val currentPrice = stack?.let {
        currentPrices[it.stockSymbol] ?: 0.0
    } ?: 1050.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(groupName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("←", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primary
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
            // Shared Vault Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(all = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Shared Vault Balance",
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "Ksh ${String.format("%.2f", sharedVaultBalance)}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Roster Section
            Text(
                "Active Members (${memberCount})",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Member avatars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                memberNames.forEach { memberName ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                memberName.first().toString(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            memberName,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Proposals List
            Text(
                "Proposals",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val proposals = stack?.activeProposals ?: listOf(activePitch)
                items(proposals) { proposal ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onNavigateToProposal
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Buy ${proposal.stockTarget}",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Ksh ${proposal.targetAmount}",
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                modifier = Modifier.padding(all = 4.dp)
                            ) {
                                Text(
                                    "Active",
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
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
fun PreviewGroupDetailScreen() {
    ShareStackTheme {
        GroupDetailScreen(groupId = "1")
    }
}