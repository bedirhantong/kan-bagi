package com.ribuufing.bloodapp.feature.authentication.presentation.signup.steps

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ribuufing.bloodapp.utils.components.BloodButton
import com.ribuufing.bloodapp.utils.components.DatePicker
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BirthDateStep(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    birthDateError: String?,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Doğum Tarihi",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )

        var showDatePicker by remember { mutableStateOf(false) }

        BloodButton(
            text = selectedDate?.let { date ->
                "Doğum Tarihi: ${date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}"
            } ?: "Doğum Tarihi Seç",
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth()
        )

        if (showDatePicker) {
            DatePicker(
                selectedDate = selectedDate,
                onDateSelected = { 
                    onDateSelected(it)
                    showDatePicker = false
                }
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