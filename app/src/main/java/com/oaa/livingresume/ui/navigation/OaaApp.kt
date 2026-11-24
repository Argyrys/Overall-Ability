package com.oaa.livingresume.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.oaa.livingresume.audio.Sfx
import com.oaa.livingresume.ui.components.HudGridBackground
import com.oaa.livingresume.ui.motion.playSfx
import com.oaa.livingresume.ui.screens.auth.AuthLogo
import com.oaa.livingresume.ui.screens.auth.AuthScreen
import com.oaa.livingresume.ui.screens.batch.BatchScreen
import com.oaa.livingresume.ui.screens.home.HomeScreen
import com.oaa.livingresume.ui.screens.improve.ImproveScreen
import com.oaa.livingresume.ui.screens.share.ShareScreen
import com.oaa.livingresume.ui.screens.timeline.SemesterDetailScreen
import com.oaa.livingresume.ui.screens.timeline.TimelineScreen
import com.oaa.livingresume.ui.theme.CyanGlow
import com.oaa.livingresume.ui.theme.DeepNavy
import com.oaa.livingresume.ui.theme.NavySurface
import com.oaa.livingresume.ui.theme.OutlineFaint
import com.oaa.livingresume.ui.theme.TextSecondary
import com.oaa.livingresume.ui.theme.microHeader
import com.oaa.livingresume.viewmodel.OaaViewModel
import com.oaa.livingresume.viewmodel.OaaViewModelFactory

/** The 5 bottom-nav tabs. */
data class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    companion object {
        val home = Destination("home", "Home", Icons.Outlined.Home)
        val timeline = Destination("timeline", "Timeline", Icons.Outlined.Timeline)
        val improve = Destination("improve", "Improve", Icons.Outlined.Lightbulb)
        val batch = Destination("batch", "Batch", Icons.Outlined.Groups)
        val share = Destination("share", "Share", Icons.Outlined.QrCode2)

        val all = listOf(home, timeline, improve, batch, share)
    }
}

/**
 * App shell: mock auth gate -> one shared [OaaViewModel] (MVVM) +
 * bottom navigation bar with 5 tabs.
 */
@Composable
fun OaaApp(
    viewModel: OaaViewModel = viewModel(factory = OaaViewModelFactory.instance(LocalContext.current)),
    navController: NavHostController = rememberNavController(),
) {
    val state by viewModel.state.collectAsState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    // Whoosh when a session comes up (first launch or after sign-in).
    LaunchedEffect(state.signedIn) {
        if (state.signedIn) playSfx(Sfx.OPEN)
    }

    // ------------------------------------------------------------- auth gate
    if (!state.signedIn) {
        when {
            state.authLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DeepNavy),
                contentAlignment = Alignment.Center,
            ) {
                HudGridBackground(Modifier.fillMaxSize())
                AuthLogo(Modifier.padding(bottom = 0.dp))
            }

            else -> AuthScreen(
                authError = state.authError,
                authBusy = state.authBusy,
                onLogin = viewModel::login,
                onFieldChange = viewModel::clearAuthError,
            )
        }
        return
    }

    // ----------------------------------------------------------- signed in
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy),
    ) {
        HudGridBackground(Modifier.fillMaxSize())

        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            bottomBar = {
                HudBottomBar(currentRoute = currentRoute) { destination ->
                    if (currentRoute != destination.route) {
                        playSfx(Sfx.CLICK)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    navController.navigate(destination.route) {
                        popUpTo(Destination.home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
        ) { padding ->
        when {
            state.loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = CyanGlow)
            }

            state.error != null && state.me == null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text("Could not load OAA data: ${state.error}", color = TextSecondary)
            }

            else -> NavHost(
                navController = navController,
                startDestination = Destination.home.route,
                modifier = Modifier.padding(padding),
                enterTransition = {
                    androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(220)) +
                        androidx.compose.animation.slideInHorizontally(
                            animationSpec = androidx.compose.animation.core.tween(260),
                            initialOffsetX = { it / 5 },
                        )
                },
                exitTransition = {
                    androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(160))
                },
                popEnterTransition = {
                    androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(220)) +
                        androidx.compose.animation.slideInHorizontally(
                            animationSpec = androidx.compose.animation.core.tween(260),
                            initialOffsetX = { -it / 5 },
                        )
                },
                popExitTransition = {
                    androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(160))
                },
            ) {
                composable(Destination.home.route) { HomeScreen(viewModel) }
                composable(Destination.timeline.route) {
                    TimelineScreen(
                        viewModel = viewModel,
                        onSemesterClick = { number ->
                            navController.navigate("semester/$number")
                        },
                    )
                }
                composable(Destination.improve.route) { ImproveScreen(viewModel) }
                composable(Destination.batch.route) { BatchScreen(viewModel) }
                composable(Destination.share.route) { ShareScreen(viewModel) }
                composable("semester/{semesterNumber}") { backStackEntry ->
                    val number = backStackEntry.arguments?.getString("semesterNumber")?.toIntOrNull() ?: 1
                    SemesterDetailScreen(
                        viewModel = viewModel,
                        semesterNumber = number,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
        }
    }
}

/**
 * Custom cut-corner HUD bottom navigation: boxed icons on a cyan bracket
 * when selected, monospace micro-labels - the COT-E terminal bar.
 */
@Composable
private fun HudBottomBar(
    currentRoute: String?,
    onNavigate: (Destination) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavySurface)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        // Hairline top edge
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(OutlineFaint)
                .align(Alignment.TopStart),
        )
        Row(Modifier.fillMaxWidth()) {
            Destination.all.forEach { destination ->
                val selected = currentRoute == destination.route
                val shape = CutCornerShape(10.dp)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate(destination) }
                        .padding(top = 10.dp, bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .then(
                                if (selected) {
                                    Modifier
                                        .background(CyanGlow.copy(alpha = 0.16f), shape)
                                        .border(1.dp, CyanGlow.copy(alpha = 0.75f), shape)
                                } else {
                                    Modifier
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label,
                            tint = if (selected) CyanGlow else TextSecondary,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = destination.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall.microHeader(),
                        color = if (selected) CyanGlow else TextSecondary,
                    )
                }
            }
        }
    }
}
