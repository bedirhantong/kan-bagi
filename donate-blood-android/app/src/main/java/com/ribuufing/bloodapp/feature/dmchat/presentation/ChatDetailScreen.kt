package com.ribuufing.bloodapp.feature.dmchat.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.layout.Box
import com.ribuufing.bloodapp.feature.dmchat.presentation.components.ChatBackground
import com.ribuufing.bloodapp.feature.dmchat.presentation.components.ChatErrorState
import com.ribuufing.bloodapp.feature.dmchat.presentation.components.ChatLoadingState
import com.ribuufing.bloodapp.feature.dmchat.presentation.components.ChatMessageInput
import com.ribuufing.bloodapp.feature.dmchat.presentation.components.ChatTopBar
import com.ribuufing.bloodapp.feature.dmchat.presentation.components.DateSeparator
import com.ribuufing.bloodapp.feature.dmchat.presentation.components.MessageBubble

private fun String.capitalize(): String {
    return this.replaceFirstChar { 
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatDetailScreen(
    navController: NavController,
    roomId: String,
    viewModel: ChatDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val chatPartnerName = remember(uiState.messages) {
        uiState.messages.firstOrNull()?.let { message ->
            if (message.sender_user_id == uiState.currentUserId) {
                message.receiver_fullname
            } else {
                message.sender_fullname
            }
        } ?: "Sohbet"
    }

    LaunchedEffect(roomId) {
        viewModel.loadChatMessages(roomId)
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.scrollToItem(uiState.messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            ChatTopBar(
                title = chatPartnerName ?: "Sohbet",
                subtitle = if (uiState.isConnected) "bağlı" else "bağlanıyor...",
                showOnlineStatus = true,
                isOnline = uiState.isConnected,
                onBackClick = { navController.navigateUp() },
                onCallClick = { /* İleride arama özelliği eklenebilir */ },
                onMoreOptionsClick = { /* İleride daha fazla seçenek eklenebilir */ }
            )
        },
        bottomBar = {
            ChatMessageInput(
                value = messageText,
                onValueChange = { messageText = it },
                onSendClick = {
                    if (messageText.isNotEmpty()) {
                        viewModel.sendMessage(messageText)
                        messageText = ""
                        coroutineScope.launch {
                            if (uiState.messages.isNotEmpty()) {
                                listState.animateScrollToItem(uiState.messages.size)
                            }
                        }
                    }
                },
                onAttachmentClick = { /* İleride dosya ekleme özelliği eklenebilir */ },
                onEmojiClick = { /* İleride emoji seçici eklenebilir */ },
                onVoiceClick = { /* İleride sesli mesaj özelliği eklenebilir */ }
            )
        }
    ) { paddingValues ->
        ChatBackground {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (uiState.isLoading) {
                    ChatLoadingState()
                } else if (uiState.error != null) {
                    ChatErrorState(
                        errorMessage = uiState.error ?: "Bir hata oluştu",
                        onRetry = { viewModel.loadChatMessages(roomId) }
                    )
                } else {
                    val groupedMessages = uiState.messages.groupBy { message ->
                        message.timestamp?.let {
                            try {
                                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
                                val date = format.parse(it)
                                val calendar = Calendar.getInstance()
                                calendar.time = date
                                
                                val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                dayFormat.format(date)
                            } catch (e: Exception) {
                                "unknown_date"
                            }
                        } ?: "unknown_date"
                    }
                    
                    val sortedDates = groupedMessages.keys.sortedBy { dateStr ->
                        try {
                            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            format.parse(dateStr).time
                        } catch (e: Exception) {
                            0L
                        }
                    }
                    
                    val now = Calendar.getInstance()
                    val yesterday = Calendar.getInstance().apply { 
                        add(Calendar.DAY_OF_YEAR, -1) 
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState
                    ) {
                        sortedDates.forEach { dateStr ->
                            item {
                                val date = try {
                                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    val parsedDate = format.parse(dateStr)
                                    val cal = Calendar.getInstance().apply { time = parsedDate }
                                    
                                    when {
                                        // Same day
                                        cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                                        cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) -> "Bugün"
                                        
                                        // Yesterday
                                        cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                                        cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "Dün"
                                        
                                        // This week
                                        cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                                        cal.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR) -> {
                                            val dayFormat = SimpleDateFormat("EEEE", Locale("tr"))
                                            dayFormat.format(cal.time).capitalize()
                                        }
                                        
                                        // This year
                                        cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) -> {
                                            val dateFormat = SimpleDateFormat("d MMMM", Locale("tr"))
                                            dateFormat.format(cal.time)
                                        }
                                        
                                        // Other years
                                        else -> {
                                            val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("tr"))
                                            dateFormat.format(cal.time)
                                        }
                                    }
                                } catch (e: Exception) {
                                    dateStr
                                }
                                
                                DateSeparator(text = date)
                            }
                            
                            val messagesForDate = groupedMessages[dateStr] ?: listOf()
                            items(messagesForDate) { message ->
                                MessageBubble(
                                    message = message,
                                    isOwnMessage = message.sender_user_id == uiState.currentUserId,
                                    onLongClick = { /* İleride mesaj seçimi işlevselliği eklenebilir */ }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}