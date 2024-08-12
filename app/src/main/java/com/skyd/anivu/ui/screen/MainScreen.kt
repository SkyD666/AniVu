package com.skyd.anivu.ui.screen

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.RssFeed
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.util.Consumer
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.skyd.anivu.R
import com.skyd.anivu.ext.activity
import com.skyd.anivu.ext.isCompact
import com.skyd.anivu.model.preference.appearance.NavigationBarLabelPreference
import com.skyd.anivu.ui.local.LocalMediaLibLocation
import com.skyd.anivu.ui.local.LocalNavController
import com.skyd.anivu.ui.local.LocalNavigationBarLabel
import com.skyd.anivu.ui.local.LocalWindowSizeClass
import com.skyd.anivu.ui.screen.download.openDownloadScreen
import com.skyd.anivu.ui.screen.feed.FEED_SCREEN_ROUTE
import com.skyd.anivu.ui.screen.feed.FeedScreen
import com.skyd.anivu.ui.screen.media.MEDIA_SCREEN_ROUTE
import com.skyd.anivu.ui.screen.media.MediaScreen
import com.skyd.anivu.ui.screen.more.MORE_SCREEN_ROUTE
import com.skyd.anivu.ui.screen.more.MoreScreen

const val MAIN_SCREEN_ROUTE = "mainScreen"

private fun handleIntent(intent: Intent?, navController: NavController) {
    intent ?: return
    val data = intent.data
    if (Intent.ACTION_VIEW == intent.action && data != null) {
        val scheme = data.scheme
        var url: String? = null
        when (scheme) {
            "anivu" -> navController.navigate(data)
            "magnet" -> url = data.toString()
            "http", "https" -> {
                val path = data.path
                if (path != null && path.endsWith(".torrent")) {
                    url = data.toString()
                }
            }
        }
        if (url != null) {
            openDownloadScreen(
                navController = navController,
                downloadLink = url,
            )
        }
    }
}

private var needHandleIntent by mutableStateOf(true)

@Composable
fun MainScreen() {
    val windowSizeClass = LocalWindowSizeClass.current
    val navController = LocalNavController.current
    val mainNavController = rememberNavController()
    val context = LocalContext.current

    val navigationBarOrRail: @Composable () -> Unit = @Composable {
        NavigationBarOrRail(navController = mainNavController)
    }

    if (needHandleIntent) {
        LaunchedEffect(Unit) {
            needHandleIntent = false
            handleIntent(intent = context.activity.intent, navController = navController)
        }
    }

    DisposableEffect(navController) {
        val listener = Consumer<Intent> { newIntent ->
            handleIntent(intent = newIntent, navController = navController)
        }
        (context.activity as ComponentActivity).addOnNewIntentListener(listener)
        onDispose { (context.activity as ComponentActivity).removeOnNewIntentListener(listener) }
    }

    Scaffold(
        bottomBar = {
            if (windowSizeClass.isCompact) {
                navigationBarOrRail()
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Row(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .run {
                    if (!windowSizeClass.isCompact) {
                        windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                    } else this
                },
        ) {
            if (!windowSizeClass.isCompact) {
                navigationBarOrRail()
            }
            NavHost(
                navController = mainNavController,
                startDestination = FEED_SCREEN_ROUTE,
                modifier = Modifier.weight(1f),
                enterTransition = { fadeIn(animationSpec = tween(170)) },
                exitTransition = { fadeOut(animationSpec = tween(170)) },
                popEnterTransition = { fadeIn(animationSpec = tween(170)) },
                popExitTransition = { fadeOut(animationSpec = tween(170)) },
            ) {
                composable(FEED_SCREEN_ROUTE) { FeedScreen() }
                composable(MEDIA_SCREEN_ROUTE) { MediaScreen(path = LocalMediaLibLocation.current) }
                composable(MORE_SCREEN_ROUTE) { MoreScreen() }
            }
        }
    }
}

@Composable
private fun NavigationBarOrRail(navController: NavController) {
    val items = listOf(
        stringResource(R.string.feed_screen_name) to FEED_SCREEN_ROUTE,
        stringResource(R.string.media_screen_name) to MEDIA_SCREEN_ROUTE,
        stringResource(R.string.more_screen_name) to MORE_SCREEN_ROUTE,
    )
    val icons = remember {
        mapOf(
            true to listOf(Icons.Filled.RssFeed, Icons.Filled.Movie, Icons.Filled.Widgets),
            false to listOf(Icons.Outlined.RssFeed, Icons.Outlined.Movie, Icons.Outlined.Widgets),
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = navBackStackEntry?.destination

    val onClick: (Int) -> Unit = { index ->
        navController.navigate(items[index].second) {
            // Pop up to the previous (?: start) destination of the graph to
            // avoid building up a large stack of destinations on the back stack as users select items
            popUpTo(
                id = navController.currentDestination?.id
                    ?: navController.graph.findStartDestination().id
            ) {
                saveState = true
                inclusive = true
            }
            // Avoid multiple copies of the same destination when reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }

    val navigationBarLabel = LocalNavigationBarLabel.current
    if (LocalWindowSizeClass.current.isCompact) {
        NavigationBar {
            items.forEachIndexed { index, item ->
                val selected = currentDest?.hierarchy?.any { it.route == item.second } == true
                NavigationBarItem(
                    icon = { Icon(icons[selected]!![index], contentDescription = item.first) },
                    label = if (navigationBarLabel == NavigationBarLabelPreference.NONE) null else {
                        { Text(item.first) }
                    },
                    alwaysShowLabel = navigationBarLabel == NavigationBarLabelPreference.SHOW,
                    selected = selected,
                    onClick = { onClick(index) }
                )
            }
        }
    } else {
        NavigationRail {
            items.forEachIndexed { index, item ->
                val selected = currentDest?.hierarchy?.any { it.route == item.second } == true
                NavigationRailItem(
                    icon = { Icon(icons[selected]!![index], contentDescription = item.first) },
                    label = if (navigationBarLabel == NavigationBarLabelPreference.NONE) null else {
                        { Text(item.first) }
                    },
                    alwaysShowLabel = navigationBarLabel == NavigationBarLabelPreference.SHOW,
                    selected = selected,
                    onClick = { onClick(index) }
                )
            }
        }
    }
}

@Composable
fun RequestStoragePermissionScreen(shouldShowRationale: Boolean, onPermissionRequest: () -> Unit) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(50.dp))

            Text(
                text = stringResource(R.string.storage_permission_request_screen_title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )

            Icon(
                modifier = Modifier
                    .padding(30.dp)
                    .size(110.dp),
                imageVector = Icons.Rounded.Storage,
                contentDescription = null,
            )

            val textToShow = if (shouldShowRationale) {
                // If the user has denied the permission but the rationale can be shown,
                // then gently explain why the app requires this permission
                stringResource(R.string.storage_permission_request_screen_rationale)
            } else {
                // If it's the first time the user lands on this feature, or the user
                // doesn't want to be asked again for this permission, explain that the
                // permission is required
                stringResource(R.string.storage_permission_request_screen_first_tip)
            }
            Text(
                text = textToShow,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 20.dp)
            )

            Button(
                modifier = Modifier.padding(vertical = 30.dp),
                onClick = onPermissionRequest,
            ) {
                Text(stringResource(R.string.storage_permission_request_screen_request_permission))
            }
        }
    }
}