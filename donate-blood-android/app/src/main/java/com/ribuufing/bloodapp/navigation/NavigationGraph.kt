package com.ribuufing.bloodapp.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.ribuufing.bloodapp.feature.authentication.presentation.SignupScreen
import com.ribuufing.bloodapp.feature.dmchat.presentation.ChatDetailScreen
import com.ribuufing.bloodapp.feature.dmchat.presentation.DmChatsListScreen
import com.ribuufing.bloodapp.feature.home.presentation.components.StoryDetail
import com.ribuufing.bloodapp.feature.home.presentation.HomeScreen
import com.ribuufing.bloodapp.feature.logintype.LoginTypeScreen
import com.ribuufing.bloodapp.feature.logintype.FormScreen
import com.ribuufing.bloodapp.feature.logintype.presentation.CompleteProfileScreen
import com.ribuufing.bloodapp.feature.map.presentation.MapScreen
import com.ribuufing.bloodapp.feature.map.presentation.HospitalDetailScreen
import com.ribuufing.bloodapp.feature.onboarding.WelcomeScreen
import com.ribuufing.bloodapp.feature.postdetail.presentation.PostDetailScreen
import com.ribuufing.bloodapp.feature.profile.presentation.OwneredPostDetail
import com.ribuufing.bloodapp.feature.profile.presentation.ProfileScreen
import com.ribuufing.bloodapp.feature.scanqr.presentation.QrScreen
import com.ribuufing.bloodapp.feature.settings.presentation.SettingsScreen
import com.ribuufing.bloodapp.feature.sharepost.presentation.SharePostScreen
import com.ribuufing.bloodapp.feature.settings.presentation.PrivacyPolicyScreen
import com.ribuufing.bloodapp.feature.settings.presentation.FAQScreen
import com.ribuufing.bloodapp.feature.settings.presentation.AboutScreen
import com.ribuufing.bloodapp.feature.settings.presentation.TermsScreen
import com.ribuufing.bloodapp.feature.settings.presentation.NotificationSettingsScreen
import com.ribuufing.bloodapp.feature.settings.presentation.LanguageSettingsScreen
import com.ribuufing.bloodapp.feature.settings.presentation.ThemeSettingsScreen
import com.ribuufing.bloodapp.feature.form.presentation.FormViewScreen


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavigationGraph(
    navController: NavHostController,
    startDestination: String,
    onBottomBarVisibility: (Boolean) -> Unit
) {
    NavHost(navController, startDestination = startDestination) {
        composable(Routes.Welcome.route) {
            onBottomBarVisibility(false)
            WelcomeScreen(navController = navController)
        }

        composable(Routes.QrScan.route){
            onBottomBarVisibility(false)
            QrScreen(navController = navController)
        }

        composable(BottomNavigationItems.Home.route) {
            onBottomBarVisibility(true)
            HomeScreen(navController = navController)
        }

        composable(BottomNavigationItems.MapItem.route) {
            onBottomBarVisibility(false)
            MapScreen(navController = navController)
        }

        composable(Routes.SharePost.route) {
            SharePostScreen(navController = navController)
            onBottomBarVisibility(false)
        }

        composable(BottomNavigationItems.Profile.route) {
            onBottomBarVisibility(true)
            ProfileScreen(navController = navController)
        }

        composable(
            route = "ownered_post_detail/{postId}/{userId}",
            enterTransition = ::slideInToLeft,
            exitTransition = ::slideOutToRight,
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType }
            )
        ){ backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            onBottomBarVisibility(false)
            OwneredPostDetail(
                navController = navController,
                postId,
                userId
            )
        }

        composable(
            Routes.Signup.route,
            enterTransition = ::slideInToLeft,
            exitTransition = ::slideOutToRight
        ) {
            onBottomBarVisibility(false)
            SignupScreen(navController = navController)
        }

        composable(
            Routes.ChatList.route,
            enterTransition = ::slideInToLeft,
            exitTransition = ::slideOutToRight
        ) {
            onBottomBarVisibility(false)
            DmChatsListScreen(navController = navController)
        }

        composable(
            route = "chat/{roomId}",
            arguments = listOf(
                navArgument("roomId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: return@composable
            onBottomBarVisibility(false)
            ChatDetailScreen(
                navController = navController,
                roomId = roomId
            )
        }

        composable(
            Routes.LoginType.route,
            enterTransition = ::slideInToLeft,
            exitTransition = ::slideOutToRight
        ){
            onBottomBarVisibility(false)
            LoginTypeScreen(navController = navController)
        }

        composable(
            route = "form_screen",
            enterTransition = ::slideInToLeft,
            exitTransition = ::slideOutToRight
        ) {
            onBottomBarVisibility(false)
            FormScreen(navController = navController)
        }

        composable(
            Routes.Settings.route,
            enterTransition = ::slideInToLeft,
            exitTransition = ::slideOutToRight
        ) {
            onBottomBarVisibility(false)
            SettingsScreen(navController = navController)
        }

        // Ayarlar ekranları
        composable(Routes.PrivacyPolicy.route) {
            onBottomBarVisibility(false)
            PrivacyPolicyScreen(navController = navController)
        }

        composable(Routes.FAQ.route) {
            onBottomBarVisibility(false)
            FAQScreen(navController = navController)
        }

        composable(Routes.About.route) {
            onBottomBarVisibility(false)
            AboutScreen(navController = navController)
        }

        composable(Routes.Terms.route) {
            onBottomBarVisibility(false)
            TermsScreen(navController = navController)
        }

        composable(Routes.Notifications.route) {
            onBottomBarVisibility(false)
            NotificationSettingsScreen(navController = navController)
        }

        composable(Routes.Language.route) {
            onBottomBarVisibility(false)
            LanguageSettingsScreen(navController = navController)
        }

        composable(Routes.Theme.route) {
            onBottomBarVisibility(false)
            ThemeSettingsScreen(navController = navController)
        }

        composable(
            route = "post_detail/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
            onBottomBarVisibility(false)
            PostDetailScreen(
                navController = navController,
                postId = postId
            )
        }

        composable(
            route = "story_detail/{storyId}",
            arguments = listOf(navArgument("storyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val storyId = backStackEntry.arguments?.getString("storyId") ?: return@composable
            onBottomBarVisibility(false)
            StoryDetail(
                navController = navController,
                storyId = storyId
            )
        }

        composable(
            route = "complete_profile_screen",
            enterTransition = ::slideInToLeft,
            exitTransition = ::slideOutToRight
        ) {
            onBottomBarVisibility(false)
            CompleteProfileScreen(onSuccess = {
                navController.navigate("form_screen") {
                    popUpTo(0) { inclusive = false }
                    launchSingleTop = true
                }
            })
        }

        composable(
            route = "view_form_screen",
            enterTransition = ::slideInToLeft,
            exitTransition = ::slideOutToRight
        ) {
            onBottomBarVisibility(false)
            FormViewScreen(
                navController = navController,
            )
        }
        
        composable(
            route = "hospital_detail/{hospitalId}",
            arguments = listOf(navArgument("hospitalId") { type = NavType.IntType })
        ) { backStackEntry ->
            val hospitalId = backStackEntry.arguments?.getInt("hospitalId") ?: return@composable
            onBottomBarVisibility(false)
            HospitalDetailScreen(
                navController = navController,
                hospitalId = hospitalId
            )
        }
    }
}

fun slideInToLeft(scope: AnimatedContentTransitionScope<NavBackStackEntry>): EnterTransition {
    return scope.slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(300)
    )
}

fun slideInToRight(scope: AnimatedContentTransitionScope<NavBackStackEntry>): EnterTransition {
    return scope.slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(300)
    )
}

fun slideOutToLeft(scope: AnimatedContentTransitionScope<NavBackStackEntry>): ExitTransition {
    return scope.slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(300)
    )
}

fun slideOutToRight(scope: AnimatedContentTransitionScope<NavBackStackEntry>): ExitTransition {
    return scope.slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(300)
    )
}