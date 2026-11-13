package com.example.sungutlele.ui.pengaturan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sungutlele.data.model.PreferencesData
import com.example.sungutlele.data.repository.SungutLeleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PengaturanUiState {
    data object Loading : PengaturanUiState()
    data class Loaded(val preferences: PreferencesData) : PengaturanUiState()
    data class Saved(val message: String) : PengaturanUiState()
    data class Error(val message: String) : PengaturanUiState()
}

class PengaturanViewModel : ViewModel() {

    private val repository = SungutLeleRepository()

    private val _uiState = MutableStateFlow<PengaturanUiState>(PengaturanUiState.Loading)
    val uiState: StateFlow<PengaturanUiState> = _uiState.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _farmName = MutableStateFlow("")
    val farmName: StateFlow<String> = _farmName.asStateFlow()

    init {
        loadPreferences()
    }

    fun onUserNameChanged(name: String) {
        _userName.value = name
    }

    fun onFarmNameChanged(name: String) {
        _farmName.value = name
    }

    fun loadPreferences() {
        _uiState.value = PengaturanUiState.Loading
        viewModelScope.launch {
            repository.getPreferences()
                .onSuccess { prefs ->
                    _userName.value = prefs.user_name ?: ""
                    _farmName.value = prefs.farm_name ?: ""
                    _uiState.value = PengaturanUiState.Loaded(prefs)
                }
                .onFailure {
                    _uiState.value = PengaturanUiState.Error(
                        it.message ?: "Gagal memuat pengaturan.",
                    )
                }
        }
    }

    fun savePreferences() {
        _uiState.value = PengaturanUiState.Loading
        viewModelScope.launch {
            val prefs = PreferencesData(
                user_name = _userName.value.ifBlank { null },
                farm_name = _farmName.value.ifBlank { null },
            )
            repository.updatePreferences(prefs)
                .onSuccess {
                    _userName.value = it.user_name ?: ""
                    _farmName.value = it.farm_name ?: ""
                    _uiState.value = PengaturanUiState.Saved("Pengaturan berhasil disimpan.")
                }
                .onFailure {
                    _uiState.value = PengaturanUiState.Error(
                        it.message ?: "Gagal menyimpan pengaturan.",
                    )
                }
        }
    }

    fun clearAllData() {
        _uiState.value = PengaturanUiState.Loading
        viewModelScope.launch {
            repository.clearAllTransactions()
                .onSuccess {
                    _uiState.value = PengaturanUiState.Saved("Semua data transaksi berhasil dihapus.")
                }
                .onFailure {
                    _uiState.value = PengaturanUiState.Error(
                        it.message ?: "Gagal menghapus data transaksi.",
                    )
                }
        }
    }

    fun consumeMessage() {
        // Reset state back to Loaded after showing toast to avoid recurring toasts
        if (_uiState.value is PengaturanUiState.Saved || _uiState.value is PengaturanUiState.Error) {
            val prefs = PreferencesData(
                user_name = _userName.value.ifBlank { null },
                farm_name = _farmName.value.ifBlank { null },
            )
            _uiState.value = PengaturanUiState.Loaded(prefs)
        }
    }
}
