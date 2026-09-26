package com.ribuufing.bloodapp.feature.authentication.presentation

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ribuufing.bloodapp.core.Constants
import com.ribuufing.bloodapp.utils.components.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.ribuufing.bloodapp.R
import com.ribuufing.bloodapp.feature.authentication.domain.model.request.BloodType
import com.ribuufing.bloodapp.feature.authentication.domain.model.request.UserType
import com.ribuufing.bloodapp.utils.components.Country
import com.ribuufing.bloodapp.utils.components.Gender
import com.ribuufing.bloodapp.utils.components.countries

data class SignupData(
    val fullName: String = "",
    val phone: String = "",
    val selectedCountry: Country = countries.first(),
    val bloodGroup: String? = null,
    val gender: Gender? = null,
    val birthDate: LocalDate? = null
)

@OptIn(ExperimentalAnimationApi::class, ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SignupScreen(
    viewModel: SignupViewModel = hiltViewModel(),
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // Background with gradient overlay
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
                                Color(0xFF1A1A1A).copy(alpha = 0.9f),
                                Color(0xFF1A1A1A).copy(alpha = 0.8f)
                            )
                        )
                    )
            )
        }

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.register),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2A2A)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Credentials Section
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Giriş Bilgileri",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFFF9E9E)
                        )

                        OutlinedTextField(
                            value = state.email,
                            onValueChange = { viewModel.onEvent(SignupEvent.UpdateEmail(it)) },
                            label = { Text("Email") },
                            isError = state.emailError != null,
                            supportingText = state.emailError?.let { { Text(it) } },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF9E9E),
                                focusedLabelColor = Color(0xFFFF9E9E),
                                cursorColor = Color(0xFFFF9E9E)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.password,
                            onValueChange = { viewModel.onEvent(SignupEvent.UpdatePassword(it)) },
                            label = { Text("Şifre") },
                            isError = state.passwordError != null,
                            supportingText = {
                                if (state.passwordError != null) {
                                    Text(state.passwordError!!)
                                } else {
                                    Text(
                                        "Şifre en az:\n" +
                                        "• 6 karakter\n" +
                                        "• 1 büyük harf\n" +
                                        "• 1 küçük harf\n" +
                                        "• 1 özel karakter (!@#\$%^&*) içermelidir",
                                        color = Color.Gray
                                    )
                                }
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF9E9E),
                                focusedLabelColor = Color(0xFFFF9E9E),
                                cursorColor = Color(0xFFFF9E9E)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.confirmPassword,
                            onValueChange = { viewModel.onEvent(SignupEvent.UpdateConfirmPassword(it)) },
                            label = { Text("Şifre Tekrar") },
                            isError = state.confirmPasswordError != null,
                            supportingText = state.confirmPasswordError?.let { { Text(it) } },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF9E9E),
                                focusedLabelColor = Color(0xFFFF9E9E),
                                cursorColor = Color(0xFFFF9E9E)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.Gray.copy(alpha = 0.3f)
                    )

                    // Personal Information Section
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Kişisel Bilgiler",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFFF9E9E)
                        )

                        OutlinedTextField(
                            value = state.name,
                            onValueChange = { viewModel.onEvent(SignupEvent.UpdateName(it)) },
                            label = { Text("İsim") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            isError = state.nameError != null,
                            supportingText = state.nameError?.let { { Text(it) } },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF9E9E),
                                focusedLabelColor = Color(0xFFFF9E9E),
                                cursorColor = Color(0xFFFF9E9E)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.surname,
                            onValueChange = { viewModel.onEvent(SignupEvent.UpdateSurname(it)) },
                            label = { Text("Soyisim") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            isError = state.surnameError != null,
                            supportingText = state.surnameError?.let { { Text(it) } },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF9E9E),
                                focusedLabelColor = Color(0xFFFF9E9E),
                                cursorColor = Color(0xFFFF9E9E)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.tcIdentityNumber,
                            onValueChange = { viewModel.onEvent(SignupEvent.UpdateTcIdentityNumber(it)) },
                            label = { Text("TC Kimlik No") },
                            isError = state.tcIdentityNumberError != null,
                            supportingText = state.tcIdentityNumberError?.let { { Text(it) } },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF9E9E),
                                focusedLabelColor = Color(0xFFFF9E9E),
                                cursorColor = Color(0xFFFF9E9E)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Date picker for birth date
                        DatePickerButton(
                            selectedDate = state.birthDate,
                            onDateSelected = { viewModel.onEvent(SignupEvent.UpdateBirthDate(it)) },
                            error = state.birthDateError
                        )
                    }

                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.Gray.copy(alpha = 0.3f)
                    )

                    // User Type Section
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Kullanıcı Bilgileri",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFFF9E9E)
                        )

                        // Blood Type Selection
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Kan Grubu",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            if (state.bloodTypeError != null) {
                                Text(
                                    text = state.bloodTypeError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                maxItemsInEachRow = 4
                            ) {
                                BloodType.entries.forEach { bloodType ->
                                    FilterChip(
                                        selected = state.bloodType == bloodType,
                                        onClick = { viewModel.onEvent(SignupEvent.UpdateBloodType(bloodType)) },
                                        label = { Text(bloodType.name.replace("_", " ")) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFFF9E9E),
                                            selectedLabelColor = Color.White
                                        ),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // User Type Selection
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Kullanıcı Tipi",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            if (state.userTypeError != null) {
                                Text(
                                    text = state.userTypeError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                UserType.entries.forEach { userType ->
                                    FilterChip(
                                        selected = state.userType == userType,
                                        onClick = { viewModel.onEvent(SignupEvent.UpdateUserType(userType)) },
                                        label = { Text(if (userType == UserType.DONOR) "Donör" else "Alıcı") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFFF9E9E),
                                            selectedLabelColor = Color.White
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Submit Button
            Button(
                onClick = { viewModel.onEvent(SignupEvent.Submit) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                enabled = !state.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9E9E),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        "Kayıt Ol",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Login Link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "Zaten hesabınız var mı?",
                    color = Color.White.copy(alpha = 0.8f)
                )
                TextButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Text(
                        "Giriş Yap",
                        color = Color(0xFFFF9E9E)
                    )
                }
            }
        }

        // Error Message
        if (state.error != null) {
            Snackbar(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                containerColor = Color(0xFF2A2A2A),
                contentColor = Color.White
            ) {
                Text(state.error!!)
            }
        }
    }

    // Navigate to login on successful registration
    LaunchedEffect(state.registrationSuccess) {
        if (state.registrationSuccess) {
            navController.popBackStack()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerButton(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    error: String?
) {
    var showDatePicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
        onValueChange = { },
        label = { Text("Doğum Tarihi") },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Tarih Seç",
                    tint = Color(0xFFFF9E9E)
                )
            }
        },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFFF9E9E),
            focusedLabelColor = Color(0xFFFF9E9E),
            cursorColor = Color(0xFFFF9E9E)
        ),
        modifier = Modifier.fillMaxWidth()
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            onDateSelected(localDate)
                        }
                        showDatePicker = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFFF9E9E)
                    )
                ) {
                    Text("Tamam")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Gray
                    )
                ) {
                    Text("İptal")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = { Text("Doğum Tarihi Seç") },
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Color(0xFFFF9E9E)
                )
            )
        }
    }
}