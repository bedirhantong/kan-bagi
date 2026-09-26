package com.ribuufing.bloodapp.feature.authentication.presentation.signup.steps

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ribuufing.bloodapp.utils.components.BloodButton
import com.ribuufing.bloodapp.feature.authentication.presentation.SignupData
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SummaryStep(
    signupData: SignupData,
    onPrevious: () -> Unit,
    onSignup: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Bilgilerinizi Onaylayın",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2A2A2A)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryItem("Ad Soyad", signupData.fullName)
                SummaryItem("Telefon", "${signupData.selectedCountry.callingCode} ${signupData.phone}")
                SummaryItem("Kan Grubu", signupData.bloodGroup ?: "-")
                SummaryItem("Cinsiyet", signupData.gender?.title ?: "-")
                SummaryItem(
                    "Doğum Tarihi",
                    signupData.birthDate?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "-"
                )
            }
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
                text = "Kayıt Ol",
                onClick = onSignup,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
    }
} 