package com.ribuufing.bloodapp.feature.dmchat.presentation

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ribuufing.bloodapp.feature.dmchat.domain.model.GetRoomsResponse
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun DmChatsListScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Mesajlar",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Geri",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Animated Search Bar
            AnimatedVisibility(
                visible = showSearchBar,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { /* Handle search */ },
                    active = showSearchBar,
                    onActiveChange = { /* No-op */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Kişi ara") },
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Ara",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = { 
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Temizle"
                                )
                            }
                        }
                    }
                ) {
                    // Search suggestions would go here
                }
            }

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                } else if (uiState.error != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.error ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        FilledTonalButton(
                            onClick = { viewModel.refresh() },
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tekrar Dene")
                        }
                    }
                } else if (uiState.chats.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Henüz mesajınız yok",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Yeni mesajlaşma başlatarak kan bağışçılarıyla iletişime geçebilirsiniz",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        itemsIndexed(uiState.chats.filter { chat ->
                            val searchLower = searchQuery.lowercase()
                            searchQuery.isEmpty() // || chat.otherUserName.lowercase().contains(searchLower)
                        }) { index, chat ->
                            ChatItem(
                                chat = chat,
                                currentUserId = uiState.currentUserId,
                                onClick = {
                                    navController.navigate("chat/${chat.room_id}")
                                }
                            )
                            
                            // Add divider except for last item
                            if (index < uiState.chats.size - 1) {
                                Divider(
                                    modifier = Modifier.padding(start = 84.dp, end = 16.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                    
                    // Pull-to-refresh would go here in a real implementation
                }
            }
        }
    }
}

@Composable
fun ChatItem(
    chat: GetRoomsResponse,
    currentUserId: String?,
    onClick: () -> Unit
) {
    // Format timestamp based on how old the message is
    val timeText = remember(chat.last_message?.timestamp) {
        chat.last_message?.timestamp?.let { timestamp ->
            try {
                // Parse the original timestamp
                val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
                val messageDate = originalFormat.parse(timestamp)
                val currentDate = Calendar.getInstance()
                val messageCalendar = Calendar.getInstance()
                messageCalendar.time = messageDate

                // Calculate the difference in days
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val messageDayStart = Calendar.getInstance().apply {
                    time = messageDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val daysDifference = ((today.timeInMillis - messageDayStart.timeInMillis) / (24 * 60 * 60 * 1000)).toInt()

                when {
                    // Today - show only time
                    daysDifference == 0 -> {
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(messageDate)
                    }
                    // Yesterday
                    daysDifference == 1 -> "dün"
                    // Older messages - show date
                    else -> {
                        SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(messageDate)
                    }
                }
            } catch (e: Exception) {
                "??:??"
            }
        } ?: "yeni"
    }

    // Get the other user's name based on current user ID
    val otherUserName = if (currentUserId == chat.user1_id) {
        chat.user2_fullname ?: "Kullanıcı"
    } else {
        chat.user1_fullname ?: "Kullanıcı"
    }

    // Determine if this is an unread message
    val isUnread = chat.last_message?.let { lastMessage ->
        // This is a placeholder. In a real app, you'd track read status
        false
    } ?: false
    
    val nameStyle = if (isUnread) {
        MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
    } else {
        MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Picture with random background color based on name
            val initialChar = otherUserName.firstOrNull()?.uppercase() ?: "?"
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        color = when (initialChar.hashCode() % 5) {
                            0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            1 -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                            2 -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
                            3 -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initialChar,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // Message Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = otherUserName,
                    style = nameStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                chat.last_message?.let { lastMessage ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isUnread) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        
                        Text(
                            text = lastMessage.content ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isUnread) 
                                MaterialTheme.colorScheme.onSurface
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Timestamp with dot indicator for unread
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isUnread) 
                        MaterialTheme.colorScheme.primary
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}