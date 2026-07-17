package com.sharestack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sharestack.models.StackMember
import com.sharestack.ui.theme.ShareStackTheme
import com.sharestack.viewmodel.ShareStackViewModel

@Composable
fun ContributionSplitDialog(
    targetAmount: Double,
    stackMembers: List<StackMember>,
    onDismiss: () -> Unit,
    onConfirm: (Map<String, Double>) -> Unit
) {
    // Automatically calculates default contributions based on their legal ownership split
    val defaultContributions = stackMembers.associate { member ->
        member.name to (targetAmount * (member.ownershipPercentage / 100.0))
    }

    var contributions by remember { mutableStateOf(defaultContributions) }

    val currentTotal = contributions.values.sum()
    val remainingShortfall = targetAmount - currentTotal

    // VALIDATION: No negative numbers, and they cannot contribute MORE than the target.
    val hasNegative = contributions.values.any { it < 0.0 }
    val isMathPerfect = kotlin.math.abs(remainingShortfall) < 0.01 && !hasNegative


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Investment Split") },
        text = {
            Column {
                Text(
                    text = "Distribute the total funding target of ${formatCurrency(targetAmount)} among the members.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                stackMembers.forEach { member ->
                    OutlinedTextField(
                        value = contributions[member.name]?.let { String.format(java.util.Locale.US, "%.2f", it) } ?: "",
                        onValueChange = { newValue ->
                            val parsed = newValue.toDoubleOrNull() ?: 0.0
                            contributions = contributions.toMutableMap().apply {
                                put(member.name, parsed)
                            }
                        },
                        label = { Text("${member.name}'s Contribution (${member.ownershipPercentage}%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // SHORTFALL UI
                if (remainingShortfall > 0.01) {
                    Text(
                        text = "Shortfall: ${formatCurrency(remainingShortfall)}\nThe numbers must match to execute the purchase.",
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                } else if (remainingShortfall< -0.01) {
                    Text(
                        text = "Overfunded by ${formatCurrency(kotlin.math.abs(remainingShortfall))}",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                val color = if (isMathPerfect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                Text(
                    text = "Current Total: ${formatCurrency(currentTotal)} / ${formatCurrency(targetAmount)}",
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(contributions) },
                enabled = isMathPerfect
            ) {
                Text("Execute Purchase")
            }

        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ActiveProposalCard(
    viewModel: ShareStackViewModel = viewModel(),
    proposalId: String = "p1",
    onNavigateBack: () -> Unit = {},
    onVoteNo: () -> Unit = {}
) {
    var showContributionPopup by remember { mutableStateOf(false) }
    var isLegalAgreed by remember { mutableStateOf(false) }

    val activePitch = viewModel.getProposalById(proposalId)
    // Get the true ownership percentages
    val parentStack = viewModel.stacks.collectAsState().value.find { s -> s.activeProposals.any { it.id == proposalId } }

    if (activePitch == null || parentStack == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val stockTarget = activePitch.stockTarget
    val targetAmount = activePitch.targetAmount
    val trueGroupMembers = parentStack.members // The actual group members with their percentages!

    Column(
        modifier = Modifier.fillMaxSize().systemBarsPadding()
    ) {
        TextButton(onClick = onNavigateBack, modifier = Modifier.padding(start = 8.dp, top = 8.dp)) {
            Text("Back to Dashboard")
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(all = 24.dp)) {
                Text("Active Proposal", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(
                    text = "Buy ${formatCurrency(targetAmount)} of $stockTarget",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
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
                    Button(
                        onClick = { showContributionPopup = true },
                        enabled = isLegalAgreed,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) { Text("Vote Yes") }

                    Spacer(modifier = Modifier.width(16.dp))

                    OutlinedButton(
                        onClick = onVoteNo,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) { Text("Vote No") }
                }
            }
        }

        if (showContributionPopup) {
            ContributionSplitDialog(
                targetAmount = targetAmount,
                stackMembers = trueGroupMembers,
                onDismiss = { showContributionPopup = false },
                onConfirm = { newSplit ->
                    viewModel.approveAndExecuteProposal(proposalId, newSplit)
                    showContributionPopup = false
                    onNavigateBack()
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