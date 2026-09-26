package com.ribuufing.bloodapp.utils.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ribuufing.bloodapp.R

enum class Gender(val title: String, val icon: Int) {
    MALE("Erkek", R.drawable.ic_male),
    FEMALE("Kadın", R.drawable.ic_female)
}

@Composable
fun GenderPicker(
    selectedGender: Gender?,
    onGenderSelected: (Gender) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Gender.values().forEach { gender ->
            val isSelected = gender == selectedGender
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) Color(0xFF4EABC8)
                        else Color(0xFF2A2A2A)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color(0xFF4EABC8)
                        else Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(enabled = enabled) { onGenderSelected(gender) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = gender.icon),
                        contentDescription = gender.title,
                        tint = if (isSelected) Color.White
                        else Color.White.copy(alpha = if (enabled) 0.7f else 0.3f)
                    )
                    Text(
                        text = gender.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) Color.White
                        else Color.White.copy(alpha = if (enabled) 0.7f else 0.3f)
                    )
                }
            }
        }
    }
} 