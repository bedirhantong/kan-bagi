package com.ribuufing.bloodapp.utils.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val bloodGroups = listOf(
    "A+", "A-", "B+", "B-", "AB+", "AB-", "0+", "0-"
)

@Composable
fun BloodGroupPicker(
    selectedBloodGroup: String?,
    onBloodGroupSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        items(bloodGroups) { bloodGroup ->
            val isSelected = bloodGroup == selectedBloodGroup
            Box(
                modifier = Modifier
                    .aspectRatio(1.5f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) Color(0xFF4EABC8)
                        else Color(0xFF2A2A2A)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color(0xFF4EABC8)
                        else Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(enabled = enabled) { onBloodGroupSelected(bloodGroup) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = bloodGroup,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) Color.White
                    else Color.White.copy(alpha = if (enabled) 0.7f else 0.3f)
                )
            }
        }
    }
} 