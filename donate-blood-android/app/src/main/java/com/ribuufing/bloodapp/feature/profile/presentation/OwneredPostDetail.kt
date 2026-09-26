package com.ribuufing.bloodapp.feature.profile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ribuufing.bloodapp.feature.home.data.dto.PostResponse
import kotlinx.coroutines.flow.collectLatest
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class OwneredPostDetailUiState(
    val post: PostResponse? = null,
    val isLoading: Boolean = false
)

sealed class OwneredPostDetailUiEvent {
    data class ShowMessage(val message: String, val isSuccess: Boolean): OwneredPostDetailUiEvent()
    object NavigateToQr: OwneredPostDetailUiEvent()
    object PostDeleted: OwneredPostDetailUiEvent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwneredPostDetail(
    navController: NavController,
    postId: String,
    userId: String,
    viewModel: OwneredPostDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Dialog state
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var pendingUpdateTitle by remember { mutableStateOf("") }
    var pendingUpdateDescription by remember { mutableStateOf("") }

    // Snackbar event collect
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is OwneredPostDetailUiEvent.ShowMessage -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
                is OwneredPostDetailUiEvent.NavigateToQr -> {
                    navController.navigate("qr_scan")
                }
                is OwneredPostDetailUiEvent.PostDeleted -> {
                    navController.popBackStack()
                }
            }
        }
    }

    LaunchedEffect(postId) {
        viewModel.loadPostDetail(postId)
    }

    val accentColor = Color(0xFF1E88E5)
    val bloodRed = Color(0xFFE53935)

    // DIALOGS
    if (showDeleteDialog) {
        ConfirmDialog(
            title = "İlanı Sil",
            message = "Bu ilanı silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.",
            confirmText = "Sil",
            dismissText = "İptal",
            icon = Icons.Default.Warning,
            confirmColor = bloodRed,
            onConfirm = {
                showDeleteDialog = false
                viewModel.deletePost(state.post?.id ?: "")
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
    if (showUpdateDialog) {
        ConfirmDialog(
            title = "İlanı Güncelle",
            message = "Değişiklikleri kaydetmek istediğinizden emin misiniz?",
            confirmText = "Kaydet",
            dismissText = "İptal",
            icon = Icons.Default.ThumbUp,
            confirmColor = accentColor,
            onConfirm = {
                showUpdateDialog = false
                state.post?.let {
                    viewModel.updatePost(it.copy(title = pendingUpdateTitle, description = pendingUpdateDescription))
                }
            },
            onDismiss = { showUpdateDialog = false }
        )
    }
    if (showStatusDialog) {
        ConfirmDialog(
            title = if (state.post?.isActive == true) "İlanı Pasifleştir" else "İlanı Aktifleştir",
            message = if (state.post?.isActive == true) "İlanı pasif yapmak istediğinizden emin misiniz?" else "İlanı tekrar aktif yapmak istediğinizden emin misiniz?",
            confirmText = if (state.post?.isActive == true) "Pasifleştir" else "Aktifleştir",
            dismissText = "İptal",
            icon = if (state.post?.isActive == true) Icons.Outlined.Close else Icons.Outlined.Check,
            confirmColor = accentColor,
            onConfirm = {
                showStatusDialog = false
                state.post?.let { viewModel.toggleStatus(it) }
            },
            onDismiss = { showStatusDialog = false }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "İlan Detayı",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bloodRed,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.post?.isActive == true) {
                Row(
                    modifier = Modifier.padding(end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.navigateToQr() },
                        icon = { Icon(Icons.Default.Menu, contentDescription = null) },
                        text = { Text("QR Okut") },
                        containerColor = accentColor,
                        contentColor = Color.White
                    )
                    ExtendedFloatingActionButton(
                        onClick = { showStatusDialog = true },
                        icon = {
                            Icon(
                                Icons.Outlined.MoreVert,
                                contentDescription = null
                            )
                        },
                        text = { Text("Pasifleştir") },
                        containerColor = Color.Gray,
                        contentColor = Color.White
                    )
                }
            } else {
                ExtendedFloatingActionButton(
                    onClick = { showStatusDialog = true },
                    icon = {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = null
                        )
                    },
                    text = { Text("Aktifleştir")},
                    containerColor = accentColor,
                    contentColor = Color.White,
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding).
            padding(bottom = 40.dp)
        ) {
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            state.post?.let { post ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            var title by remember { mutableStateOf(post.title ?: "") }
                            var description by remember { mutableStateOf(post.description ?: "") }
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Başlık") },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                                enabled = post.isActive == true
                            )
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Açıklama") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = post.isActive == true
                            )
                            if (post.isActive == true) {
                                Button(
                                    onClick = {
                                        pendingUpdateTitle = title
                                        pendingUpdateDescription = description
                                        showUpdateDialog = true
                                    },
                                    modifier = Modifier.align(Alignment.End),
                                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                                ) {
                                    Icon(Icons.Default.ThumbUp, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Kaydet")
                                }
                            }
                        }
                    }

                    // Hasta Bilgileri
                    InfoCard(
                        title = "Hasta Bilgileri",
                        icon = Icons.Outlined.Person,
                        iconTint = accentColor
                    ) {
                        InfoRow(Icons.Outlined.Person, "Hasta Adı", post.patientFullName ?: "-")
                        InfoRow(Icons.Outlined.DateRange, "Yaş", post.patientAge?.toString() ?: "-")
                        InfoRow(Icons.Outlined.Favorite, "Kan Grubu", post.bloodType ?: "-")
                    }

                    // Hastane Bilgileri
                    InfoCard(
                        title = "Hastane Bilgileri",
                        icon = Icons.Outlined.Home,
                        iconTint = accentColor
                    ) {
                        InfoRow(Icons.Outlined.AccountBox, "Hastane", post.hospital?.name ?: "-")
                        InfoRow(Icons.Outlined.LocationOn, "Adres", post.hospital?.address ?: "-")
                    }

                    // İletişim Bilgileri
                    InfoCard(
                        title = "İletişim Bilgileri",
                        icon = Icons.Outlined.Call,
                        iconTint = accentColor
                    ) {
                        InfoRow(Icons.Outlined.Person, "İstek Sahibi", "${post.ownerName} ${post.ownerSurname}")
                        post.phoneNumbers?.forEach { phone ->
                            InfoRow(Icons.Outlined.Phone, "Telefon", phone)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(title: String, icon: ImageVector, iconTint: Color, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Divider(color = Color.LightGray, thickness = 1.dp)
            content()
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label + ":", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray, fontWeight = FontWeight.Medium), modifier = Modifier.width(100.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black))
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    icon: ImageVector,
    confirmColor: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(icon, contentDescription = null, tint = confirmColor, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Text(message, style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(dismissText)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = confirmColor)
                    ) {
                        Text(confirmText, color = Color.White)
                    }
                }
            }
        }
    }
}