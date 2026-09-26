package com.ribuufing.bloodapp.feature.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ribuufing.bloodapp.R
import com.ribuufing.bloodapp.navigation.Routes
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val state by settingsViewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Ayarlar") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Hesap Ayarları
            item {
                SettingsSection(title = "Hesap Ayarları") {
                    SettingsItem(
                        icon = Icons.Default.Person,
                        title = "Profil Bilgileri",
                        subtitle = "Kişisel bilgilerinizi düzenleyin",
                        onClick = { navController.navigate(Routes.Profile.route) }
                    )
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = "Bildirim Ayarları",
                        subtitle = "Bildirim tercihlerinizi yönetin",
                        onClick = { navController.navigate(Routes.Notifications.route) }
                    )
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "Doldurduğum Formu Görüntüle",
                        subtitle = "Daha önce doldurduğunuz formu inceleyin",
                        onClick = { navController.navigate("view_form_screen") }
                    )
                }
            }

            // Uygulama Ayarları
            item {
                SettingsSection(title = "Uygulama Ayarları") {
                    SettingsItem(
                        icon = Icons.Default.FavoriteBorder,
                        title = "Tema",
                        subtitle = state.theme,
                        onClick = { navController.navigate(Routes.Theme.route) }
                    )
                    SettingsItem(
                        icon = Icons.Default.Person,
                        title = "Dil",
                        subtitle = state.language,
                        onClick = { navController.navigate(Routes.Language.route) }
                    )
                }
            }

            // Yasal ve Destek
            item {
                SettingsSection(title = "Yasal ve Destek") {
                    SettingsItem(
                        icon = Icons.Default.Close,
                        title = "Gizlilik Politikası",
                        subtitle = "KVKK ve veri işleme politikalarımız",
                        onClick = { navController.navigate(Routes.PrivacyPolicy.route) }
                    )
                    SettingsItem(
                        icon = Icons.Default.Menu,
                        title = "Kullanım Koşulları",
                        subtitle = "Uygulama kullanım şartları",
                        onClick = { navController.navigate(Routes.Terms.route) }
                    )
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "Sıkça Sorulan Sorular",
                        subtitle = "Yardım ve destek",
                        onClick = { navController.navigate(Routes.FAQ.route) }
                    )
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "Hakkında",
                        subtitle = "Uygulama bilgileri ve sürüm",
                        onClick = { navController.navigate(Routes.About.route) }
                    )
                }
            }

            // Çıkış Yap
            item {
                SettingsSection(title = "") {
                    SettingsItem(
                        icon = Icons.Default.ExitToApp,
                        title = "Çıkış Yap",
                        subtitle = "Hesabınızdan çıkış yapın",
                        onClick = {
                            settingsViewModel.logout()
                            navController.navigate(Routes.LoginType.route) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (title.isNotEmpty()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = tint
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}