package com.ribuufing.bloodapp.feature.dmchat.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.ribuufing.bloodapp.R

@Composable
fun ChatBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Background Image with overlay gradient
        Image(
            painter = painterResource(id = R.drawable.chat_bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.25f), // Daha görünür bir arkaplan
            contentScale = ContentScale.Crop
        )
        
        // Semi-transparent gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.5f), // Daha saydam
                            MaterialTheme.colorScheme.background.copy(alpha = 0.7f), // Daha saydam
                        )
                    )
                )
        )
        
        // Content
        content()
    }
}