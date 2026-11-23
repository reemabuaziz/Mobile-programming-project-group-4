package com.group04.materialexchange.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.group04.materialexchange.platform.ExchangeSystemReceiver
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.group04.materialexchange.ui.components.Glyph
import com.group04.materialexchange.ui.screens.*
import com.group04.materialexchange.ui.theme.*
import com.group04.materialexchange.viewmodel.ExchangeViewModel

@Composable
fun MaterialExchangeApp(vm: ExchangeViewModel = viewModel()) {
    val data by vm.data.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    var systemEvent by remember { mutableStateOf<String?>(null) }

    // Lecture 12: register a short-lived system BroadcastReceiver with DisposableEffect.
    DisposableEffect(context) {
        val receiver = ExchangeSystemReceiver { message -> systemEvent = message }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_BATTERY_OKAY)
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose { context.unregisterReceiver(receiver) }
    }

    LaunchedEffect(systemEvent) {
        systemEvent?.let { snackbar.showSnackbar(it); systemEvent = null }
    }
    val navController = rememberNavController()
    val startDestination = remember { if (data.student == null) Routes.SignIn else Routes.Home }

    LaunchedEffect(error) {
        error?.let {
            val result = snackbar.showSnackbar(it, actionLabel = "Retry", duration = SnackbarDuration.Long)
            vm.clearError()
            if (result == SnackbarResult.ActionPerformed) vm.retry()
        }
    }

    LaunchedEffect(data.student?.id) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (data.student == null && currentRoute != null && currentRoute != Routes.SignIn && currentRoute != Routes.CreateAccount) {
            navController.navigate(Routes.SignIn) {
                popUpTo(Routes.Home) { inclusive = true }
                launchSingleTop = true
            }
        } else if (data.student != null && (currentRoute == Routes.SignIn || currentRoute == Routes.CreateAccount)) {
            navController.navigate(Routes.Home) {
                popUpTo(Routes.SignIn) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route.orEmpty()
    val rootRoutes = setOf(Routes.Home, Routes.MyActivity, Routes.Notifications, Routes.Profile)
    val showBottomBar = route in rootRoutes

    Scaffold(
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Blush, tonalElevation = 0.dp) {
                    val items = listOf(
                        Triple(Routes.Home, "Home", "home"),
                        Triple(Routes.myActivity(), "Activity", "activity"),
                        Triple(Routes.Notifications, "Notifications", "bell"),
                        Triple(Routes.Profile, "Profile", "user")
                    )
                    items.forEachIndexed { index, item ->
                        val selected = when (index) {
                            0 -> route == Routes.Home
                            1 -> route.startsWith("activity")
                            2 -> route == Routes.Notifications
                            else -> route == Routes.Profile
                        }
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.first) {
                                    popUpTo(Routes.Home)
                                    launchSingleTop = true
                                }
                            },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (index == 2 && data.notices.any { it.userId == data.student?.id && !it.read }) {
                                            Badge(containerColor = ReuseGreen)
                                        }
                                    }
                                ) {
                                    Glyph(item.third, color = if (selected) ReuseGreen else Color.Black)
                                }
                            },
                            label = { Text(item.second, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ReuseGreen,
                                selectedTextColor = ReuseGreen,
                                unselectedIconColor = Color.Black,
                                unselectedTextColor = Color.Black,
                                indicatorColor = MutedRose
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (route == Routes.Home) {
                FloatingActionButton(
                    onClick = { navController.navigate(Routes.CreateListing) },
                    modifier = Modifier.semantics { contentDescription = "Create Listing" },
                    containerColor = DustyRose,
                    contentColor = Color.Black
                ) { Glyph("plus") }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            NavHost(navController = navController, startDestination = startDestination) {
                composable(Routes.SignIn) {
                    SignInScreen(busy, vm) { navController.navigate(Routes.CreateAccount) }
                }
                composable(Routes.CreateAccount) {
                    CreateAccountScreen(busy, vm) { navController.popBackStack(Routes.SignIn, inclusive = false) }
                }
                composable(Routes.Home) {
                    HomeFeedScreen(data) { navController.navigate(Routes.itemDetails(it)) }
                }
                composable(
                    Routes.ItemDetails,
                    arguments = listOf(navArgument("itemId") { type = NavType.StringType })
                ) { entry ->
                    ItemDetailsScreen(
                        itemId = entry.arguments?.getString("itemId").orEmpty(),
                        data = data,
                        busy = busy,
                        onRequest = { itemId, onSuccess -> vm.request(itemId, onSuccess) },
                        back = { navController.popBackStack() },
                        activity = { navController.navigate(Routes.myActivity("Sent Requests")) { popUpTo(Routes.Home); launchSingleTop = true } }
                    )
                }
                composable(Routes.CreateListing) {
                    CreateListingScreen(
                        data = data,
                        busy = busy,
                        vm = vm,
                        back = { navController.popBackStack() },
                        done = { navController.navigate(Routes.myActivity()) { popUpTo(Routes.Home); launchSingleTop = true } }
                    )
                }
                composable(
                    Routes.EditListing,
                    arguments = listOf(navArgument("itemId") { type = NavType.StringType })
                ) { entry ->
                    EditListingScreen(
                        itemId = entry.arguments?.getString("itemId").orEmpty(),
                        data = data,
                        busy = busy,
                        vm = vm,
                        back = { navController.popBackStack() },
                        done = { navController.popBackStack() }
                    )
                }
                composable(
                    Routes.MyActivity,
                    arguments = listOf(navArgument("tab") { type = NavType.StringType; defaultValue = "My Listings" })
                ) { entry ->
                    MyActivityScreen(
                        data = data,
                        busy = busy,
                        vm = vm,
                        initialTab = entry.arguments?.getString("tab") ?: "My Listings",
                        edit = { navController.navigate(Routes.editListing(it)) },
                        openItem = { navController.navigate(Routes.itemDetails(it)) },
                        add = { navController.navigate(Routes.CreateListing) }
                    )
                }
                composable(Routes.Notifications) { NotificationsScreen(data, vm) }
                composable(Routes.Profile) { ProfileScreen(data, busy, vm) }
            }
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        }
    }
}
