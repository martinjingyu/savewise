package com.cs407.savewise.viewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class MeUiState(
    val userName: String = "User Name",
    val region: String = "United States",
    val autoRecording: Boolean = false,
    val language: String = "English",
    val recordingStorageDays: Int = 7 // 0,1,3,7,30
)

class MeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MeUiState())
    val uiState: StateFlow<MeUiState> = _uiState

    fun setUserName(name: String) = _uiState.update { it.copy(userName = name) }
    fun setRegion(region: String) = _uiState.update { it.copy(region = region) }
    fun setAutoRecording(enabled: Boolean) = _uiState.update { it.copy(autoRecording = enabled) }
    fun setLanguage(lang: String) = _uiState.update { it.copy(language = lang) }
    fun setRecordingStorageDays(days: Int) = _uiState.update { it.copy(recordingStorageDays = days) }
}
