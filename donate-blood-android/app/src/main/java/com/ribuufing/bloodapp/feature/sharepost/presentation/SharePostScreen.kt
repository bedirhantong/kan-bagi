package com.ribuufing.bloodapp.feature.sharepost.presentation

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ribuufing.bloodapp.navigation.BottomNavigationItems
import com.ribuufing.bloodapp.utils.observeInternetConnectivity
import com.ribuufing.bloodapp.core.utils.BloodAppDialog
import androidx.compose.ui.window.DialogProperties
import com.ribuufing.bloodapp.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharePostScreen(
    navController: NavController,
    viewModel: SharePostViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val hospitals by viewModel.hospitals.collectAsState()
    val bloodTypes by viewModel.bloodTypes.collectAsState()
    val isConnected by observeInternetConnectivity()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadProfileInfo()
        viewModel.loadInitialData()
    }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SharePostUiEvent.Success -> {
                    Toast.makeText(
                        context,
                        "Kan bağışı isteği başarıyla oluşturuldu",
                        Toast.LENGTH_LONG
                    ).show()
                    navController.navigate(BottomNavigationItems.Home.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kan Bağışı İsteği Oluştur",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hasta Bilgileri Kartı
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Hasta Bilgileri",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        // Hasta Adı
                        OutlinedTextField(
                            value = state.patientName,
                            onValueChange = { viewModel.onEvent(SharePostEvent.PatientNameChanged(it)) },
                            label = { Text("Hasta Adı") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Person, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        // Hasta Yaşı
                        OutlinedTextField(
                            value = state.patientAge,
                            onValueChange = { viewModel.onEvent(SharePostEvent.PatientAgeChanged(it)) },
                            label = { Text("Hasta Yaşı") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Menu, contentDescription = null)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }
                
                // Hastane ve Kan Grubu Kartı
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Hastane ve Kan Bilgileri",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        // Hastane Seçimi
                        ExposedDropdownMenuBox(
                            expanded = state.isHospitalDropdownExpanded,
                            onExpandedChange = { viewModel.onEvent(SharePostEvent.HospitalDropdownExpandedChanged(it)) }
                        ) {
                            OutlinedTextField(
                                value = state.selectedHospital?.let { "${it.name} - ${it.city}" } ?: "",
                                onValueChange = { viewModel.onEvent(SharePostEvent.HospitalSearch(it)) },
                                label = { Text("Hastane Seçin") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Home, contentDescription = null)
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.isHospitalDropdownExpanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                singleLine = true
                            )
                            
                            ExposedDropdownMenu(
                                expanded = state.isHospitalDropdownExpanded,
                                onDismissRequest = { viewModel.onEvent(SharePostEvent.HospitalDropdownExpandedChanged(false)) }
                            ) {
                                (if (state.filteredHospitals.isEmpty()) hospitals else state.filteredHospitals).forEach { hospital ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = hospital.name,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                                Text(
                                                    text = "${hospital.city} - ${hospital.address}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            viewModel.onEvent(SharePostEvent.HospitalSelected(hospital))
                                            viewModel.onEvent(SharePostEvent.HospitalDropdownExpandedChanged(false))
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Kan Grubu Seçimi
                        ExposedDropdownMenuBox(
                            expanded = state.isBloodTypeDropdownExpanded,
                            onExpandedChange = { viewModel.onEvent(SharePostEvent.BloodTypeDropdownExpandedChanged(it)) }
                        ) {
                            OutlinedTextField(
                                value = state.selectedBloodType?.type ?: "",
                                onValueChange = { viewModel.onEvent(SharePostEvent.BloodTypeSearch(it)) },
                                label = { Text("Kan Grubu") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Favorite, contentDescription = null)
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.isBloodTypeDropdownExpanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                singleLine = true
                            )
                            
                            ExposedDropdownMenu(
                                expanded = state.isBloodTypeDropdownExpanded,
                                onDismissRequest = { viewModel.onEvent(SharePostEvent.BloodTypeDropdownExpandedChanged(false)) }
                            ) {
                                (if (state.filteredBloodTypes.isEmpty()) bloodTypes else state.filteredBloodTypes).forEach { bloodType ->
                                    DropdownMenuItem(
                                        text = { Text("${bloodType.type} (${bloodType.description})") },
                                        onClick = {
                                            viewModel.onEvent(SharePostEvent.BloodTypeSelected(bloodType))
                                            viewModel.onEvent(SharePostEvent.BloodTypeDropdownExpandedChanged(false))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // İletişim Bilgileri Kartı
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "İletişim Bilgileri",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        // İstek Sahibi Adı
                        OutlinedTextField(
                            value = state.ownerName,
                            onValueChange = { viewModel.onEvent(SharePostEvent.RequesterNameChanged(it)) },
                            label = { Text("İstek Sahibi Adı") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Person, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        // İstek Sahibi Soyadı
                        OutlinedTextField(
                            value = state.ownerSurname,
                            onValueChange = { viewModel.onEvent(SharePostEvent.RequesterSurnameChanged(it)) },
                            label = { Text("İstek Sahibi Soyadı") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Person, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        // Telefon Numaraları
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            var newPhoneNumber by remember { mutableStateOf("") }
                            
                            // Mevcut telefon numaraları
                            state.phoneNumbers.forEach { phoneNumber ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = phoneNumber,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    IconButton(
                                        onClick = { viewModel.onEvent(SharePostEvent.ContactNumberRemoved(phoneNumber)) }
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Numarayı Sil",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                            
                            // Yeni numara ekleme
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newPhoneNumber,
                                    onValueChange = { newPhoneNumber = it },
                                    label = { Text("Yeni Telefon Numarası") },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Phone, contentDescription = null)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Phone,
                                        imeAction = ImeAction.Done
                                    )
                                )
                                IconButton(
                                    onClick = {
                                        if (newPhoneNumber.isNotBlank()) {
                                            viewModel.onEvent(SharePostEvent.ContactNumberAdded(newPhoneNumber))
                                            newPhoneNumber = ""
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Numara Ekle",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Başlık ve Açıklama Kartı
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Başlık ve Açıklama",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        // Başlık
                        OutlinedTextField(
                            value = state.title,
                            onValueChange = { viewModel.onEvent(SharePostEvent.TitleChanged(it)) },
                            label = { Text("Başlık") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Menu, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        // Açıklama
                        OutlinedTextField(
                            value = state.description,
                            onValueChange = { viewModel.onEvent(SharePostEvent.AdditionalInfoChanged(it)) },
                            label = { Text("Açıklama") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Menu, contentDescription = null)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 5
                        )
                    }
                }
                
                // Şartlar ve Koşullar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = state.acceptedTerms,
                        onCheckedChange = { viewModel.onEvent(SharePostEvent.TermsAccepted(it)) }
                    )
                    Text(
                        text = "Şartları ve koşulları kabul ediyorum",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable {
                            navController.navigate(Routes.Terms.route)
                        },
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Hata Mesajı
                AnimatedVisibility(visible = state.error != null) {
                    Text(
                        text = state.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Gönder Butonu
                Button(
                    onClick = { viewModel.onEvent(SharePostEvent.SubmitRequest) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !state.isLoading && state.acceptedTerms,
                    shape = RoundedCornerShape(28.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Kan Bağışı İsteği Oluştur")
                    }
                }
            }
            
            // Show loading indicator
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Show error dialog instead of Snackbar
            if (state.showErrorDialog) {
                BloodAppDialog(
                    onDismissRequest = { viewModel.onEvent(SharePostEvent.CloseErrorDialog) },
                    onConfirmClick = { viewModel.onEvent(SharePostEvent.CloseErrorDialog) },
                    title = "Kan Bağışı İsteği Oluşturulamadı",
                    message = state.error ?: "Bir hata oluştu. Lütfen tekrar deneyin.",
                    icon = Icons.Filled.Warning,
                    showLogo = false,
                    confirmText = "Tamam",
                    dismissText = "",
                    properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
                )
            }
        }
    }
}