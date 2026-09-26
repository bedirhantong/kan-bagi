package com.ribuufing.bloodapp.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun OnboardingScreen(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(page.backgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = page.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .weight(0.4f)
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color(0xFF1A1A1A).copy(alpha = 0.8f)
            )
        }
    }
} 