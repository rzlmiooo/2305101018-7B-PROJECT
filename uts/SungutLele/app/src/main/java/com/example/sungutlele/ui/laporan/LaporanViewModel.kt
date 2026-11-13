package com.example.sungutlele.ui.laporan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sungutlele.data.model.DetailReportData
import com.example.sungutlele.data.model.SimpleReportData
import com.example.sungutlele.data.repository.SungutLeleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LaporanUiState {
    data object Loading : LaporanUiState()
    data class SimpleLoaded(val data: SimpleReportData) : LaporanUiState()
    data class DetailLoaded(val data: DetailReportData) : LaporanUiState()
    data class Error(val message: String) : LaporanUiState()
}

class LaporanViewModel : ViewModel() {

    private val repository = SungutLeleRepository()

    private val _uiState = MutableStateFlow<LaporanUiState>(LaporanUiState.Loading)
    val uiState: StateFlow<LaporanUiState> = _uiState.asStateFlow()

    init {
        loadSimpleReport("monthly")
    }

    fun loadSimpleReport(period: String = "monthly", startDate: String? = null, endDate: String? = null) {
        _uiState.value = LaporanUiState.Loading
        viewModelScope.launch {
            repository.getSimpleReport(period, startDate, endDate)
                .onSuccess { _uiState.value = LaporanUiState.SimpleLoaded(it) }
                .onFailure { _uiState.value = LaporanUiState.Error(it.message ?: "Gagal memuat laporan.") }
        }
    }

    fun loadDetailReport(period: String = "monthly", startDate: String? = null, endDate: String? = null) {
        _uiState.value = LaporanUiState.Loading
        viewModelScope.launch {
            repository.getDetailReport(period, startDate, endDate)
                .onSuccess { _uiState.value = LaporanUiState.DetailLoaded(it) }
                .onFailure { _uiState.value = LaporanUiState.Error(it.message ?: "Gagal memuat laporan.") }
        }
    }
}
