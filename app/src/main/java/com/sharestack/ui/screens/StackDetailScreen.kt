package com.sharestack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharestack.models.Proposal
import com.sharestack.ui.theme.ShareStackTheme

@Composable
fun RedistributionDialog(
    targetAmount: Double,
    remainingMembers: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (Map<String, Double>) -> Unit
) {
    // Keep track of what each person types in
    var contributions by remember {
        mutableStateOf(remainingMembers.associateWith { 0.0 })
    }

    val currentTotal = contributions.values.sum()
    val isBalanced = currentTotal == targetAmount

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cover the Shortfall") },
        text = {
            Column {
                Text(
                    text = "A member opted out. The remaining members must cover the total target of Ksh $targetAmount.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Generate an input field for each remaining member
                remainingMembers.forEach { member ->
                    OutlinedTextField(
                        value = contributions[member]?.toString() ?: "",
                        onValueChange = { newValue ->
                            val parsed = newValue.toDoubleOrNull() ?: 0.0
                            contributions = contributions.toMutableMap().apply {
                                put(member, parsed)
                            }
                        },
                        label = { Text("${member}'s new amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Show if they are short or over the target
                val color = if (isBalanced) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                Text(
                    text = "Current Total: Ksh $currentTotal / $targetAmount",
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(contributions) },
                enabled = isBalanced
            ) {
                Text("Confirm Split")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel Vote")
            }
        }
    )
}

@Composable
fun ActiveProposalCard(
    onNavigateBack: () -> Unit = {},
    onConfirmRedistribution: (Map<String, Double>) -> Unit = {}
) {
    // State to control when the popup shows
    var showRedistributionPopup by remember { mutableStateOf(false) }

    // Instantiate the Data Class directly
    val activePitch = Proposal(
        id = "p1",
        stockTarget = "Nvidia (NVDA)",
        targetAmount = 17000.0,
        activeMembers = listOf("Joe", "Austin", "Sarah")
    )

    // Mapping variables directly to the object properties
    val stockTarget = activePitch.stockTarget
    val targetAmount = activePitch.targetAmount
    val activeMembers = activePitch.activeMembers

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        // Back Button
        TextButton(
            onClick = onNavigateBack,
            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
        ) {
            Text("Back to Dashboard")
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(all = 16.dp)
            ) {
                Text(
                    "Active Proposal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Buy Ksh $targetAmount of $stockTarget",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // VOTE YES BUTTON
                    Button(
                        onClick = { /* ToDo: Send Yes to backend */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Vote Yes")
                    }

                    // VOTE NO BUTTON (Triggers Redistribution)
                    OutlinedButton(
                        onClick = { showRedistributionPopup = true }
                    ) {
                        Text("Vote No")
                    }
                }
            }
        }

        // Connect the Dialog to the State
        if (showRedistributionPopup) {
            RedistributionDialog(
                targetAmount = targetAmount,
                remainingMembers = activeMembers,
                onDismiss = { showRedistributionPopup = false },
                onConfirm = { newSplit ->
                    onConfirmRedistribution(newSplit)
                    showRedistributionPopup = false
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewActiveProposalCard() {
    ShareStackTheme {
        ActiveProposalCard()
    }
}