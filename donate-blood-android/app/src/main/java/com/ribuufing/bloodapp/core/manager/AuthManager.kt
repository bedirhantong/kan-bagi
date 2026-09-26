package com.ribuufing.bloodapp.core.manager

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isUserLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        private set(value) = prefs.edit { putBoolean(KEY_IS_LOGGED_IN, value) }

    var userPhone: String?
        get() = prefs.getString(KEY_USER_PHONE, null)
        private set(value) = prefs.edit { putString(KEY_USER_PHONE, value) }
        
    var idToken: String?
        get() = prefs.getString(KEY_ID_TOKEN, null)
        private set(value) = prefs.edit { putString(KEY_ID_TOKEN, value) }
    
    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)
        private set(value) = prefs.edit { putString(KEY_ACCESS_TOKEN, value) }
    
    var accessTokenExpiry: Long
        get() = prefs.getLong(KEY_ACCESS_TOKEN_EXPIRY, 0)
        private set(value) = prefs.edit { putLong(KEY_ACCESS_TOKEN_EXPIRY, value) }
    
    var userId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        private set(value) = prefs.edit { putString(KEY_USER_ID, value) }

    var userType: String
        get() = prefs.getString(KEY_USER_TYPE, "REGULAR_USER") ?: "REGULAR_USER"
        private set(value) = prefs.edit { putString(KEY_USER_TYPE, value) }


    fun saveMsalAccessToken(token: String) {
        accessToken = token
    }

    fun saveUserId(id: String?) {
        userId = id
    }

    fun saveUserType(type: String) {
        userType = type
    }

    fun isHospitalStaff(): Boolean {
        return userType == "HOSPITAL_STAFF"
    }

    var ipAddress: String?
        get() = prefs.getString(KEY_IP_ADDRESS, null)
        set(value) = prefs.edit { putString(KEY_IP_ADDRESS, value) }

    var formId: String?
        get() = prefs.getString(FORM_ID, null)
        set(value) = prefs.edit { putString(FORM_ID, value) }

    // Microsoft B2C ile kayıt için
    fun saveLoginState(
        username: String,
        idToken: String?,
        accessToken: String,
        expiresIn: Long?,
        userId: String?
    ) {
        isUserLoggedIn = true
        userPhone = username
        this.idToken = idToken
        this.accessToken = accessToken
        
        // Tokenın geçerlilik süresini belirle (şu anki zaman + sona erme süresi)
        this.accessTokenExpiry = if (expiresIn != null) {
            System.currentTimeMillis() + (expiresIn * 1000)
        } else {
            0
        }
        
        this.userId = userId
    }

    fun clearLoginState() {
        isUserLoggedIn = false
        userPhone = null
        idToken = null
        accessToken = null
        accessTokenExpiry = 0
        userId = null
        userType = "REGULAR_USER"
    }

    companion object {
        private const val PREFS_NAME = "blood_app_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_ID_TOKEN = "id_token"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_ACCESS_TOKEN_EXPIRY = "access_token_expiry"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_TYPE = "user_type"
        private const val KEY_IP_ADDRESS = "ip_address"
        private const val FORM_ID = "form_id"
    }
} 