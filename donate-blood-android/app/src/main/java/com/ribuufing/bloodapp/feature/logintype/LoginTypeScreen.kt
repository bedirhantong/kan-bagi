package com.ribuufing.bloodapp.feature.logintype

import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ribuufing.bloodapp.R
import com.ribuufing.bloodapp.core.Constants
import com.ribuufing.bloodapp.feature.authentication.presentation.AuthViewModel
import com.ribuufing.bloodapp.feature.authentication.presentation.LoginState
import com.ribuufing.bloodapp.feature.authentication.presentation.ProfileCompletionState
import com.ribuufing.bloodapp.feature.form.domain.response.GetQuestionsResponse
import com.ribuufing.bloodapp.feature.form.domain.usecase.GetFormUseCase
import com.ribuufing.bloodapp.feature.form.presentation.FormViewModel
import com.ribuufing.bloodapp.feature.logintype.composables.LoginTypeButton
import com.ribuufing.bloodapp.feature.logintype.domain.model.request.CompleteProfileRequest
import com.ribuufing.bloodapp.navigation.BottomNavigationItems
import com.ribuufing.bloodapp.navigation.Routes
import com.ribuufing.bloodapp.utils.components.AuthDivider
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Giriş tipi seçim ekranı
 *
 * Kullanıcıya farklı giriş tipleri sunar:
 * 1. Microsoft hesabı ile giriş (B2C)
 * 2. Hastane kullanıcısı olarak giriş
 *
 * @param authViewModel Kimlik doğrulama işlemlerini yöneten ViewModel
 * @param navController Ekranlar arası geçişleri yönetmek için navigasyon denetleyicisi
 */
@Composable
fun LoginTypeScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    val loginState by authViewModel.loginState.collectAsState()
    val profileState by authViewModel.profileState.collectAsState()
    val activity = context as ComponentActivity
    var showLoading by remember { mutableStateOf(false) }

    val formViewModel: FormViewModel = hiltViewModel()
    val formScreenNavigationTrigger by formViewModel.formScreenNavigationTrigger.collectAsState()

    var isInitializing by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            authViewModel.initializeMsal(activity)
            delay(1000)
            isInitializing = false
        } catch (e: Exception) {
            Log.e("AUTH_ERROR", "Error during MSAL initialization: ${e.message}")
            Toast.makeText(context, "Authentication initialization failed. Please try again.", Toast.LENGTH_LONG).show()
            isInitializing = false
        }
    }

    if (isInitializing || showLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        if (isInitializing) return
    }

    // Ana ekran yapısı
    LoginTypeScreenContent(
        activity = activity,
        navController = navController
    )

    // Handle login state changes
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.MsalSuccess -> {
                Log.d("LoginTypeScreen", "MSAL authentication successful")
                showLoading = true
                // Profile check will be triggered automatically by ViewModel only for regular users
                if (!authViewModel.isHospitalStaff()) {
                    authViewModel.checkProfileCompletion()
                } else {
                    // Hospital staff - directly mark as success
                    authViewModel.setLoginSuccess()
                }
            }
            is LoginState.Success -> {
                Log.d("LoginTypeScreen", "Full authentication successful")
                showLoading = false
                Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                
                // Navigate based on user type
                if (authViewModel.isHospitalStaff()) {
                    navController.navigate(Routes.QrScan.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                } else {
                    navController.navigate(BottomNavigationItems.Home.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            is LoginState.Error -> {
                Log.e("LoginTypeScreen", "Authentication error:")
                showLoading = false
                handleLoginError(context, (loginState as LoginState.Error).message)
            }
            else -> { /* No action needed for other states */ }
        }
    }

    // Handle profile state changes
    LaunchedEffect(profileState) {
        when (profileState) {
            is ProfileCompletionState.RequiresCompletion -> {
                navController.navigate("complete_profile_screen") {
                    popUpTo(0) { inclusive = false }
                    launchSingleTop = true
                }
            }
            is ProfileCompletionState.Completed -> {
                // Sadece bir kez kontrol et
                authViewModel.checkFormAndNavigate(navController)
            }
            is ProfileCompletionState.Error -> {
                Log.e("LoginTypeScreen", "Profile completion error: ${(profileState as ProfileCompletionState.Error).message}")
                showLoading = false
                Toast.makeText(
                    context,
                    "Profile completion failed: ${(profileState as ProfileCompletionState.Error).message}",
                    Toast.LENGTH_LONG
                ).show()
            }
            is ProfileCompletionState.Loading -> {
                Log.d("LoginTypeScreen", "Profile completion loading")
                showLoading = true
            }
            else -> { /* No action needed for other states */ }
        }
    }

    if (formScreenNavigationTrigger) {
        LaunchedEffect(Unit) {
            navController.navigate("form_screen") {
                popUpTo(0) { inclusive = false }
                launchSingleTop = true
            }
            formViewModel.onFormScreenNavigated()
        }
    }
}

/**
 * Login tipi ekranının ana içeriği
 */
@Composable
private fun LoginTypeScreenContent(
    activity: ComponentActivity,
    authViewModel: AuthViewModel = hiltViewModel(),
    navController: NavController
) {

    Box(modifier = Modifier.fillMaxSize().imePadding()) {
        // Arkaplan
        BackgroundContent()

        // Ana içerik
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(26.dp, Alignment.CenterVertically)
        ) {
            // Başlık
//            LoginTitle()

            // Giriş butonları
            LoginButtons(
                activity = activity,
                authViewModel = authViewModel,
                navController = navController
            )
        }
    }
}

/**
 * Ekran arkaplanını oluşturur
 */
@Composable
private fun BackgroundContent() {
    Box(modifier = Modifier.fillMaxSize()) {


        AsyncImage(
            model = Constants.AUTH_BACKGROUND_IMAGE,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Futuristic gradient overlay with depth effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF000000).copy(alpha = 0.85f),
                            Color(0xFF1A237E).copy(alpha = 0.6f),
                            Color(0xFF000000).copy(alpha = 0.85f)
                        )
                    )
                )
                .blur(radius = 2.dp)
        )
        
        // Dynamic particles effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.Transparent,
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

/**
 * Giriş butonlarını içeren bileşen
 */
@Composable
private fun LoginButtons(
    activity: ComponentActivity,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Microsoft login button
        LoginTypeButton(
            text = stringResource(id = R.string.continue_with_regular_user),
            onClick = {
                try {
                    authViewModel.signIn(activity)
                } catch (e: Exception) {
                    Log.e("AUTH_ERROR", "Error during login: ${e.message}")
                    Toast.makeText(context, "Login failed. Please try again.", Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF2196F3), Color(0xFF1976D2))
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
        )

        AuthDivider(
            text = stringResource(R.string.or),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Hospital user login button
        LoginTypeButton(
            text = stringResource(id = R.string.continue_with_hospital_user),
            onClick = {
                try {
                    authViewModel.signIn(activity, isHospitalStaff = true)
                } catch (e: Exception) {
                    Log.e("AUTH_ERROR", "Error during hospital staff login: ${e.message}")
                    Toast.makeText(context, "Hastane personeli girişi başarısız oldu. Lütfen tekrar deneyin.", Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF4A148C), Color(0xFF7B1FA2))
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
        )
    }
}

/**
 * Giriş hatalarını işler
 */
private fun handleLoginError(context: android.content.Context, errorMessage: String) {
    Log.e("AUTH_ERROR", "Authentication error: $errorMessage")

    val userMessage = when {
        errorMessage.contains("MSAL is not initialized") ->
            "Authentication service is not ready. Please try again in a moment."
        errorMessage.contains("AADB2C90118") ->
            "Password reset in progress..."
        errorMessage.contains("Missing required tokens") ->
            "Authentication successful but token processing failed. Please try again."
        errorMessage.contains("Could not retrieve authentication token") ->
            "Could not retrieve authentication credentials. Please try again."
        errorMessage.contains("InteractionRequiredException") ->
            "Please try logging in again."
        errorMessage.contains("User canceled authentication") ->
            "Login was cancelled."
        errorMessage.contains("No id_token found") ->
            "Authentication successful but credentials not received. Please try again."
        errorMessage.contains("Error parsing authentication response") ->
            "Error processing authentication response. Please try again."
        else -> errorMessage
    }

    Log.d("LoginTypeScreen", "Error: $userMessage")
    Toast.makeText(context, userMessage, Toast.LENGTH_LONG).show()
}