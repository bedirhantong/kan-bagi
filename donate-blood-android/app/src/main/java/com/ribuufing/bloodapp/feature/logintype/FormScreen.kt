package com.ribuufing.bloodapp.feature.logintype

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ribuufing.bloodapp.feature.form.domain.response.GetQuestionsResponse
import com.ribuufing.bloodapp.feature.form.presentation.FormViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.ribuufing.bloodapp.feature.form.presentation.FormSubmitState
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    navController: NavController,
    viewModel: FormViewModel = hiltViewModel()
) {
    val questions by viewModel.formQuestions.collectAsState()
    val answers by viewModel.formAnswers.collectAsState()
    val submitState by viewModel.formSubmitState.collectAsState()
    val context = LocalContext.current
    
    // Track unanswered questions for validation
    val unansweredQuestions = remember(questions, answers) {
        questions.filter { question ->
            question.value != null && !answers.containsKey(question.value)
        }
    }
    
    var showValidationError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.startFormFlow()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kan Bağışı Formu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(MaterialTheme.colorScheme.background)) {
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Form header
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Kan Bağışı Uygunluk Formu",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Tüm soruları cevaplayınız. Bütün bilgiler gizli tutulacaktır.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Error message
                if (submitState is FormSubmitState.Error) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Clear, 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = (submitState as FormSubmitState.Error).message,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
                
                // Validation error message
                if (showValidationError && unansweredQuestions.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Clear, 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Lütfen tüm soruları yanıtlayın (${unansweredQuestions.size} soru cevaplanmadı)",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
                
                // Questions
                items(questions.sortedBy { it.value ?: 0 }, key = { it.value ?: it.key ?: "" }) { question ->
                    FormQuestionCard(
                        question = question,
                        selectedValue = answers[question.value],
                        onValueChange = { ans ->
                            if (question.value != null) viewModel.updateFormAnswer(question.value, ans)
                        }
                    )
                }
                
                // Submit Button
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { 
                            if (unansweredQuestions.isEmpty()) {
                                viewModel.submitForm()
                                showValidationError = false
                            } else {
                                showValidationError = true
                            }
                        },
                        enabled = submitState !is FormSubmitState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (submitState is FormSubmitState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "GÖNDER",
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                    // Bottom spacer
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
            
            // Success dialog handling with navigation
            if (submitState is FormSubmitState.Success) {
                LaunchedEffect(Unit) {
                    Toast.makeText(context, "Form başarıyla gönderildi", Toast.LENGTH_SHORT).show()
                    navController.navigate(com.ribuufing.bloodapp.navigation.BottomNavigationItems.Home.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                    viewModel.closeFormDialog()
                }
            }
        }
    }
}

@Composable
fun FormQuestionCard(
    question: GetQuestionsResponse,
    selectedValue: String?,
    onValueChange: (String) -> Unit
) {
    val selectedBool = selectedValue?.toBooleanStrictOrNull()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = question.displayname ?: beautifyQuestionKey(question.key ?: "Soru"),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            if (!question.displayname.isNullOrBlank() && question.displayname != question.key && question.key != null) {
                Text(
                    text = question.key,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Yes option
                FilterChip(
                    selected = selectedBool == true,
                    onClick = { onValueChange("true") },
                    label = { Text("Evet") },
                    leadingIcon = if (selectedBool == true) {
                        { Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.weight(1f)
                )
                
                // No option
                FilterChip(
                    selected = selectedBool == false,
                    onClick = { onValueChange("false") },
                    label = { Text("Hayır") },
                    leadingIcon = if (selectedBool == false) {
                        { Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// Helper function to make question keys more readable
private fun beautifyQuestionKey(key: String): String {
    return key.replace("_", " ")
        .lowercase()
        .replaceFirstChar { it.uppercase() }
}