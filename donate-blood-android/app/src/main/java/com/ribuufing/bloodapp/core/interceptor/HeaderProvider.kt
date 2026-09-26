package com.ribuufing.bloodapp.core.interceptor

import com.ribuufing.bloodapp.core.manager.AuthManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeaderProvider @Inject constructor(
    private val authManager: AuthManager
) {
    private var language: String = "tr"

    fun setLanguage(lang: String) {
        language = lang
    }

    fun getHeaders(): Map<String, String> {
        val headers = mutableMapOf(
            "Content-Type" to "application/json",
            "accept" to "*/*",
            "Language" to language
        )
        
        // AuthManager'dan access token'ı al ve header'a ekle
        authManager.accessToken?.let { token ->
            headers["Authorization"] = "Bearer $token"
        }
        
        return headers
    }
} 