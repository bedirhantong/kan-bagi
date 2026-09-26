package com.ribuufing.bloodapp.feature.map.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ribuufing.bloodapp.feature.listhospitals.data.HospitalResponse

@Composable
fun HospitalHeader(
    hospital: HospitalResponse,
    scrollProgress: Float,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val headerHeight = 250.dp
    val headerAlpha = 1f - (scrollProgress * 2).coerceIn(0f, 0.7f)
    val titleScale = (1f - (scrollProgress * 0.15f)).coerceIn(0.85f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height * 0.6f
                    ),
                )
            }
    ) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                        )
                    )
                )
        )
        
        // Header content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 72.dp)
                .alpha(headerAlpha),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Hospital name
            Text(
                text = hospital.name ?: "Hospital",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = titleScale
                        scaleY = titleScale
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(4.dp),
                        clip = false
                    )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Address with icon
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = hospital.address ?: "No address information",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Quick action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Maps button
                if (hospital.lat != null && hospital.lon != null) {
                    ActionButton(
                        text = "Yol Tarifi",
                        icon = Icons.Filled.LocationOn,
                        onClick = {
                            val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${hospital.lat},${hospital.lon}&destination_name=${Uri.encode(hospital.name)}")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            intent.setPackage("com.google.android.apps.maps")
                            
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback to browser if Maps isn't installed
                                val browserIntent = Intent(Intent.ACTION_VIEW, uri)
                                context.startActivity(browserIntent)
                            }
                        }
                    )
                }
                
                // Call button
                hospital.phoneNumber?.let { phone ->
                    ActionButton(
                        text = "Ara",
                        icon = Icons.Filled.Call,
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL)
                            intent.data = Uri.parse("tel:$phone")
                            context.startActivity(intent)
                        }
                    )
                }
                
                // Website button
                hospital.webSite?.let { website ->
                    ActionButton(
                        text = "Web",
                        icon = Icons.Filled.Info,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW)
                            val uri = if (website.startsWith("http")) website else "https://$website"
                            intent.data = Uri.parse(uri)
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
        
        // Back button
        Box(
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.TopStart)
        ) {
            IconButton(
                onClick = onBackPressed,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f))
            ) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White,
            containerColor = Color.White.copy(alpha = 0.2f)
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = SolidColor(Color.White.copy(alpha = 0.5f)),
            width = 1.dp
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text, 
            fontSize = 12.sp, 
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}