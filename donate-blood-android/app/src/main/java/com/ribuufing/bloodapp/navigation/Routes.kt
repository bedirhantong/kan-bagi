package com.ribuufing.bloodapp.navigation

sealed class Routes(val route: String) {
    object Welcome : Routes("welcome")
    object Signup : Routes("signup")
    object LoginType : Routes("login_type")
    object QrScan : Routes("qr_scan")
    object Map : Routes("map")
    object Home : Routes("home")
    object Profile : Routes("profile")
    object Settings : Routes("settings")
    object ChatList : Routes("chat_list")
    object SharePost : Routes("share_post")
    object PrivacyPolicy : Routes("privacy_policy")
    object FAQ : Routes("faq")
    object About : Routes("about")
    object Terms : Routes("terms")
    object Notifications : Routes("notifications")
    object Language : Routes("language")
    object Theme : Routes("theme")
}