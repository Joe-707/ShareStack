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
                                onNavigateToHome = { email, password ->
                                    // ✅ Use a coroutine to handle the suspend function
                                    CoroutineScope(Dispatchers.Main).launch {
                                        // Show loading in LoginScreen (handled internally)
                                        val success = viewModel.login(email, password)
                                        if (success) {
                                            navController.navigate("home") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        } else {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Login failed: Invalid email or password",
                                                Toast.LENGTH_SHORT
                                            ).show()
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
                                onLogout = {
                                    viewModel.logout()
                                    navController.navigate("login") {
                                        popUpTo(0)
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
                                onConfirmRedistribution = { newSplit ->
                                    viewModel.redistributeFunds(proposalId, newSplit)
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