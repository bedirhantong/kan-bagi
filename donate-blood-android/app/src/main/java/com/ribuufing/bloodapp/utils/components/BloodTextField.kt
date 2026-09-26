package com.ribuufing.bloodapp.utils.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BloodTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    errorMessage: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val backgroundColor = Color(0xFF2A2A2A)
    val borderColor = if (isError) {
        MaterialTheme.colorScheme.error
    } else {
        Color(0xFF4EABC8).copy(alpha = 0.5f)
    }
    
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue -> 
                if (keyboardOptions.keyboardType == KeyboardOptions.Default.keyboardType) {
                    onValueChange(newValue.lowercase())
                } else {
                    onValueChange(newValue)
                }
            },
            label = { 
                Text(
                    text = label,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = if (isError) MaterialTheme.colorScheme.error 
                        else Color.White.copy(alpha = if (enabled) 0.7f else 0.3f)
                    )
                )
            },
            leadingIcon = leadingIcon?.let { 
                { 
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = if (isError) MaterialTheme.colorScheme.error 
                        else Color(0xFF4EABC8).copy(alpha = if (enabled) 1f else 0.3f)
                    )
                }
            },
            isError = isError,
            enabled = enabled,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            shape = RoundedCornerShape(12.dp),
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .border(
                    width = 1.dp,
                    color = borderColor.copy(alpha = if (enabled) 1f else 0.3f),
                    shape = RoundedCornerShape(12.dp)
                ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4EABC8),
                unfocusedBorderColor = Color.Transparent,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                disabledTextColor = Color.White.copy(alpha = 0.3f),
                cursorColor = Color(0xFF4EABC8),
                errorCursorColor = MaterialTheme.colorScheme.error,
            ),
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = Color.White
            )
        )
        
        AnimatedVisibility(
            visible = isError && !errorMessage.isNullOrEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
} 