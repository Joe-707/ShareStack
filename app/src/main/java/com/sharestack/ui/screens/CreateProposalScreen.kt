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
import com.sharestack.ui.theme.ShareStackTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProposalScreen(
    groupId: String,
    onNavigateBack: () -> Unit = {},
    onSubmit: (String, Double) -> Unit = { _, _ -> }
) {
    var stockTicker by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }

    // Validation: Amount must be a valid number AND at least Ksh 100
    val parsedAmount = targetAmount.toDoubleOrNull()
    val isAmountValid = parsedAmount != null && parsedAmount >= 100.0
    val isFormValid = stockTicker.isNotBlank() && isAmountValid

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pitch a Stock", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("←", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(all = 24.dp)
        ) {
            Text(
                text = "What should the group buy?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Select a stock ticker and the total amount of Ksh you want the group to pool together.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- DROPDOWN MENU IMPLEMENTATION ---
            var expanded by remember { mutableStateOf(false) }
            val availableStocks = listOf("NVDA (Nvidia)", "AAPL (Apple)", "MSFT (Microsoft)", "TSLA (Tesla)", "AMZN (Amazon)")

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = stockTicker,
                    onValueChange = {},
                    readOnly = true, // Prevents manual typing!
                    label = { Text("Target Stock") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    availableStocks.forEach { selection ->
                        DropdownMenuItem(
                            text = { Text(selection) },
                            onClick = {
                                stockTicker = selection.substringBefore(" ")
                                expanded = false
                            }
                        )
                    }
                }
            }
            // ------------------------------------

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Input
            OutlinedTextField(
                value = targetAmount,
                onValueChange = { targetAmount = it },
                label = { Text("Target Amount (Ksh)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = targetAmount.isNotEmpty() && !isAmountValid,
                singleLine = true
            )

            if (targetAmount.isNotEmpty() && !isAmountValid) {
                Text(
                    text = "Amount must be at least Ksh 100",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Submit Button
            Button(
                onClick = {
                    parsedAmount?.let {
                        onSubmit(stockTicker, it)
                    }
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Submit Pitch to Group", fontSize = 18.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCreateProposal() {
    ShareStackTheme {
        CreateProposalScreen(groupId = "1")
    }
}