package com.sharestack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharestack.models.StackMember

// Data class to track dynamic partner inputs
data class PartnerInput(val name: String = "", val share: String = "")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    currentUserName: String,
    onNavigateBack: () -> Unit = {},
    onSubmit: (String, String, List<StackMember>) -> Unit = { _, _, _ -> }
) {
    var groupName by remember { mutableStateOf("") }
    var stockTicker by remember { mutableStateOf("") }

    // Dynamic list that can hold up to 6 partners
    val partners = remember { mutableStateListOf<PartnerInput>() }

    // Real-time Math Engine: dynamically calculates the remaining share
    val partnerShareSum = partners.sumOf { it.share.toIntOrNull() ?: 0 }
    val myShare = 100 - partnerShareSum

    val hasPartners = partners.isNotEmpty()

    // Checks that all partner fields have names, and their share is greater than 0
    val isPartnersValid = partners.all {
        it.name.trim().length >= 2 && it.share.isNotBlank() && (it.share.toIntOrNull() ?: 0) > 0
    }

    val isFormValid = groupName.trim().length >= 3 &&
            stockTicker.isNotBlank() &&
            isPartnersValid &&
            myShare >= 0 &&
            partnerShareSum <= 100 &&
            (!hasPartners || myShare < 100) // If you added a partner, you CANNOT own 100%!

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Stack", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("←", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        // Using LazyColumn so the screen scrolls nicely if 6 partners are added
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            item {
                Text(
                    text = "Start a Co-Investment Pool",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group Name (e.g., Strathmore Tech Fund)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                var expanded by remember { mutableStateOf(false) }
                val availableStocks = listOf("NVDA (Nvidia)", "AAPL (Apple)", "MSFT (Microsoft)", "TSLA (Tesla)", "AMZN (Amazon)")

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = stockTicker,
                        onValueChange = {},
                        readOnly = true,
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

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Co-Owners (Max 6)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    if (partners.size < 6) {
                        TextButton(onClick = { partners.add(PartnerInput()) }) {
                            Text("+ Add Partner")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Dynamically renders the text boxes for each partner added
            itemsIndexed(partners) { index, partner ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = partner.name,
                        onValueChange = { partners[index] = partner.copy(name = it) },
                        label = { Text("Name") },
                        modifier = Modifier.weight(2f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = partner.share,
                        onValueChange = { partners[index] = partner.copy(share = it) },
                        label = { Text("%") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(onClick = { partners.removeAt(index) }) {
                        Text("X", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Ownership Split", fontWeight = FontWeight.Bold)
                        Text(
                            "Your Share ($currentUserName): $myShare%",
                            color = if (myShare < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        if (myShare < 0) {
                            Text("Shares cannot exceed 100%", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        val dynamicMembers = mutableListOf(StackMember(currentUserName, myShare))
                        partners.forEach {
                            dynamicMembers.add(StackMember(it.name, it.share.toIntOrNull() ?: 0))
                        }
                        onSubmit(groupName, stockTicker, dynamicMembers)
                    },
                    enabled = isFormValid,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Create Stack", fontSize = 18.sp)
                }
            }
        }
    }
}