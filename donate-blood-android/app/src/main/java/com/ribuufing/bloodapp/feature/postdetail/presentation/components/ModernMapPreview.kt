package com.ribuufing.bloodapp.feature.postdetail.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ModernMapPreview() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            // Map background pattern
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridSize = 20.dp.toPx()
                val lightGray = Color.LightGray.copy(alpha = 0.5f)
                val mapBackgroundColor = Color(0xFFECEFF1)

                // Fill background
                drawRect(color = mapBackgroundColor)

                // Draw horizontal lines
                for (i in 0..(size.height / gridSize).toInt()) {
                    drawLine(
                        color = lightGray,
                        start = Offset(0f, i * gridSize),
                        end = Offset(size.width, i * gridSize),
                        strokeWidth = 1f
                    )
                }

                // Draw vertical lines
                for (i in 0..(size.width / gridSize).toInt()) {
                    drawLine(
                        color = lightGray,
                        start = Offset(i * gridSize, 0f),
                        end = Offset(i * gridSize, size.height),
                        strokeWidth = 1f
                    )
                }

                // Draw roads
                val roadColor = Color.White
                val roadWidth = 12.dp.toPx()

                // Horizontal main road
                drawRect(
                    color = roadColor,
                    topLeft = Offset(0f, size.height/2 - roadWidth/2),
                    size = androidx.compose.ui.geometry.Size(size.width, roadWidth)
                )

                // Vertical main road
                drawRect(
                    color = roadColor,
                    topLeft = Offset(size.width/2 - roadWidth/2, 0f),
                    size = androidx.compose.ui.geometry.Size(roadWidth, size.height)
                )

                // Draw location marker
                val centerX = size.width / 2
                val centerY = size.height / 2
                val markerSize = 15.dp.toPx()
                val pulseSize = 25.dp.toPx()

                // Draw pulse circle
                drawCircle(
                    color = Color(0xFF1E88E5).copy(alpha = 0.2f),
                    radius = pulseSize,
                    center = Offset(centerX, centerY)
                )

                // Draw circle
                drawCircle(
                    color = Color(0xFF1E88E5),
                    radius = markerSize,
                    center = Offset(centerX, centerY)
                )

                // Draw inner circle
                drawCircle(
                    color = Color.White,
                    radius = markerSize / 2,
                    center = Offset(centerX, centerY)
                )
            }

            // Overlay with semi-transparent card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xAA000000)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Haritada Görüntüle",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}