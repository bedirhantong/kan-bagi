package com.ribuufing.bloodapp.feature.home.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ribuufing.bloodapp.feature.home.presentation.StoryBoard

@Composable
fun Story(
    story: StoryBoard,
    onStoryClick: () -> Unit
) {
    val storySize = 75.dp
    val gradientColors = listOf(
        Color(0xFFFF5673),
        Color(0xFFFF8C48)
    )
    
    Column(
        modifier = Modifier.width(storySize),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AsyncImage(
            model = story.icon,
            contentDescription = "Story for ${story.name}",
            modifier = Modifier
                .size(storySize)
                .clip(CircleShape)
                .border(
                    width = 2.5.dp,
                    brush = Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(100f, 100f)
                    ),
                    shape = CircleShape
                )
                .padding(3.dp)
                .clip(CircleShape)
                .clickable(onClick = onStoryClick),
            contentScale = ContentScale.Crop
        )
        
        Text(
            text = story.name,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(storySize)
        )
    }
}