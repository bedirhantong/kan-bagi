package com.ribuufing.bloodapp.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext

@Composable
fun observeInternetConnectivity(): State<Boolean> {
    val context = LocalContext.current
    return produceState(initialValue = NetworkConnectivity.isInternetAvailable(context)) {
        NetworkConnectivity.observeConnectivity(context).collect { isConnected ->
            value = isConnected
        }
    }
} 