package com.example.decorra.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.decorra.R
import com.example.decorra.data.QuoteDraftRepository
import com.example.decorra.ui.client.ChatScreen
import com.example.decorra.ui.client.CustomizeScreen
import com.example.decorra.ui.client.EventDateScreen
import com.example.decorra.ui.client.HomeScreen
import com.example.decorra.ui.client.NotificationsScreen
import com.example.decorra.ui.client.PortfolioScreen
import com.example.decorra.ui.client.ProfileScreen
import com.example.decorra.ui.client.QuoteBreakdownScreen
import com.example.decorra.ui.client.QuoteDetailScreen
import com.example.decorra.ui.client.QuotesScreen
import com.example.decorra.ui.client.SectionScreen
import com.example.decorra.ui.client.SettingsScreen
import com.example.decorra.ui.client.ThemeDetailScreen
import com.example.decorra.ui.theme.Espresso
import com.example.decorra.ui.theme.Sand
import com.example.decorra.data.UserSession
import com.example.decorra.data.AccountRepository

private data class TabItem(
    val route: String,
    @StringRes val label: Int,
    val icon: ImageVector
)

private val tabs = listOf(
    TabItem(Routes.HOME, R.string.nav_home, Icons.Outlined.Home),
    TabItem(Routes.QUOTES, R.string.nav_quotes, Icons.Outlined.Description),
    TabItem(Routes.CHATS, R.string.nav_chats, Icons.Outlined.ChatBubbleOutline),
    TabItem(Routes.PROFILE, R.string.nav_profile, Icons.Outlined.PersonOutline)
)

@Composable
fun ClientNavHost(onLogOut: () -> Unit) {
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            ClientBottomBar(currentRoute) { route -> nav.navigateTab(route) }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onBrowsePortfolio = { nav.navigate(Routes.PORTFOLIO) },
                    onQuotes = { nav.navigateTab(Routes.QUOTES) },
                    onMyDesigns = { nav.navigate(Routes.section(Routes.SECTION_DESIGNS)) },
                    onMessageDecorator = { nav.navigateTab(Routes.CHATS) },
                    onProfile = { nav.navigateTab(Routes.PROFILE) },
                    onNotifications = { nav.navigate(Routes.NOTIFICATIONS) }
                )
            }
            composable(Routes.QUOTES) {
                QuotesScreen(
                    onQuoteClick = { id -> nav.navigate(Routes.quoteDetail(id)) },
                    onBrowsePortfolio = { nav.navigate(Routes.PORTFOLIO) }
                )
            }
            composable(
                route = Routes.QUOTE_DETAIL,
                arguments = listOf(navArgument(Routes.ARG_QUOTE_ID) { type = NavType.StringType })
            ) { entry ->
                QuoteDetailScreen(
                    quoteId = entry.arguments?.getString(Routes.ARG_QUOTE_ID).orEmpty(),
                    onBack = { nav.popBackStack() }
                )
            }
            composable(Routes.CHATS) {
                ChatScreen(
                    title = stringResource(R.string.chat_title),
                    threadId = AccountRepository.currentUid().orEmpty(),
                    asClient = true
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onSection = { key -> nav.navigate(Routes.section(key)) },
                    onNotifications = { nav.navigate(Routes.NOTIFICATIONS) },
                    onSettings = { nav.navigate(Routes.SETTINGS) },
                    onLogOut = onLogOut
                )
            }
            composable(Routes.NOTIFICATIONS) {
                NotificationsScreen(onBack = { nav.popBackStack() })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { nav.popBackStack() })
            }
            composable(
                route = Routes.SECTION,
                arguments = listOf(navArgument(Routes.ARG_SECTION) { type = NavType.StringType })
            ) { entry ->
                SectionScreen(
                    sectionKey = entry.arguments?.getString(Routes.ARG_SECTION).orEmpty(),
                    onBack = { nav.popBackStack() },
                    onQuoteClick = { id -> nav.navigate(Routes.quoteDetail(id)) }
                )
            }
            composable(Routes.PORTFOLIO) {
                PortfolioScreen(
                    onBack = { nav.popBackStack() },
                    onThemeClick = { id -> nav.navigate(Routes.themeDetail(id)) },
                    onRentContinue = { nav.navigate(Routes.EVENT_DATE) }
                )
            }
            composable(
                route = Routes.THEME_DETAIL,
                arguments = listOf(navArgument(Routes.ARG_THEME_ID) { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString(Routes.ARG_THEME_ID).orEmpty()
                ThemeDetailScreen(
                    themeId = id,
                    onBack = { nav.popBackStack() },
                    onCustomize = { themeId ->
                        QuoteDraftRepository.start(themeId)
                        nav.navigate(Routes.CUSTOMIZE)
                    }
                )
            }
            composable(Routes.CUSTOMIZE) {
                CustomizeScreen(
                    onBack = { nav.popBackStack() },
                    onContinue = { nav.navigate(Routes.EVENT_DATE) }
                )
            }
            composable(Routes.EVENT_DATE) {
                EventDateScreen(
                    onBack = { nav.popBackStack() },
                    onContinue = { nav.navigate(Routes.BREAKDOWN) }
                )
            }
            composable(Routes.BREAKDOWN) {
                QuoteBreakdownScreen(
                    onBack = { nav.popBackStack() },
                    onDone = { nav.navigateTab(Routes.QUOTES) }
                )
            }
        }
    }
}

@Composable
private fun ClientBottomBar(currentRoute: String?, onTab: (String) -> Unit) {
    val selected = when (currentRoute) {
        Routes.PORTFOLIO, Routes.THEME_DETAIL, Routes.CUSTOMIZE,
        Routes.EVENT_DATE, Routes.BREAKDOWN, Routes.NOTIFICATIONS -> Routes.HOME
        Routes.QUOTE_DETAIL -> Routes.QUOTES
        Routes.SETTINGS, Routes.SECTION -> Routes.PROFILE
        else -> currentRoute
    }
    NavigationBar(containerColor = Color.White) {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = selected == tab.route,
                onClick = { onTab(tab.route) },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(stringResource(tab.label)) },
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