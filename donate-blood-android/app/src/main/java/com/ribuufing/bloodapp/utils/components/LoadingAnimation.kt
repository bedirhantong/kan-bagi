package com.ribuufing.bloodapp.utils.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ribuufing.bloodapp.R
import com.ribuufing.bloodapp.utils.loading.LoadingManager

@Composable
fun CentralLoadingAnimation() {
    val isLoading by LoadingManager.isLoading.collectAsState()
    
    if (isLoading) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                color = Color.Transparent
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Rotating circular progress
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(90.dp)
                            .alpha(0.5f),
                        color = Color(0xFFFF9FB7),
                        strokeWidth = 2.dp
                    )

                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Loading",
                        modifier = Modifier
                            .size(90.dp)
                    )
                }
            }
        }
    }
} 