package com.ribuufing.bloodapp.feature.dmchat.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ribuufing.bloodapp.feature.dmchat.domain.model.LastMessage
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageBubble(
    message: LastMessage,
    isOwnMessage: Boolean,
    onLongClick: (LastMessage) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var isSelected by remember { mutableStateOf(false) }
    var isPressing by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    
    // Telegram tarzı mezgj renkleri
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> if (isOwnMessage) 
                           Color(0xFF1E88E5) 
                         else 
                           Color(0xFFE0E0E0)
            isPressing -> if (isOwnMessage) 
                           Color(0xFF1976D2) 
                         else 
                           Color(0xFFD0D0D0)
            isOwnMessage -> Color(0xFF2196F3) // Daha canlı mavi
            else -> Color(0xFFEEEEEE) // Hafif gri
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "Background Color Animation"
    )
    
    val textColor by animateColorAsState(
        targetValue = when {
            isSelected -> if (isOwnMessage) Color.White else Color.Black
            isOwnMessage -> Color.White
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "Text Color Animation"
    )
    
    // Telegram tarzında daha yuvarlatılmış baloncuk şekli
    val bubbleShape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (isOwnMessage) 18.dp else 6.dp,
        bottomEnd = if (isOwnMessage) 6.dp else 18.dp
    )

    val formattedTime = remember(message.timestamp) {
        message.timestamp?.let { timestamp ->
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
                    daysDifference == 1 -> "dün " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(messageDate)
                    // Older messages - show date
                    else -> {
                        SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(messageDate)
                    }
                }
            } catch (e: Exception) {
                "??:??"
            }
        } ?: "??:??"
    }

    // Gölge rengi
    val shadowColor = if (isOwnMessage) Color(0x801976D2) else Color(0x40000000)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
    ) {
        // Mesaj Balonu
        Box(
            modifier = Modifier
                .padding(
                    start = if (isOwnMessage) 64.dp else 8.dp,
                    end = if (isOwnMessage) 8.dp else 64.dp,
                    top = 2.dp,
                    bottom = 2.dp
                )
                .shadow(
                    elevation = if (isPressing) 1.dp else 2.dp,
                    shape = bubbleShape,
                    spotColor = shadowColor.copy(alpha = if (isPressing) 0.4f else 0.7f),
                    ambientColor = shadowColor.copy(alpha = if (isPressing) 0.3f else 0.5f)
                )
                .clip(bubbleShape)
                .background(backgroundColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { /* Basit dokunuş için action yok */ }
                .pointerInput(message.content) {
                    detectTapGestures(
                        onPress = {
                            isPressing = true
                            tryAwaitRelease()
                            isPressing = false
                        },
                        onLongPress = {
                            isSelected = !isSelected
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongClick(message)
                        }
                    )
                }
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = 8.dp, 
                    bottom = 8.dp
                )
            ) {
                // Mesaj içeriği
                Text(
                    text = message.content ?: "",
                    color = textColor.copy(alpha = 0.95f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // Saat ve iletildi durumu
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                 ) {
                     Text(
                         text = formattedTime,
                         color = textColor.copy(alpha = 0.7f),
                         style = MaterialTheme.typography.bodySmall,
                         fontSize = 12.sp
                     )
                    
                    if (isOwnMessage) {
                        Spacer(modifier = Modifier.width(4.dp))
                        // Telegram tarzı çift tik
                        Row(
                            modifier = Modifier.padding(1.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // İlk tik
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(
                                        color = textColor.copy(alpha = 0.8f),
                                        shape = CircleShape
                                    )
                            )
                            
                            Spacer(modifier = Modifier.width(1.dp))
                            
                            // İkinci tik
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(
                                        color = textColor.copy(alpha = 0.8f), 
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                 }
             }
         }
    }
}