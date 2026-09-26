package com.ribuufing.bloodapp.feature.settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
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
fun LanguageSettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val languages = remember {
        listOf(
            Language("Türkçe", "tr"),
            Language("English", "en"),
            Language("العربية", "ar"),
            Language("Deutsch", "de"),
            Language("Español", "es"),
            Language("Français", "fr"),
            Language("Русский", "ru")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dil Ayarları") },
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
                text = "Uygulama Dili",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            languages.forEach { language ->
//                LanguageItem(
//                    language = language,
//                    selected = state.language == language.displayName,
//                    onSelect = {  }
//                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Not: Dil değişikliği uygulamanın yeniden başlatılmasını gerektirebilir.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageItem(
    language: Language,
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
            headlineContent = { Text(language.displayName) },
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

data class Language(
    val displayName: String,
    val code: String
) 