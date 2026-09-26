package com.ribuufing.bloodapp.utils.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePicker(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = { },
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(localDate)
                    }
                }
            ) {
                Text("Tamam", color = Color(0xFF4EABC8))
            }
        },
        dismissButton = {
            TextButton(onClick = { }) {
                Text("İptal", color = Color.White)
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = false,
            title = {
                Text(
                    text = "Doğum Tarihinizi Seçin",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            },
            colors = DatePickerDefaults.colors(
                containerColor = Color(0xFF2A2A2A),
                titleContentColor = Color.White,
                headlineContentColor = Color.White,
                weekdayContentColor = Color.White,
                subheadContentColor = Color.White,
                yearContentColor = Color.White,
                currentYearContentColor = Color(0xFF4EABC8),
                selectedYearContainerColor = Color(0xFF4EABC8),
                selectedYearContentColor = Color.White,
                dayContentColor = Color.White,
                selectedDayContainerColor = Color(0xFF4EABC8),
                selectedDayContentColor = Color.White,
                todayContentColor = Color(0xFF4EABC8),
                todayDateBorderColor = Color(0xFF4EABC8)
            ),
            modifier = modifier
        )
    }
} 