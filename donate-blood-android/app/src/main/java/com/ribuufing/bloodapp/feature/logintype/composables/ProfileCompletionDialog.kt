package com.ribuufing.bloodapp.feature.logintype.composables

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ribuufing.bloodapp.feature.logintype.domain.model.request.CompleteProfileRequest
import java.time.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileCompletionDialog(
    onDismiss: () -> Unit,
    onSubmit: (CompleteProfileRequest) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var tcIdentityNumber by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    // Dropdown states
    var expandedBloodType by remember { mutableStateOf(false) }
    var expandedUserType by remember { mutableStateOf(false) }
    var expandedGender by remember { mutableStateOf(false) }

    var bloodType by remember { mutableStateOf(0) }
    var userType by remember { mutableStateOf(0) }
    var gender by remember { mutableStateOf(0) }

    // Blood type options
    val bloodTypes = listOf(
        "A Rh+" to 0,
        "A Rh-" to 1,
        "B Rh+" to 2,
        "B Rh-" to 3,
        "AB Rh+" to 4,
        "AB Rh-" to 5,
        "0 Rh+" to 6,
        "0 Rh-" to 7
    )

    // User type options
    val userTypes = listOf(
        "Hasta" to 0,
        "Doktor" to 1
    )

    // Gender options
    val genders = listOf(
        "Erkek" to 0,
        "Kadın" to 1
    )

    // Form validation
    val isFormValid = remember(name, surname, phoneNumber, tcIdentityNumber, birthDate) {
        name.isNotBlank() && surname.isNotBlank() &&
                phoneNumber.isNotBlank() && tcIdentityNumber.isNotBlank() &&
                birthDate.isNotBlank()
    }

    // Date picker
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    selectedDate?.let {
                        birthDate = "${it.year}-${it.monthValue.toString().padStart(2, '0')}-${it.dayOfMonth.toString().padStart(2, '0')}"
                    }
                    showDatePicker = false
                }) {
                    Text("Tamam")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("İptal")
                }
            }
        ) {
//            DatePicker(
//                state = rememberDatePickerState(
//                    initialSelectedDateMillis = selectedDate?.let {
//                        LocalDateTime.of(it, LocalTime.NOON)
//                            .toInstant(ZoneOffset.UTC)
//                            .toEpochMilli()
//                    }
//                ),
//                title = { Text("Doğum Tarihi Seçin") },
//                headline = { Text("Lütfen doğum tarihinizi seçin") },
//                showModeToggle = false,
//                onDateSelected = { millis ->
//                    millis?.let {
//                        val date = Instant.ofEpochMilli(it)
//                            .atZone(ZoneId.systemDefault())
//                            .toLocalDate()
//                        selectedDate = date
//                    }
//                }
//            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Profilinizi Tamamlayın",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Daha iyi bir deneyim için lütfen bilgilerinizi eksiksiz doldurun",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Form fields with animations
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Personal information section
                        Text(
                            text = "Kişisel Bilgiler",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Name and surname in a row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Ad") },
                                modifier = Modifier.weight(1f),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Person,
                                        contentDescription = null
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            OutlinedTextField(
                                value = surname,
                                onValueChange = { surname = it },
                                label = { Text("Soyad") },
                                modifier = Modifier.weight(1f),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Person,
                                        contentDescription = null
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }

                        // Contact information section
                        Text(
                            text = "İletişim Bilgileri",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Telefon Numarası") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Phone,
                                    contentDescription = null
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        // Identity information section
                        Text(
                            text = "Kimlik Bilgileri",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        OutlinedTextField(
                            value = tcIdentityNumber,
                            onValueChange = {
                                if (it.length <= 11 && it.all { char -> char.isDigit() }) {
                                    tcIdentityNumber = it
                                }
                            },
                            label = { Text("TC Kimlik Numarası") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            supportingText = {
                                Text("11 haneli TC kimlik numaranızı girin")
                            }
                        )

                        // Date picker field
                        OutlinedTextField(
                            value = birthDate,
                            onValueChange = { },
                            label = { Text("Doğum Tarihi") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.DateRange,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(
                                        imageVector = Icons.Filled.DateRange,
                                        contentDescription = "Tarih Seç"
                                    )
                                }
                            },
                            readOnly = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        // Additional information section
                        Text(
                            text = "Ek Bilgiler",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Blood type dropdown
                        ExposedDropdownMenuBox(
                            expanded = expandedBloodType,
                            onExpandedChange = { expandedBloodType = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = bloodTypes.find { it.second == bloodType }?.first ?: "Kan Grubu Seçin",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Kan Grubu") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Favorite,
                                        contentDescription = null
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBloodType)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = expandedBloodType,
                                onDismissRequest = { expandedBloodType = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                bloodTypes.forEach { (text, value) ->
                                    DropdownMenuItem(
                                        text = { Text(text) },
                                        onClick = {
                                            bloodType = value
                                            expandedBloodType = false
                                        }
                                    )
                                }
                            }
                        }

                        // User type dropdown
                        ExposedDropdownMenuBox(
                            expanded = expandedUserType,
                            onExpandedChange = { expandedUserType = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = userTypes.find { it.second == userType }?.first ?: "Kullanıcı Tipi Seçin",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Kullanıcı Tipi") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.AccountCircle,
                                        contentDescription = null
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUserType)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = expandedUserType,
                                onDismissRequest = { expandedUserType = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                userTypes.forEach { (text, value) ->
                                    DropdownMenuItem(
                                        text = { Text(text) },
                                        onClick = {
                                            userType = value
                                            expandedUserType = false
                                        }
                                    )
                                }
                            }
                        }

                        // Gender dropdown
                        ExposedDropdownMenuBox(
                            expanded = expandedGender,
                            onExpandedChange = { expandedGender = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = genders.find { it.second == gender }?.first ?: "Cinsiyet Seçin",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Cinsiyet") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Face,
                                        contentDescription = null
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGender)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = expandedGender,
                                onDismissRequest = { expandedGender = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                genders.forEach { (text, value) ->
                                    DropdownMenuItem(
                                        text = { Text(text) },
                                        onClick = {
                                            gender = value
                                            expandedGender = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text("İptal")
                    }


                }
            }
        }
    }
}

