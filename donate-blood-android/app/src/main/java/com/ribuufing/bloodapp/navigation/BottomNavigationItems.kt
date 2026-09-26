package com.ribuufing.bloodapp.navigation

import com.ribuufing.bloodapp.R
import okhttp3.Route

sealed class BottomNavigationItems(
    val route: String,
    val title: String,
    val selectedIcon: Int,
    val unselectedIcon: Int
) {
    object Home : BottomNavigationItems(
        route = Routes.Home.route,
        title = "Ana Sayfa",
        selectedIcon = R.drawable.home,
        unselectedIcon = R.drawable.home_light
    )

//    object Search : BottomNavigationItems(
//        route = "search",
//        title = "Search",
//        selectedIcon = R.drawable.search,
//        unselectedIcon = R.drawable.search
//    )

    object SharePost : BottomNavigationItems(
        route = Routes.SharePost.route,
        title = "İlan Oluştur",
        selectedIcon = R.drawable.add_light,
        unselectedIcon = R.drawable.add
    )

    object MapItem : BottomNavigationItems(
        route = Routes.Map.route,
        title = "Hastaneler",
        selectedIcon = R.drawable.map_logo,
        unselectedIcon = R.drawable.map_logo
    )

    object Profile : BottomNavigationItems(
        route = Routes.Profile.route,
        title = "Profilim",
        selectedIcon = R.drawable.profile_light,
        unselectedIcon = R.drawable.profile_dark
    )
}