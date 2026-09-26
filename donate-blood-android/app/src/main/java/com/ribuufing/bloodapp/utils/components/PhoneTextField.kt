package com.ribuufing.bloodapp.utils.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Country(
    val name: String,
    val code: String,
    val flag: String,
    val callingCode: String
)

val countries = listOf(
    Country("Turkey", "TR", "🇹🇷", "+90"),
    Country("United States", "US", "🇺🇸", "+1"),
    Country("United Kingdom", "GB", "🇬🇧", "+44"),
    Country("Germany", "DE", "🇩🇪", "+49"),
    Country("France", "FR", "🇫🇷", "+33"),
    Country("Italy", "IT", "🇮🇹", "+39"),
    Country("Spain", "ES", "🇪🇸", "+34"),
    Country("Netherlands", "NL", "🇳🇱", "+31"),
    Country("Belgium", "BE", "🇧🇪", "+32"),
    Country("Switzerland", "CH", "🇨🇭", "+41")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneTextField(
    phone: String,
    onPhoneChange: (String) -> Unit,
    selectedCountry: Country,
    onCountryChange: (Country) -> Unit,
    errorMessage: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var showCountryPicker by remember { mutableStateOf(false) }
    val backgroundColor = Color(0xFF2A2A2A)
    val borderColor = if (isError) {
        MaterialTheme.colorScheme.error
    } else {
        Color(0xFF4EABC8).copy(alpha = 0.5f)
    }

    Column {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .border(
                    width = 1.dp,
                    color = borderColor.copy(alpha = if (enabled) 1f else 0.3f),
                    shape = RoundedCornerShape(12.dp)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Country Selector
            Row(
                modifier = Modifier
                    .clickable(enabled = enabled) { showCountryPicker = true }
                    .padding(start = 16.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${selectedCountry.flag} ${selectedCountry.callingCode}",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = if (enabled) 1f else 0.3f)
                    )
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select country",
                    tint = Color.White.copy(alpha = if (enabled) 1f else 0.3f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(Color.White.copy(alpha = if (enabled) 0.2f else 0.1f))
                )
            }

            // Phone Number Input
            OutlinedTextField(
                value = phone,
                onValueChange = { newValue ->
                    if (newValue.length <= 10) {
                        onPhoneChange(newValue.filter { it.isDigit() })
                    }
                },
                enabled = enabled,
                placeholder = {
                    Text(
                        text = "5XX XXX XX XX",
                        color = Color.White.copy(alpha = if (enabled) 0.5f else 0.3f)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    errorBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    disabledTextColor = Color.White.copy(alpha = 0.3f)
                ),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                    .background(Color.Transparent)
            )
        }

        // Error Message
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

    // Country Picker Dialog
    if (showCountryPicker) {
        AlertDialog(
            onDismissRequest = { showCountryPicker = false },
            title = {
                Text(
                    "Ülke Seçin",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            },
            text = {
                Column {
                    countries.forEach { country ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCountryChange(country)
                                    showCountryPicker = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = country.flag,
                                fontSize = 24.sp
                            )
                            Column {
                                Text(
                                    text = country.name,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = country.callingCode,
                                    color = Color.White.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            },
            containerColor = Color(0xFF2A2A2A),
            confirmButton = {}
        )
    }
} 