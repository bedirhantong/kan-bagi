package com.ribuufing.bloodapp

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ribuufing.bloodapp.core.manager.AuthManager
import com.ribuufing.bloodapp.navigation.BottomBar
import com.ribuufing.bloodapp.navigation.BottomNavigationItems
import com.ribuufing.bloodapp.navigation.NavigationGraph
import com.ribuufing.bloodapp.navigation.Routes
import com.ribuufing.bloodapp.ui.theme.BloodAppTheme
import com.ribuufing.bloodapp.ui.theme.splash.AnimatedSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var authManager: AuthManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val startDestination = if (authManager.isUserLoggedIn) {
            BottomNavigationItems.Home.route
        } else {
            Routes.Welcome.route
        }

        setContent {
            BloodAppTheme {
                var showSplash by remember { mutableStateOf(true) }
                
                if (showSplash) {
                    AnimatedSplashScreen(
                        onAnimationFinish = {
                            showSplash = false
                        }
                    )
                } else {
                    MainContent(startDestination)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    private fun MainContent(startDestination: String) {
        val navController: NavHostController = rememberNavController()
        var buttonsVisible by remember { mutableStateOf(false) }

        Scaffold(
            bottomBar = {
                if (buttonsVisible) {
                    BottomBar(navController = navController, state = buttonsVisible)
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (buttonsVisible) 56.dp else 0.dp)
            ) {
                NavigationGraph(
                    navController = navController,
                    startDestination = startDestination
                ) { isVisible ->
                    buttonsVisible = isVisible
                }
            }
        }
    }
}


