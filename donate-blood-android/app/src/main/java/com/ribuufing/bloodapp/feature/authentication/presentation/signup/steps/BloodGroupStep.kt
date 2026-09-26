package com.ribuufing.bloodapp.feature.authentication.presentation.signup.steps

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ribuufing.bloodapp.utils.components.BloodButton
import com.ribuufing.bloodapp.utils.components.BloodGroupPicker

@Composable
fun BloodGroupStep(
    selectedBloodGroup: String?,
    onBloodGroupSelected: (String) -> Unit,
    bloodGroupError: String?,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Kan Grubu",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            BloodGroupPicker(
                selectedBloodGroup = selectedBloodGroup,
                onBloodGroupSelected = onBloodGroupSelected
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BloodButton(
                text = "Geri",
                onClick = onPrevious,
                modifier = Modifier.weight(1f)
            )

            BloodButton(
                text = "Devam et",
                onClick = onNext,
                modifier = Modifier.weight(1f)
            )
        }
    }
} 