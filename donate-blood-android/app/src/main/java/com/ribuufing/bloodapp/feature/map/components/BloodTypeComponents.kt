package com.ribuufing.bloodapp.feature.map.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodTypeFilter(
    selectedType: String? = null,
    onFilterChange: (String?) -> Unit
) {
    val bloodTypes = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "0+", "0-")
    var selectedBloodType by remember { mutableStateOf(selectedType) }
    
    Column {
        Text(
            text = "Kan Grupları",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
        ) {
            item { 
                FilterChip(
                    selected = selectedBloodType == null,
                    onClick = { 
                        selectedBloodType = null
                        onFilterChange(null)
                    },
                    label = { Text("Tümü") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
            
            items(bloodTypes) { type ->
                FilterChip(
                    selected = selectedBloodType == type,
                    onClick = { 
                        selectedBloodType = if (selectedBloodType == type) null else type
                        onFilterChange(if (selectedBloodType == type) null else type)
                    },
                    label = { Text(type) },
                    leadingIcon = if (selectedBloodType == type) {
                        { 
                            Icon(
                                Icons.Filled.Check, 
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE53935),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

/**
 * A component that displays blood type availability status
 */
@Composable
fun BloodAvailability() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Random availability data for demo purposes
        val bloodTypes = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "0+", "0-")
        val levels = bloodTypes.associateWith { 
            when (Random.nextInt(3)) {
                0 -> "Kritik Düşük"
                1 -> "Normal"
                else -> "Yeterli"
            }
        }

        bloodTypes.chunked(4).forEach { chunk ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                chunk.forEach { type ->
                    BloodTypeStatusCard(
                        bloodType = type,
                        status = levels[type] ?: "Bilinmiyor",
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
        }
    }
}

/**
 * A card displaying blood type status information
 * @param bloodType The blood type (e.g. A+)
 * @param status The availability status (e.g. "Critical")
 * @param modifier Modifier for the card
 */
@Composable
fun BloodTypeStatusCard(
    bloodType: String,
    status: String,
    modifier: Modifier = Modifier
) {
    val color = when (status) {
        "Kritik Düşük" -> Color(0xFFE53935)
        "Normal" -> Color(0xFFFFA000)
        "Yeterli" -> Color(0xFF43A047)
        else -> Color.Gray
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = if (status == "Kritik Düşük") 0.7f else 0.1f,
        targetValue = if (status == "Kritik Düşük") 0.2f else 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Card(
        modifier = modifier
            .padding(4.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = animatedAlpha)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (status == "Kritik Düşük") 2.dp else 0.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = bloodType,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = color
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = status,
                fontSize = 12.sp,
                color = color
            )
        }
    }
}

/**
 * A component that displays the working hours status
 * @param isOpen Whether the facility is currently open
 * @param hours The working hours as a string
 */
@Composable
fun WorkingHoursStatus(
    isOpen: Boolean,
    hours: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusIndicator(isActive = isOpen)
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isOpen) "Şu anda açık" else "Şu anda kapalı",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isOpen) Color(0xFF43A047) else Color(0xFFE53935)
                )
            }
            
            Text(
                text = "Çalışma saatleri: $hours",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}