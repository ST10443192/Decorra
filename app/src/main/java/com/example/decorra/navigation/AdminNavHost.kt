package com.example.decorra.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.decorra.R
import com.example.decorra.ui.admin.AdminAlertsScreen
import com.example.decorra.ui.admin.AdminDashboardScreen
import com.example.decorra.ui.admin.AdminEventDetailScreen
import com.example.decorra.ui.admin.AdminEventsScreen
import com.example.decorra.ui.admin.AdminMessagesScreen
import com.example.decorra.ui.admin.AdminProfileScreen
import com.example.decorra.ui.admin.AdminQuoteDetailScreen
import com.example.decorra.ui.admin.AdminQuotesScreen
import com.example.decorra.ui.admin.AdminSectionScreen
import com.example.decorra.ui.admin.InventoryScreen
import com.example.decorra.ui.admin.ManageScreen
import com.example.decorra.ui.client.ChatScreen
import com.example.decorra.ui.client.SettingsScreen
import com.example.decorra.ui.theme.Espresso
import com.example.decorra.ui.theme.Sand
import androidx.compose.material.icons.outlined.Inventory

private data class AdminTab(
    val route: String,
    @StringRes val label: Int,
    val icon: ImageVector
)

private val adminTabs = listOf(
    AdminTab(Routes.HOME, R.string.nav_home, Icons.Outlined.Home),
    AdminTab(Routes.QUOTES, R.string.nav_quotes, Icons.Outlined.Description),
    AdminTab(Routes.EVENTS, R.string.nav_events, Icons.Outlined.Event),
    AdminTab(Routes.CHATS, R.string.nav_messages, Icons.Outlined.ChatBubbleOutline),
    AdminTab(Routes.PROFILE, R.string.nav_more, Icons.Outlined.Menu)
)

@Composable
fun AdminNavHost(onLogOut: () -> Unit) {
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    var openThreadId by rememberSaveable { mutableStateOf("") }
    var openThreadName by rememberSaveable { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { AdminBottomBar(currentRoute) { route -> nav.navigateTab(route) } }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                AdminDashboardScreen(
                    onRequests = { nav.navigateTab(Routes.QUOTES) },
                    onEvents = { nav.navigateTab(Routes.EVENTS) },
                    onMessages = { nav.navigateTab(Routes.CHATS) },
                    onQuote = { id -> nav.navigate(Routes.eventDetail(id)) },
                    onAlerts = { nav.navigate(Routes.ADMIN_ALERTS) },
                    onInventory = { nav.navigate(Routes.ADMIN_INVENTORY) }
                )
            }
            composable(Routes.ADMIN_ALERTS) {
                AdminAlertsScreen(
                    onBack = { nav.popBackStack() },
                    onInventory = { nav.navigate(Routes.ADMIN_INVENTORY) }
                )
            }
            composable(Routes.ADMIN_INVENTORY) {
                InventoryScreen(onBack = { nav.popBackStack() })
            }
            composable(Routes.QUOTES) {
                AdminQuotesScreen(onQuoteClick = { id -> nav.navigate(Routes.quoteDetail(id)) })
            }
            composable(
                route = Routes.QUOTE_DETAIL,
                arguments = listOf(navArgument(Routes.ARG_QUOTE_ID) { type = NavType.StringType })
            ) { entry ->
                AdminQuoteDetailScreen(
                    quoteId = entry.arguments?.getString(Routes.ARG_QUOTE_ID).orEmpty(),
                    onBack = { nav.popBackStack() }
                )
            }
            composable(Routes.EVENTS) {
                AdminEventsScreen(onEventClick = { id -> nav.navigate(Routes.eventDetail(id)) })
            }
            composable(
                route = Routes.EVENT_DETAIL,
                arguments = listOf(navArgument(Routes.ARG_QUOTE_ID) { type = NavType.StringType })
            ) { entry ->
                AdminEventDetailScreen(
                    quoteId = entry.arguments?.getString(Routes.ARG_QUOTE_ID).orEmpty(),
                    onBack = { nav.popBackStack() }
                )
            }
            composable(Routes.CHATS) {
                AdminMessagesScreen(onOpen = { thread ->
                    openThreadId = thread.clientUid
                    openThreadName = thread.clientName.ifBlank { thread.clientEmail }
                    nav.navigate(Routes.ADMIN_CHAT)
                })
            }
            composable(Routes.ADMIN_CHAT) {
                ChatScreen(
                    title = openThreadName,
                    threadId = openThreadId,
                    asClient = false,
                    onBack = { nav.popBackStack() }
                )
            }
            composable(Routes.PROFILE) {
                AdminProfileScreen(
                    onManage = { nav.navigate(Routes.MANAGE) },
                    onInventory = { nav.navigate(Routes.ADMIN_INVENTORY) },
                    onSection = { key -> nav.navigate(Routes.adminSection(key)) },
                    onSettings = { nav.navigate(Routes.SETTINGS) },
                    onLogOut = onLogOut
                )
            }
            composable(Routes.MANAGE) { ManageScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.SETTINGS) { SettingsScreen(onBack = { nav.popBackStack() }) }
            composable(
                route = Routes.ADMIN_SECTION,
                arguments = listOf(navArgument(Routes.ARG_SECTION) { type = NavType.StringType })
            ) { entry ->
                AdminSectionScreen(
                    sectionKey = entry.arguments?.getString(Routes.ARG_SECTION).orEmpty(),
                    onBack = { nav.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun AdminBottomBar(currentRoute: String?, onTab: (String) -> Unit) {
    val selected = when (currentRoute) {
        Routes.ADMIN_ALERTS -> Routes.HOME
        Routes.QUOTE_DETAIL -> Routes.QUOTES
        Routes.EVENT_DETAIL -> Routes.EVENTS
        Routes.ADMIN_CHAT -> Routes.CHATS
        Routes.ADMIN_INVENTORY, Routes.MANAGE, Routes.SETTINGS, Routes.ADMIN_SECTION -> Routes.PROFILE
        else -> currentRoute
    }
    NavigationBar(containerColor = Color.White) {
        adminTabs.forEach { tab ->
            NavigationBarItem(
                selected = selected == tab.route,
                onClick = { onTab(tab.route) },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = {
                    Text(
                        stringResource(tab.label),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Sand,
                    selectedIconColor = Espresso,
                    selectedTextColor = Espresso,
                    unselectedIconColor = Espresso.copy(alpha = 0.75f),
                    unselectedTextColor = Espresso.copy(alpha = 0.75f)
                )
            )
        }
    }
}

private fun NavController.navigateTab(route: String) = navigate(route) {
    popUpTo(graph.findStartDestination().id)
    launchSingleTop = true
}