package com.ribuufing.bloodapp.feature.onboarding

import androidx.compose.ui.graphics.Color

data class OnboardingPage(
    val imageUrl: String = "",
    val title: String,
    val description: String,
    val backgroundColor: Color = Color(0xFFF5F5F5)
)