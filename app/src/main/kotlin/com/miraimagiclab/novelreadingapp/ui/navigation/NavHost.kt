package com.miraimagiclab.novelreadingapp.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.miraimagiclab.novelreadingapp.data.auth.TokenStorage
import com.miraimagiclab.novelreadingapp.ui.auth.authNavigation
import com.miraimagiclab.novelreadingapp.ui.book.bookNavigation
import com.miraimagiclab.novelreadingapp.ui.dialog.markAllChaptersAsReadDialog
import com.miraimagiclab.novelreadingapp.ui.dialog.updatesAvailableDialog
import com.miraimagiclab.novelreadingapp.ui.downloadmanager.downloadManager
import com.miraimagiclab.novelreadingapp.ui.home.homeNavigation
import com.miraimagiclab.novelreadingapp.ui.onboarding.OnboardingScreen
import com.miraimagiclab.novelreadingapp.ui.onboarding.OnboardingViewModel
import com.miraimagiclab.novelreadingapp.utils.LocalSnackbarHost
import com.miraimagiclab.novelreadingapp.utils.expandEnter
import com.miraimagiclab.novelreadingapp.utils.expandExit
import com.miraimagiclab.novelreadingapp.utils.expandPopEnter
import com.miraimagiclab.novelreadingapp.utils.expandPopExit
import io.lain4504.novelreadingapp.api.ui.LocalNavController

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun LightNovelReaderNavHost(
    navController: NavHostController,
    tokenStorage: TokenStorage,
    onBuildNavHost: NavGraphBuilder.() -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val startDestination = if (tokenStorage.hasCompletedOnboarding()) Route.Main else Route.Onboarding

    CompositionLocalProvider(
        LocalNavController provides navController,
        LocalSnackbarHost provides snackbarHostState
    ) {
        SharedTransitionLayout {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                enterTransition = { expandEnter() },
                exitTransition = { expandExit() },
                popEnterTransition = { expandPopEnter() },
                popExitTransition = { expandPopExit() }
            ) {
                composable<Route.Onboarding> {
                    val viewModel: OnboardingViewModel = hiltViewModel()
                    OnboardingScreen(
                        onGetStarted = {
                            viewModel.onGetStarted()
                            navController.navigate(Route.Main) {
                                popUpTo(Route.Onboarding) { inclusive = true }
                            }
                        }
                    )
                }
                homeNavigation(this@SharedTransitionLayout)
                bookNavigation()
                authNavigation()
                updatesAvailableDialog()
                downloadManager()
                markAllChaptersAsReadDialog()
                onBuildNavHost.invoke(this)
            }
        }
    }
}