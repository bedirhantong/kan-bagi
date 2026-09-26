package com.ribuufing.bloodapp.feature.postdetail.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ribuufing.bloodapp.core.utils.BloodAppDialog
import com.ribuufing.bloodapp.feature.postdetail.presentation.components.LoadingAnimationWithLogo
import com.ribuufing.bloodapp.feature.postdetail.presentation.components.ModernInfoRow
import com.ribuufing.bloodapp.feature.postdetail.presentation.components.ModernInfoSection
import com.ribuufing.bloodapp.feature.postdetail.presentation.components.ModernMapPreview
import com.ribuufing.bloodapp.feature.postdetail.presentation.components.QrCodeDialog
import com.ribuufing.bloodapp.feature.postdetail.presentation.viewmodel.PostDetailViewModel
import com.ribuufing.bloodapp.navigation.BottomNavigationItems
import com.ribuufing.bloodapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    navController: NavController,
    postId: String,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showFormConfirmationDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted && uiState.post?.hospital != null) {
            val hospital = uiState.post!!.hospital!!
            openMapsWithDirections(context, hospital.lat, hospital.lon, hospital.name)
        }
    }
    
    var isMapLoading by remember { mutableStateOf(false) }
    
    val navigateToHospital: () -> Unit = {
        val hospital = uiState.post?.hospital
        if (hospital != null && hospital.lat != null && hospital.lon != null) {
            if (hasLocationPermission) {
                isMapLoading = true
                try {
                    openMapsWithDirections(context, hospital.lat, hospital.lon, hospital.name)
                } finally {
                    isMapLoading = false
                }
            } else {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        } else {
            viewModel.setError("Hastane konum bilgisi bulunamadı")
        }
    }

    LaunchedEffect(uiState.shouldNavigateToChat) {
        if (uiState.shouldNavigateToChat && uiState.chatRoomId != null) {
            navController.navigate("chat/${uiState.chatRoomId}") {
                launchSingleTop = true
            }
            viewModel.onChatNavigated()
        }
    }

    if (showDeleteDialog) {
        BloodAppDialog(
            onDismissRequest = { showDeleteDialog = false },
            onConfirmClick = {
                viewModel.deletePost(postId)
                showDeleteDialog = false
            },
            title = "Kan Bağışı İsteğini Sil",
            message = "Bu kan bağışı isteğini silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.",
            icon = Icons.Filled.Warning,
            confirmText = "Sil",
            dismissText = "İptal"
        )
    }

    LaunchedEffect(postId) {
        viewModel.loadPostDetail(postId)
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            navController.navigate(BottomNavigationItems.Home.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    if (uiState.showQrDialog && uiState.qrCodeBase64 != null) {
        val qrCode = uiState.qrCodeBase64!!
        QrCodeDialog(
            qrCodeBase64 = qrCode,
            onDismiss = { viewModel.hideQrDialog() }
        )
    }

    if (showFormConfirmationDialog) {
        BloodAppDialog(
            onDismissRequest = { showFormConfirmationDialog = false },
            onConfirmClick = { 
                viewModel.createMatching()
                showFormConfirmationDialog = false
            },
            title = "Bağış İşleminizi Onaylayın",
            message = "Daha önce doldurduğunuz kan bağışı formunu görüntülemek veya düzenlemek istiyor musunuz? Doğrudan devam ederseniz, mevcut formunuz ile QR kod oluşturulacaktır.",
            icon = Icons.Filled.KeyboardArrowRight,
            showLogo = true,
            confirmText = "QR Göster",
            dismissText = "İptal",
            onAlternativeAction = {
                navController.navigate("view_form_screen")
                showFormConfirmationDialog = false
            },
            alternativeActionText = "Formu Görüntüle"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Kan Bağışı Detayı",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Geri", modifier = Modifier.size(28.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bloodRed,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                modifier = Modifier.shadow(elevation = 8.dp)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp),
                color = Color.White,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ElevatedButton(
                        onClick = { /* Call action */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color.White,
                            contentColor = accentColor
                        ),
                        shape = RoundedCornerShape(24.dp),
                        elevation = ButtonDefaults.elevatedButtonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Icon(
                            Icons.Filled.Call,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "ARA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    ElevatedButton(
                        onClick = { viewModel.handleChatNavigation() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = accentColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp),
                        elevation = ButtonDefaults.elevatedButtonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Icon(
                            Icons.Filled.Create,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "MESAJ AT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(surfaceColor)
        ) {
            if (uiState.isLoading) {
                LoadingAnimationWithLogo(bloodRed)
            }

            uiState.error?.let { error ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = bloodRed
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(error, textAlign = TextAlign.Center, color = textPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadPostDetail(postId) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = bloodRed
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tekrar Dene")
                    }
                }
            }

            AnimatedVisibility(
                visible = !uiState.isLoading && uiState.error == null && uiState.post != null,
                enter = fadeIn(initialAlpha = 0.3f),
                exit = fadeOut()
            ) {
                uiState.post?.let { post ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 2.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SuggestionChip(
                                        onClick = { /* No action */ },
                                        label = {
                                            Text(post.bloodType ?: "?", fontWeight = FontWeight.Bold)
                                        },
                                        icon = {
                                            Icon(
                                                Icons.Outlined.Favorite,
                                                contentDescription = null,
                                                tint = bloodRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = bloodRedLight,
                                            labelColor = bloodRed,
                                            iconContentColor = bloodRed
                                        ),
                                        border = null
                                    )
                                    
                                    Text(
                                        text = "2 saat önce",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textSecondary
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = post.title ?: "Başlık yok",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary,
                                        lineHeight = 28.sp
                                    )
                                )

                                Text(
                                    text = post.description ?: "Açıklama yok",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = textSecondary,
                                        lineHeight = 24.sp
                                    )
                                )
                            }
                        }

                        // Patient info with modern design
                        ModernInfoSection(
                            title = "Hasta Bilgileri",
                            icon = Icons.Outlined.Person,
                            iconTint = accentColor,
                            backgroundColor = Color.White
                        ) {
                            ModernInfoRow(
                                icon = Icons.Outlined.Person,
                                label = "Hasta Adı",
                                value = post.patientFullName ?: "İsim yok",
                                iconTint = accentColor
                            )

                            ModernInfoRow(
                                icon = Icons.Outlined.DateRange,
                                label = "Yaş",
                                value = "${post.patientAge} yaş",
                                iconTint = accentColor
                            )

                            ModernInfoRow(
                                icon = Icons.Outlined.Favorite,
                                label = "Kan Grubu",
                                value = post.bloodType ?: "Bilinmiyor",
                                iconTint = bloodRed,
                                valueColor = bloodRed
                            )
                        }

                        // Hospital info with modern design
                        ModernInfoSection(
                            title = "Hastane Bilgileri",
                            icon = Icons.Outlined.Home,
                            iconTint = accentColor,
                            backgroundColor = Color.White
                        ) {
                            ModernInfoRow(
                                icon = Icons.Outlined.Home,
                                label = "Hastane",
                                value = post.hospital?.name ?: "Hastane adı yok",
                                iconTint = accentColor
                            )

                            ModernInfoRow(
                                icon = Icons.Outlined.LocationOn,
                                label = "Adres",
                                value = post.hospital?.address ?: "Adres yok",
                                iconTint = accentColor
                            )

                            // Modern Map Preview
                            ModernMapPreview()

                            FilledTonalButton(
                                onClick = navigateToHospital,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = accentColorLight,
                                    contentColor = accentColor
                                ),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isMapLoading
                            ) {
                                if (isMapLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = accentColor
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.LocationOn,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Yol Tarifi Al",
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        ModernInfoSection(
                            title = "İletişim Bilgileri",
                            icon = Icons.Outlined.Call,
                            iconTint = accentColor,
                            backgroundColor = Color.White
                        ) {
                            ModernInfoRow(
                                icon = Icons.Outlined.Person,
                                label = "İstek Sahibi",
                                value = "${post.ownerName} ${post.ownerSurname}",
                                iconTint = accentColor
                            )

                            post.phoneNumbers?.forEachIndexed { index, phone ->
                                ModernInfoRow(
                                    icon = Icons.Outlined.Phone,
                                    label = if (index == 0) "Telefon" else "Telefon ${index + 1}",
                                    value = phone,
                                    iconTint = accentColor,
                                    isClickable = true,
                                    onClick = { /* Call phone number */ }
                                )
                            }
                        }

                        // Donate button with modern design
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 2.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Bağış Yapmak İster Misiniz?",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    "QR kodu ile hastanede eşleşmeyi tamamlayabilirsiniz",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textSecondary,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Button(
                                    onClick = { showFormConfirmationDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = bloodRed,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(26.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Favorite,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "QR Oluştur",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

private fun openMapsWithDirections(context: android.content.Context, lat: Double?, lon: Double?, hospitalName: String?) {
    if (lat == null || lon == null) return
    
    try {
        val encodedHospitalName = Uri.encode(hospitalName ?: "Hastane")
        // Use directions mode instead of direct navigation to show the route first
        val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lon&destination_name=$encodedHospitalName&travelmode=driving")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps")
        
        // Check if Google Maps is installed
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // Fallback to browser if Google Maps app is not installed
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(browserIntent)
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Harita uygulaması açılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}