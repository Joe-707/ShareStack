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
import com.sharestack.viewModel.ShareStackViewModel
import android.widget.Toast

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
                                    val success = viewModel.login(email, password)
                                    if (success) {
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    } else {
                                        // Show Toast message for failed login
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Login failed: Invalid email or password",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                        }

                        // REGISTER SCREEN
                        composable("register") {
                            RegisterScreen(
                                onNavigateToLogin = {
                                    navController.navigate("login")
                                },
                                onNavigateToHome = { name, email, password ->
                                    // ✅ Call register (was signup)
                                    val success = viewModel.register(name, email, password)
                                    if (success) {
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                        // HOME DASHBOARD
                        composable("home") {
                            HomeDashboardScreen(
                                viewModel = viewModel,
                                onNavigateToGroupHub = { clickedGroupId ->
                                    // Navigates to the specific group card clicked
                                    navController.navigate("group-detail/$clickedGroupId")
                                },
                                onNavigateToProposal = {
                                    navController.navigate("proposal-detail/p1")
                                },

                                onLogout = {
                                    viewModel.logout()
                                    // Clears the backstack so they can't hit 'back' to enter the app again
                                    navController.navigate("login") {
                                        popUpTo(0)
                                    }
                                }

                            )
                        }

                        // GROUP DETAIL
                        composable(
                            route = "group-detail/{groupId}",
                            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val groupId = backStackEntry.arguments?.getString("groupId") ?: "1"
                            GroupDetailScreen(
                                groupId = groupId,
                                viewModel=viewModel,
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

                        // CREATE PROPOSAL
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

                        // PROPOSAL DETAIL (Active Proposal Card)
                        composable(
                            route="proposal-detail/{proposalId}",
                            arguments = listOf(navArgument("proposalId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val proposalId = backStackEntry.arguments?.getString("proposalId") ?: "p1"

                            ActiveProposalCard(
                                proposalId = proposalId,
                                viewModel= viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onConfirmRedistribution = { newSplit ->
                                    viewModel.redistributeFunds(proposalId, newSplit)
                                    navController.popBackStack()
                                },
                                onVoteNo = {
                                    // They opted out, so we just send them back to the dashboard!
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