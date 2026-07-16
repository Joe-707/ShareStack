package com.sharestack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions          // ✅ ADD THIS
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType              // ✅ ADD THIS
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sharestack.models.Proposal
import com.sharestack.ui.theme.ShareStackTheme
import com.sharestack.viewmodel.ShareStackViewModel
import java.text.NumberFormat
import java.util.Locale

private fun formatUSD(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(amount)
}
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
    viewModel: ShareStackViewModel = viewModel(),
    proposalId: String = "p1",
    onNavigateBack: () -> Unit = {},
    onConfirmRedistribution: (Map<String, Double>) -> Unit = {},
    onVoteNo: () -> Unit = {}
) {
    // UI State
    var showRedistributionPopup by remember { mutableStateOf(false) }
    var isLegalAgreed by remember { mutableStateOf(false) }

    // ✅ Get the REAL proposal from the ViewModel (or use dummy data)
    val activePitch = viewModel.getProposalById(proposalId) ?: Proposal(
        id = "p1",
        stockTarget = "Nvidia (NVDA)",
        targetAmount = 17000.0,
        activeMembers = listOf("Joe", "Austin") // Sarah already voted No!
    )

    val stockTarget = activePitch.stockTarget
    val targetAmount = activePitch.targetAmount
    val remainingMembers = activePitch.activeMembers

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
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(all = 24.dp)
            ) {
                Text(
                    "Active Proposal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "Buy ${formatUSD(targetAmount)} of $stockTarget",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // VISUAL CUE: The Shortfall Warning
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Notice: A member opted out. Remaining members must cover the shortfall if approved.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(all = 12.dp)
                    )
                }

                // THE LEGAL CHECKBOX
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isLegalAgreed,
                        onCheckedChange = { isLegalAgreed = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = "I agree to bind my funds to the group Co-Ownership terms for this transaction.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // VOTE YES BUTTON - Disabled until the box is checked!
                    Button(
                        onClick = { showRedistributionPopup = true },
                        enabled = isLegalAgreed,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Vote Yes")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // VOTE NO BUTTON
                    OutlinedButton(
                        onClick = onVoteNo,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Vote No")
                    }
                }
            }
        }

        // Redistribution Dialog
        if (showRedistributionPopup) {
            RedistributionDialog(
                targetAmount = targetAmount,
                remainingMembers = remainingMembers,
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
        ActiveProposalCard(proposalId="p1")
    }
}