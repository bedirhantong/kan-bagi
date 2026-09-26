package com.ribuufing.bloodapp.feature.settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val themes = remember {
        listOf(
            Theme(
                "Sistem Varsayılanı",
                "Sistem temasını kullan",
                Icons.Outlined.Check
            ),
            Theme(
                "Açık Tema",
                "Aydınlık mod",
                Icons.Outlined.Check
            ),
            Theme(
                "Koyu Tema",
                "Karanlık mod",
                Icons.Outlined.CheckCircle
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tema Ayarları") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .selectableGroup()
        ) {
            Text(
                text = "Uygulama Teması",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            themes.forEach { theme ->
                ThemeItem(
                    theme = theme,
                    selected = state.theme == theme.name,
                    onSelect = { viewModel.updateTheme(theme.name) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Not: Tema değişikliği anında uygulanacaktır.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Ek tema ayarları
            ListItem(
                headlineContent = { Text("Dinamik Renk") },
                supportingContent = { Text("Material You renk şemasını kullan") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingContent = {
                    Switch(
                        checked = true,
                        onCheckedChange = { }
                    )
                }
            )

            ListItem(
                headlineContent = { Text("AMOLED Karanlık Tema") },
                supportingContent = { Text("Tam siyah arka plan kullan") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingContent = {
                    Switch(
                        checked = false,
                        onCheckedChange = { }
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeItem(
    theme: Theme,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.RadioButton
            )
    ) {
        ListItem(
            headlineContent = { Text(theme.name) },
            supportingContent = { Text(theme.description) },
            leadingContent = {
                Icon(
                    imageVector = theme.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingContent = {
                if (selected) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Seçili",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

data class Theme(
    val name: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) 