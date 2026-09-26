package com.ribuufing.bloodapp.feature.authentication.presentation.signup.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.ribuufing.bloodapp.utils.components.BloodButton
import com.ribuufing.bloodapp.utils.components.BloodTextField
import com.ribuufing.bloodapp.utils.components.Gender
import com.ribuufing.bloodapp.utils.components.GenderPicker

@Composable
fun PersonalInfoStep(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    fullNameError: String?,
    selectedGender: Gender?,
    onGenderSelected: (Gender) -> Unit,
    genderError: String?,
    onNext: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Kişisel Bilgiler",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )

        BloodTextField(
            value = fullName,
            onValueChange = onFullNameChange,
            label = "Ad Soyad",
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            errorMessage = fullNameError,
            isError = fullNameError != null
        )

        GenderPicker(
            selectedGender = selectedGender,
            onGenderSelected = onGenderSelected
        )

        BloodButton(
            text = "Devam et",
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        )
    }
} 