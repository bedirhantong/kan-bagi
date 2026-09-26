package com.ribuufing.bloodapp.utils.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpBottomSheet(
    phoneNumber: String,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onVerify: (String) -> Unit
) {
    var otp by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf<String?>(null) }

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Enter Verification Code",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )

                Text(
                    text = "We've sent a verification code to\n$phoneNumber",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                BloodTextField(
                    value = otp,
                    onValueChange = { 
                        if (it.length <= 4) {
                            otp = it.filter { char -> char.isDigit() }
                            otpError = null
                        }
                    },
                    label = "Enter OTP",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword
                    ),
                    errorMessage = otpError,
                    isError = otpError != null,
                    modifier = Modifier.fillMaxWidth()
                )

                BloodButton(
                    text = "Verify",
                    onClick = {
                        when {
                            otp.isEmpty() -> {
                                otpError = "OTP is required"
                            }
                            otp.length < 4 -> {
                                otpError = "Please enter valid OTP"
                            }
                            else -> {
                                onVerify(otp)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
} 