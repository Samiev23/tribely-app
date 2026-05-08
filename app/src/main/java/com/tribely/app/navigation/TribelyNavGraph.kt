package com.tribely.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tribely.app.feature.group.GroupSelectScreen
import com.tribely.app.feature.main.MainTabsScreen
import com.tribely.app.ui.screens.SplashScreen
import com.tribely.app.ui.screens.auth.AuthScreen
import com.tribely.app.ui.screens.onboarding.OnboardingScreen

@Composable
fun TribelyNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = TribelyDestinations.SPLASH
    ) {
        composable(TribelyDestinations.SPLASH) {
            SplashScreen(
                onSplashFinished = { destination ->
                    navController.navigate(destination) {
                        popUpTo(TribelyDestinations.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(TribelyDestinations.WELCOME) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(TribelyDestinations.LOGIN) {
                        popUpTo(TribelyDestinations.WELCOME) { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate(TribelyDestinations.LOGIN) {
                        popUpTo(TribelyDestinations.WELCOME) { inclusive = true }
                    }
                },
                onClose = {
                    navController.popBackStack()
                }
            )
        }

        composable(TribelyDestinations.LOGIN) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(TribelyDestinations.GROUP_SELECT) {
                        popUpTo(TribelyDestinations.LOGIN) { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(TribelyDestinations.GROUP_SELECT) {
            GroupSelectScreen(
                onGroupReady = {
                    navController.navigate(TribelyDestinations.MAIN) {
                        popUpTo(TribelyDestinations.GROUP_SELECT) { inclusive = true }
                    }
                }
            )
        }

        composable(TribelyDestinations.MAIN) {
            MainTabsScreen(
                onLoggedOut = {
                    navController.navigate(TribelyDestinations.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
