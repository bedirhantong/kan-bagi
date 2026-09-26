package com.ribuufing.bloodapp.core.utils

import android.util.Log

object ErrorHandlingUtils {
    
    /**
     * Extracts the actual message from error response strings that might include HTTP status codes
     */
    fun getCleanErrorMessage(errorMessage: String?): String {
        if (errorMessage == null) return "Bir hata oluştu"
        
        Log.d("ErrorHandling", "Original error: $errorMessage")
        
        // Remove any HTTP status codes from the message
        val messageWithoutHttpCodes = errorMessage.replace(Regex("HTTP \\d+ \\w+"), "").trim()
        
        // Try to extract JSON error message if the error is in JSON format
        if (errorMessage.contains("resultMessage")) {
            try {
                val resultMessageRegex = "\"resultMessage\":\"([^\"]+)\"".toRegex()
                val matchResult = resultMessageRegex.find(errorMessage)
                if (matchResult != null) {
                    return matchResult.groupValues[1]
                }
            } catch (e: Exception) {
                Log.e("ErrorHandling", "Failed to parse JSON error message", e)
            }
        }
        
        return when {
            messageWithoutHttpCodes.isNotBlank() -> messageWithoutHttpCodes
            errorMessage.contains("406") -> "Bilgiler uygun değil. Lütfen tekrar deneyin."
            else -> errorMessage
        }
    }
}