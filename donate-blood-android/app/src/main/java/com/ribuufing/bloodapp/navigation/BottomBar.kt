package com.ribuufing.bloodapp.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomBar(
    navController: NavHostController,
    state: Boolean,
) {
    val screens = listOf(
        BottomNavigationItems.Home,
//        BottomNavigationItems.Search,
        BottomNavigationItems.SharePost,
        BottomNavigationItems.MapItem,
        BottomNavigationItems.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier.height(65.dp) // Instagram-style compact height
    ) {
        screens.forEachIndexed { index, screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(
                            id = if (currentRoute == screen.route)
                                screen.selectedIcon
                            else
                                screen.unselectedIcon
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
//                label = {
//                    Text(
//                        text = screen.title,
//                        style = MaterialTheme.typography.labelSmall
//                    )
//                },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFFF9E9E),
                    selectedTextColor = Color(0xFFFF9FCD),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.padding(0.dp)
            )
        }
    }
}