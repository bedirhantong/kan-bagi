package com.ribuufing.bloodapp.feature.logintype.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ribuufing.bloodapp.feature.logintype.domain.model.request.CompleteProfileRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteProfileScreen(
    onSuccess: () -> Unit,
    viewModel: CompleteProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var tcIdentityNumber by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }

    // Kan grubu seçenekleri (label, backendValue)
    val bloodTypes = listOf(
        "0Rh+" to "O_Positive",
        "0Rh-" to "O_Negative",
        "ARh+" to "A_Positive",
        "ARh-" to "A_Negative",
        "BRh+" to "B_Positive",
        "BRh-" to "B_Negative",
        "ABRh+" to "AB_Positive",
        "ABRh-" to "AB_Negative"
    )
    var bloodTypeIndex by remember { mutableStateOf(0) }
    var bloodTypeExpanded by remember { mutableStateOf(false) }

    // Kullanıcı tipi (label, backendValue)
    val userTypes = listOf(
        "Normal Kullanıcı" to "Donor",
        "Hastane Personeli" to "HospitalStaff"
    )
    var userTypeIndex by remember { mutableStateOf(0) }
    var userTypeExpanded by remember { mutableStateOf(false) }

    // Cinsiyet (label, backendValue)
    val genders = listOf(
        "Kadın" to "female",
        "Erkek" to "male"
    )
    var genderIndex by remember { mutableStateOf(0) }
    var genderExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state is CompleteProfileState.Success) {
            onSuccess()
            viewModel.reset()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Profilini Tamamla") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Ad") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = surname,
                onValueChange = { surname = it },
                label = { Text("Soyad") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Telefon Numarası") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = tcIdentityNumber,
                onValueChange = { tcIdentityNumber = it },
                label = { Text("TC Kimlik No") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = birthDate,
                onValueChange = { birthDate = it },
                label = { Text("Doğum Tarihi (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )
            // Kan grubu dropdown
            ExposedDropdownMenuBox(
                expanded = bloodTypeExpanded,
                onExpandedChange = { bloodTypeExpanded = !bloodTypeExpanded }
            ) {
                OutlinedTextField(
                    value = bloodTypes[bloodTypeIndex].first,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kan Grubu") },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodTypeExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = bloodTypeExpanded,
                    onDismissRequest = { bloodTypeExpanded = false }
                ) {
                    bloodTypes.forEachIndexed { idx, (label, _) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                bloodTypeIndex = idx
                                bloodTypeExpanded = false
                            }
                        )
                    }
                }
            }
            // Kullanıcı tipi dropdown
            ExposedDropdownMenuBox(
                expanded = userTypeExpanded,
                onExpandedChange = { userTypeExpanded = !userTypeExpanded }
            ) {
                OutlinedTextField(
                    value = userTypes[userTypeIndex].first,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kullanıcı Tipi") },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = userTypeExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = userTypeExpanded,
                    onDismissRequest = { userTypeExpanded = false }
                ) {
                    userTypes.forEachIndexed { idx, (label, _) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                userTypeIndex = idx
                                userTypeExpanded = false
                            }
                        )
                    }
                }
            }
            // Cinsiyet dropdown
            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { genderExpanded = !genderExpanded }
            ) {
                OutlinedTextField(
                    value = genders[genderIndex].first,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Cinsiyet") },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false }
                ) {
                    genders.forEachIndexed { idx, (label, _) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                genderIndex = idx
                                genderExpanded = false
                            }
                        )
                    }
                }
            }
            if (state is CompleteProfileState.Error) {
                Text((state as CompleteProfileState.Error).message, color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = {
                    viewModel.completeProfile(
                        CompleteProfileRequest(
                            name = name,
                            surname = surname,
                            phoneNumber = phoneNumber,
                            tcIdentityNumber = tcIdentityNumber,
                            birthDate = birthDate,
                            bloodType = bloodTypes[bloodTypeIndex].second,
                            userType = userTypes[userTypeIndex].second,
                            gender = genders[genderIndex].second
                        )
                    )
                },
                enabled = state !is CompleteProfileState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state is CompleteProfileState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Kaydet ve Devam Et")
                }
            }
        }
    }
} 