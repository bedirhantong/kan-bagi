package com.ribuufing.bloodapp.feature.authentication.presentation

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.exception.MsalException
import com.microsoft.identity.client.IPublicClientApplication
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication
import com.ribuufing.bloodapp.R
import com.ribuufing.bloodapp.core.Constants
import com.ribuufing.bloodapp.navigation.BottomNavigationItems
import com.ribuufing.bloodapp.navigation.Routes
import com.ribuufing.bloodapp.utils.components.AuthDivider
import com.ribuufing.bloodapp.utils.components.BloodButton
import com.ribuufing.bloodapp.utils.components.OtpBottomSheet
import com.ribuufing.bloodapp.utils.components.PhoneTextField
import com.ribuufing.bloodapp.utils.components.SocialLoginButton
import com.ribuufing.bloodapp.utils.components.countries
import java.security.MessageDigest

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    navController: NavController
) {
    var phone by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var showOtpSheet by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf(countries.first()) }
    val context = LocalContext.current
    val loginState by authViewModel.loginState.collectAsState()
    val activity = context as ComponentActivity
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current


    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // Background Image with Overlay
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = Constants.AUTH_BACKGROUND_IMAGE,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            // Title
            Text(
                text = stringResource(R.string.login_as_user),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )

            // Phone Input
            PhoneTextField(
                phone = phone,
                onPhoneChange = {
                    phone = it
                    phoneError = null
                },
                selectedCountry = selectedCountry,
                onCountryChange = { selectedCountry = it },
                errorMessage = phoneError,
                isError = phoneError != null,
                modifier = Modifier.fillMaxWidth()
            )

            val phoneNumberErrorResource = stringResource(id = R.string.enter_phone_number_error)
            val enterValidPhoneNumberResource = stringResource(id = R.string.enter_a_valid_number)
            val continueStringResource = stringResource(id = R.string.send_code)
            // Continue B
            BloodButton(
                text = continueStringResource,
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    when {
                        phone.isEmpty() -> {
                            phoneError = phoneNumberErrorResource
                        }

                        phone.length < 10 -> {
                            phoneError = enterValidPhoneNumberResource
                        }

                        else -> {
                            showOtpSheet = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Or Divider
            AuthDivider(
                text = stringResource(R.string.or),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Social Login Buttons
            SocialLoginButton(
                text = stringResource(id = R.string.continue_with_google),
                icon = painterResource(id = R.drawable.ic_google),
                onClick = { /* Handle Google login */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
            
            // Microsoft Login Button
            SocialLoginButton(
                text = "Continue with Microsoft",
                icon = painterResource(id = R.drawable.ic_microsoft),
                onClick = { 

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dont_have_account),
                    color = Color.White
                )
                TextButton(
                    onClick = { navController.navigate(Routes.Signup.route) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(stringResource(R.string.register), color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Error Message
        AnimatedVisibility(
            visible = loginState is LoginState.Error,
            enter = slideInVertically { with(density) { 40.dp.roundToPx() } } + fadeIn(),
            exit = slideOutVertically { with(density) { 40.dp.roundToPx() } } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Snackbar(
                modifier = Modifier.padding(16.dp)
            ) {
                when (val state = loginState) {
                    is LoginState.Error -> Text(text = state.message)
                    else -> {}
                }
            }
        }
    }

    OtpBottomSheet(
        phoneNumber = phone,
        isVisible = showOtpSheet,
        onDismiss = { showOtpSheet = false },
        onVerify = { otp ->
//            authViewModel.login(phone, otp)
        }
    )

    // Observe login state
//    LaunchedEffect(loginState) {
//        when (val state = loginState) {
//            is LoginState.Success -> {
//                Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
//                navController.navigate(BottomNavigationItems.Home.route) {
//                    popUpTo(0) { inclusive = true }
//                    launchSingleTop = true
//                }
//            }
//            is LoginState.Error -> {
//                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
//            }
//            else -> {}
//        }
//    }
}