package com.sharestack.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sharestack.models.Proposal
import com.sharestack.ui.theme.ShareStackTheme
import com.sharestack.viewmodel.ShareStackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    viewModel: ShareStackViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToProposal: (String) -> Unit = {},
    onNavigateToCreate: () -> Unit = {}
) {
    val stacks by viewModel.stacks.collectAsState()
    val stack = stacks.find { it.id == groupId }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(groupName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("←", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp), // Makes the FAB a rounded square instead of a perfect circle
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
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
            // SHARED VAULT CARD - Matching the 24dp roundness and 12dp shadow from the dashboard
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(all = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Shared Vault Balance", color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text(
                        "Ksh ${String.format("%.2f", sharedVaultBalance)}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Active Members (${memberCount})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                memberNames.forEach { memberName ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(memberName.first().toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Text(memberName, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp), fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Proposals", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val proposals = stack?.activeProposals ?: listOf(activePitch)
                items(proposals) { proposal ->
                    // PROPOSAL CARDS - Soft shadow and medium rounding
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {onNavigateToProposal(proposal.id)},
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(all = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Buy ${proposal.stockTarget}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Ksh ${proposal.targetAmount}", color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 4.dp))
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp), // Sharper, pill-like rounding for the tag
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                            ) {
                                Text(
                                    "Active",
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
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
    ShareStackTheme { GroupDetailScreen(groupId = "1") }
}