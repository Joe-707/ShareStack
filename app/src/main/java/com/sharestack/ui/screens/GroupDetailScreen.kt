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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sharestack.viewmodel.ShareStackViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val investmentGroups by viewModel.investmentGroups.collectAsState()
    val personalGroupData = investmentGroups.find { it.id == groupId }

    val groupName = stack?.name ?: "Loading Group..."
    val memberCount = stack?.members?.size ?: 0
    val members = stack?.members ?: emptyList()
    val myVaultBalance = personalGroupData?.totalValue ?: 0.0

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Active Proposals", "Ledger")

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
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("+", fontSize = 24.sp, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(all = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Your Portfolio Balance", color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text(formatCurrency(myVaultBalance), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Equity Split (${memberCount})", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                members.forEach { member ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Text(member.name.first().toString().uppercase(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Text(member.name, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp), fontWeight = FontWeight.Bold)
                        Text("${member.ownershipPercentage}%", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TabRow(selectedTabIndex = selectedTabIndex, containerColor = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.primary) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTabIndex == index, onClick = { selectedTabIndex = index }, text = { Text(title, fontWeight = FontWeight.Bold) })
                }
            }

            if (selectedTabIndex == 0) {
                LazyColumn(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(stack?.activeProposals ?: emptyList()) { proposal ->
                        Card(modifier = Modifier.fillMaxWidth(), onClick = { onNavigateToProposal(proposal.id) }) {
                            Row(modifier = Modifier.fillMaxWidth().padding(all = 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Buy ${proposal.stockTarget}", fontWeight = FontWeight.Bold)
                                    Text(formatCurrency(proposal.targetAmount), color = MaterialTheme.colorScheme.secondary)
                                }
                                Text("Active", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(stack?.ledger?.reversed() ?: emptyList()) { entry ->
                        val date = SimpleDateFormat("MMM dd, HH:mm", Locale.US).format(Date(entry.timestamp))
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(entry.description, fontWeight = FontWeight.Bold)
                                    Text(formatCurrency(entry.totalAmount), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Text(date, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                entry.contributions.entries.forEach { (name, amt) ->
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(name, fontSize = 14.sp)
                                        Text(formatCurrency(amt), fontSize = 14.sp)
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