package com.ribuufing.bloodapp.core.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ribuufing.bloodapp.R

@Composable
fun BloodAppDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit,
    title: String,
    message: String,
    icon: ImageVector? = null,
    showLogo: Boolean = true,
    confirmText: String = "Onayla",
    dismissText: String = "İptal",
    confirmButtonColor: Color = Color(0xFFE53935),
    dismissButtonColor: Color = Color(0xFF757575),
    properties: DialogProperties = DialogProperties(),
    onAlternativeAction: (() -> Unit)? = null,
    alternativeActionText: String = "Daha sonra hatırlat"
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (showLogo) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(150.dp)
                            .padding(bottom = 5.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(bottom = 16.dp),
                            tint = confirmButtonColor
                        )
                    }
                }

                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = dismissButtonColor
                        )
                    ) {
                        Text(dismissText)
                    }

                    Button(
                        onClick = onConfirmClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = confirmButtonColor
                        )
                    ) {
                        Text(confirmText)
                    }
                }

                if (onAlternativeAction != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onAlternativeAction,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(alternativeActionText, color = Color.Gray)
                    }
                }
            }
        }
    }
}