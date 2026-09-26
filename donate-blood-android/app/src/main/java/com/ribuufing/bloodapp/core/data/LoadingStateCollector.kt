package com.ribuufing.bloodapp.core.data

import kotlinx.coroutines.flow.MutableStateFlow

interface LoadingStateCollector {
    val loadingStateFlow: MutableStateFlow<LoadingState>
}
data class LoadingState(var show: Visibility = Visibility.DEFAULT)

enum class Visibility{
    SHOW,HIDE,DEFAULT
}