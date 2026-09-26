package com.ribuufing.bloodapp.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.ribuufing.bloodapp.feature.settings.presentation.viewmodel.NotificationsViewModel
import com.ribuufing.bloodapp.feature.sharepost.domain.model.Hospital

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    navController: NavController,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showHospitalSheet by remember { mutableStateOf(false) }
    var showBloodTypeSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Bildirim Ayarları",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Geri"
                        )
                    }
                },
                actions = {
                    if (state.isLoading || state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Yenile"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    NotificationCard(
                        title = "Tüm Bildirimler",
                        description = "Tüm bildirimleri aç/kapat",
                        icon = Icons.Outlined.Notifications,
                        checked = state.notificationsEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.updateNotificationSettings(
                                emailEnabled = enabled,
                                phoneEnabled = enabled,
                                pushEnabled = enabled
                            )
                        },
                        enabled = !state.isSaving
                    )
                }

                if (state.notificationsEnabled) {
                    item {
                        Text(
                            text = "Bildirim Kanalları",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    item {
                        NotificationChannelsCard(
                            emailEnabled = state.emailNotifications,
                            phoneEnabled = state.phoneNotifications,
                            pushEnabled = state.pushNotifications,
                            onEmailChange = { viewModel.updateNotificationSettings(emailEnabled = it) },
                            onPhoneChange = { viewModel.updateNotificationSettings(phoneEnabled = it) },
                            onPushChange = { viewModel.updateNotificationSettings(pushEnabled = it) },
                            enabled = !state.isSaving
                        )
                    }

                    item {
                        Text(
                            text = "Bildirim Tercihleri",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    item {
                        PreferencesCard(
                            selectedHospitals = state.selectedHospitals,
                            selectedBloodTypes = state.selectedBloodTypes,
                            onHospitalClick = { showHospitalSheet = true },
                            onBloodTypeClick = { showBloodTypeSheet = true },
                            enabled = !state.isSaving
                        )
                    }
                }
            }
        }
    }

    // Hospital Selection Bottom Sheet
    if (showHospitalSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHospitalSheet = false },
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Hastane Seçimi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn {
                    items(state.availableHospitals) { hospital ->
                        HospitalSelectionItem(
                            hospital = hospital,
                            isSelected = hospital.id.toString() in state.selectedHospitals,
                            onSelectionChanged = { 
                                viewModel.toggleHospitalSelection(hospital.id.toString())
                            },
                            enabled = !state.isSaving
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Blood Type Selection Bottom Sheet
    if (showBloodTypeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBloodTypeSheet = false },
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Kan Grubu Seçimi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // A_NEGATIVE
                LazyColumn {
                    items(listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "0+", "0-")) { bloodType ->
                        BloodTypeSelectionItem(
                            bloodType = bloodType,
                            isSelected = bloodType in state.selectedBloodTypes,
                            onSelectionChanged = {
                                viewModel.toggleBloodTypeSelection(bloodType)
                            },
                            enabled = !state.isSaving
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Error Dialog
    state.error?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            title = { Text("Hata") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissError() }) {
                    Text("Tamam")
                }
            }
        )
    }
}

@Composable
fun NotificationCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    }
}

@Composable
fun NotificationChannelsCard(
    emailEnabled: Boolean,
    phoneEnabled: Boolean,
    pushEnabled: Boolean,
    onEmailChange: (Boolean) -> Unit,
    onPhoneChange: (Boolean) -> Unit,
    onPushChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            ChannelItem(
                title = "E-posta Bildirimleri",
                description = "Kan ihtiyaçları ve eşleşmeler hakkında e-posta al",
                icon = Icons.Outlined.Email,
                checked = emailEnabled,
                onCheckedChange = onEmailChange,
                enabled = enabled
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            ChannelItem(
                title = "SMS Bildirimleri",
                description = "Telefon üzerinden SMS bildirimleri al",
                icon = Icons.Outlined.Phone,
                checked = phoneEnabled,
                onCheckedChange = onPhoneChange,
                enabled = enabled
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            ChannelItem(
                title = "Push Bildirimleri",
                description = "Uygulama üzerinden anlık bildirimler al",
                icon = Icons.Outlined.Notifications,
                checked = pushEnabled,
                onCheckedChange = onPushChange,
                enabled = enabled
            )
        }
    }
}

@Composable
fun ChannelItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
fun PreferencesCard(
    selectedHospitals: List<String>,
    selectedBloodTypes: List<String>,
    onHospitalClick: () -> Unit,
    onBloodTypeClick: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PreferenceItem(
                title = "Tercih Edilen Hastaneler",
                subtitle = if (selectedHospitals.isEmpty()) "Hastane seçilmedi" 
                          else "${selectedHospitals.size} hastane seçildi",
                icon = Icons.Outlined.Home,
                onClick = onHospitalClick,
                enabled = enabled
            )
            
            Divider()
            
            PreferenceItem(
                title = "Tercih Edilen Kan Grupları",
                subtitle = if (selectedBloodTypes.isEmpty()) "Kan grubu seçilmedi"
                          else selectedBloodTypes.joinToString(", "),
                icon = Icons.Outlined.Favorite,
                onClick = onBloodTypeClick,
                enabled = enabled
            )
        }
    }
}

@Composable
fun PreferenceItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant 
                  else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalSelectionItem(
    hospital: Hospital,
    isSelected: Boolean,
    onSelectionChanged: () -> Unit,
    enabled: Boolean = true
) {
    ListItem(
        headlineContent = { Text(hospital.name) },
        supportingContent = { 
            Text(
                text = "${hospital.city} - ${hospital.address}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Outlined.Home,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelectionChanged() },
                enabled = enabled
            )
        },
        modifier = Modifier.clickable(
            enabled = enabled,
            onClick = onSelectionChanged
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodTypeSelectionItem(
    bloodType: String,
    isSelected: Boolean,
    onSelectionChanged: () -> Unit,
    enabled: Boolean = true
) {
    ListItem(
        headlineContent = { Text(bloodType) },
        leadingContent = {
            Icon(
                imageVector = Icons.Outlined.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelectionChanged() },
                enabled = enabled
            )
        },
        modifier = Modifier.clickable(
            enabled = enabled,
            onClick = onSelectionChanged
        )
    )
} 