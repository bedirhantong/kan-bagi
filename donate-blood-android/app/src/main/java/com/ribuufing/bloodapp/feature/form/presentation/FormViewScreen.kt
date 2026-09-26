package com.ribuufing.bloodapp.feature.form.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.ribuufing.bloodapp.feature.form.domain.response.GetQuestionsResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormViewScreen(
    viewModel: FormViewModel = hiltViewModel(),
    navController: NavController
) {
    val formState by viewModel.formViewState.collectAsState()
    val questions by viewModel.formQuestions.collectAsState()
    val answers by viewModel.formAnswers.collectAsState()
    val originalAnswers = remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    val updateState by viewModel.updateState.collectAsState()
    
    // Track if changes have been made to the form
    val hasChanges = remember(answers, originalAnswers.value) {
        answers != originalAnswers.value && originalAnswers.value.isNotEmpty()
    }

    // Track unanswered questions for validation
    val unansweredQuestions = remember(questions, answers) {
        questions.filter { question ->
            question.value != null && !answers.containsKey(question.value)
        }
    }
    
    var showValidationError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // First load questions, then load form for edit
        viewModel.loadQuestions()
    }
    
    // Load form after questions are loaded and parse formData
    LaunchedEffect(formState.questionsLoaded) {
        if (formState.questionsLoaded) {
            viewModel.loadFormForEdit()
        }
    }
    
    // Store original answers when first loaded
    LaunchedEffect(formState) {
        if (formState.form != null && originalAnswers.value.isEmpty()) {
            originalAnswers.value = answers
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kan Bağışı Formu") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    if (hasChanges) {
                        // Clear button - revert to original answers
                        IconButton(onClick = {
                            // Restore original answers
                            originalAnswers.value.forEach { (id, value) ->
                                viewModel.updateFormAnswer(id, value)
                            }
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Temizle")
                        }
                        
                        // Save button
                        IconButton(onClick = {
                            if (unansweredQuestions.isEmpty()) {
                                viewModel.updateForm()
                                showValidationError = false
                            } else {
                                showValidationError = true
                            }
                        }) {
                            Icon(Icons.Default.Send, contentDescription = "Kaydet")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                formState.loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                formState.error != null -> {
                    Text(
                        text = formState.error ?: "Bir hata oluştu",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                formState.form != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Form header
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

                        // Validation error message
                        if (showValidationError && unansweredQuestions.isNotEmpty()) {
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
                        
                        // Success message
                        if (updateState is FormUpdateState.Success) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFDCEDC8) // Light green
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Send,
                                        contentDescription = null,
                                        tint = Color(0xFF33691E) // Dark green
                                    )
                                    Text(
                                        text = "Form başarıyla kaydedildi!",
                                        color = Color(0xFF33691E),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        
                        // Error message
                        if (updateState is FormUpdateState.Error) {
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
                                        text = (updateState as FormUpdateState.Error).message,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        
                        // Questions
                        questions.sortedBy { it.value ?: 0 }.forEach { question ->
                            FormQuestionCard(
                                question = question,
                                selectedValue = answers[question.value],
                                onValueChange = { newAnswer ->
                                    if (question.value != null) {
                                        viewModel.updateFormAnswer(question.value, newAnswer)
                                    }
                                }
                            )
                        }
                        
                        // Bottom spacer
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                
                    // Save button at the bottom
                    if (hasChanges) {
                        Button(
                            onClick = {
                                if (unansweredQuestions.isEmpty()) {
                                    viewModel.updateForm()
                                    showValidationError = false
                                } else {
                                    showValidationError = true
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "KAYDET",
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
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