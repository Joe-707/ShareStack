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
                        // LOGIN SCREEN
                        composable("login") {
                            LoginScreen(
                                onNavigateToRegister = {
                                    navController.navigate("register")
                                },
                                onNavigateToHome = {
                                    viewModel.login("demo@example.com", "password")
                                    navController.navigate("home")
                                }
                            )
                        }

                        // REGISTER SCREEN
                        composable("register") {
                            RegisterScreen(
                                onNavigateToLogin = {
                                    navController.navigate("login")
                                },
                                onNavigateToHome = {
                                    viewModel.signup("Demo User", "demo@example.com", "password")
                                    navController.navigate("home")
                                }
                            )
                        }

                        // HOME DASHBOARD
                        composable("home") {
                            HomeDashboardScreen(
                                onNavigateToGroupHub = {
                                    // Navigate to group detail with first group ID
                                    val firstGroupId = viewModel.stacks.value.firstOrNull()?.id ?: "1"
                                    navController.navigate("group-detail/$firstGroupId")
                                },
                                onNavigateToProposal = {
                                    // Navigate to active proposal card
                                    navController.navigate("proposal-detail")
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
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToProposal = {
                                    navController.navigate("proposal-detail")
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
                        composable("proposal-detail") {
                            ActiveProposalCard(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onConfirmRedistribution = { newSplit ->
                                    viewModel.redistributeFunds("p1", newSplit)
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