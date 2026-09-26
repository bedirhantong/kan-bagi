package com.ribuufing.bloodapp.utils.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SignupTimeline(
    currentStep: Int,
    steps: List<String>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            if (index > 0) {
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            TimelineStep(
                step = step,
                isCompleted = index < currentStep,
                isActive = index == currentStep,
                isLastStep = index == steps.size - 1
            )
            
            if (!isLastStep(index, steps)) {
                TimelineLine(
                    isCompleted = index < currentStep,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TimelineStep(
    step: String,
    isCompleted: Boolean,
    isActive: Boolean,
    isLastStep: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        // Circle
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> Color(0xFF4EABC8)
                        isActive -> Color(0xFF4EABC8).copy(alpha = 0.7f)
                        else -> Color(0xFF2A2A2A)
                    }
                )
                .border(
                    width = 2.dp,
                    color = when {
                        isCompleted -> Color(0xFF4EABC8)
                        isActive -> Color(0xFF4EABC8)
                        else -> Color.White.copy(alpha = 0.3f)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = (step.first()).toString(),
                color = when {
                    isCompleted || isActive -> Color.White
                    else -> Color.White.copy(alpha = 0.5f)
                },
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Step Text
        Text(
            text = step,
            style = MaterialTheme.typography.bodySmall,
            color = when {
                isCompleted || isActive -> Color.White
                else -> Color.White.copy(alpha = 0.5f)
            }
        )
    }
}

@Composable
private fun TimelineLine(
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(2.dp)
            .padding(horizontal = 4.dp)
            .background(
                if (isCompleted) Color(0xFF4EABC8)
                else Color.White.copy(alpha = 0.3f)
            )
    )
}

private fun isLastStep(index: Int, steps: List<String>): Boolean {
    return index == steps.size - 1
} 