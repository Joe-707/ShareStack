package com.sharestack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sharestack.ui.screens.*
import com.sharestack.ui.theme.ShareStackTheme
import com.sharestack.viewmodel.ShareStackViewModel
import android.widget.Toast
import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShareStackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: ShareStackViewModel = viewModel()

                    NavHost(
                        navController = navController,
                        startDestination = "login"
                    ) {
                        // ========== LOGIN SCREEN ==========
                        composable("login") {
                            LoginScreen(
                                onNavigateToRegister = {
                                    navController.navigate("register")
                                },
                                onLoginSubmit = { email, password, onResult ->
                                    CoroutineScope(Dispatchers.Main).launch {
                                        val statusMessage = viewModel.login(email, password)

                                        if (statusMessage == "Success") {
                                            onResult(null) // No errors!
                                            navController.navigate("home") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        } else {
                                            // Send the error message back
                                            onResult(statusMessage)
                                        }
                                    }
                                }
                            )
                        }

                        // ========== REGISTER SCREEN ==========
                        composable("register") {
                            RegisterScreen(
                                onNavigateToLogin = {
                                    navController.navigate("login")
                                },
                                onNavigateToHome = { name, email, password ->
                                    CoroutineScope(Dispatchers.Main).launch {
                                        val success = viewModel.register(name, email, password)
                                        if (success) {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Registration successful! Welcome $name",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            navController.navigate("home") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        } else {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Registration failed: Email already exists",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            )
                        }

                        // ========== HOME DASHBOARD ==========
                        composable("home") {
                            HomeDashboardScreen(
                                viewModel = viewModel,
                                onNavigateToGroupHub = { clickedGroupId ->
                                    navController.navigate("group-detail/$clickedGroupId")
                                },
                                onNavigateToProposal = {
                                    navController.navigate("proposal-detail/p1")
                                },
                                onNavigateToCreateGroup = {
                                    navController.navigate("create-group")
                                },
                                onLogout = {
                                    viewModel.logout()
                                    navController.navigate("login") {
                                        popUpTo(0)
                                    }
                                }
                            )
                        }

                        // ========== CREATE GROUP ==========
                        composable("create-group") {
                            // Extract the current user's name to pass to the group
                            val currentUser by viewModel.currentUser.collectAsState()

                            CreateGroupScreen(
                                currentUserName = currentUser?.name ?: "User",
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onSubmit = { name, ticker, members ->
                                    // Launch a background coroutine to talk to Supabase
                                    CoroutineScope(Dispatchers.Main).launch {
                                        val success = viewModel.createNewStack(name, ticker, members)
                                        if (success) {
                                            navController.popBackStack() // Go back to dashboard on success
                                        } else {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Failed to create group",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            )
                        }

                        // ========== GROUP DETAIL ==========
                        composable(
                            route = "group-detail/{groupId}",
                            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val groupId = backStackEntry.arguments?.getString("groupId") ?: "1"
                            GroupDetailScreen(
                                groupId = groupId,
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToProposal = { clickedProposalId ->
                                    navController.navigate("proposal-detail/$clickedProposalId")
                                },
                                onNavigateToCreate = {
                                    navController.navigate("create-proposal/$groupId")
                                }
                            )
                        }

                        // ========== CREATE PROPOSAL ==========
                        composable(
                            route = "create-proposal/{groupId}",
                            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val groupId = backStackEntry.arguments?.getString("groupId") ?: "1"
                            CreateProposalScreen(
                                groupId = groupId,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onSubmit = { stockTicker, targetAmount ->
                                    viewModel.createProposal(groupId, stockTicker, targetAmount)
                                    navController.popBackStack()
                                }
                            )
                        }
                        // ========== PROPOSAL DETAIL ==========
                        composable(
                            route = "proposal-detail/{proposalId}",
                            arguments = listOf(navArgument("proposalId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val proposalId = backStackEntry.arguments?.getString("proposalId") ?: "p1"

                            ActiveProposalCard(
                                proposalId = proposalId,
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onVoteNo = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}