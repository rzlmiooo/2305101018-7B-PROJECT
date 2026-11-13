package com.example.sungutlele.ui.catat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sungutlele.data.model.TransactionData
import com.example.sungutlele.data.repository.SungutLeleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * A single message in the conversation.
 */
data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
)

/**
 * UI state for the Catat (transaction recording) screen.
 *
 * Follows the state flow from AGENTS.md:
 * User Input → Processing → Draft/Confirmation → Confirmed/Cancelled
 */
sealed class CatatUiState {
    data object Idle : CatatUiState()
    data object Processing : CatatUiState()
    data class WaitingConfirmation(val transaction: TransactionData) : CatatUiState()
    data class Success(val message: String) : CatatUiState()
    data class Error(val message: String) : CatatUiState()
}

class CatatViewModel : ViewModel() {

    private val repository = SungutLeleRepository()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _uiState = MutableStateFlow<CatatUiState>(CatatUiState.Idle)
    val uiState: StateFlow<CatatUiState> = _uiState.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    fun onInputChanged(text: String) {
        _inputText.value = text
    }

    fun onSendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return

        _messages.update { it + ChatMessage(text = text, isFromUser = true) }
        _inputText.value = ""
        _uiState.value = CatatUiState.Processing

        viewModelScope.launch {
            repository.extractTransaction(text)
                .onSuccess { transaction ->
                    _messages.update {
                        it + ChatMessage(text = buildDraftMessage(transaction), isFromUser = false)
                    }
                    _uiState.value = CatatUiState.WaitingConfirmation(transaction)
                }
                .onFailure { error ->
                    _messages.update {
                        it + ChatMessage(
                            text = error.message ?: "Terjadi kesalahan.",
                            isFromUser = false,
                        )
                    }
                    _uiState.value = CatatUiState.Idle
                }
        }
    }

    fun onConfirmTransaction() {
        val state = _uiState.value
        if (state !is CatatUiState.WaitingConfirmation) return

        _messages.update { it + ChatMessage(text = "Oke, simpan.", isFromUser = true) }
        _uiState.value = CatatUiState.Processing

        viewModelScope.launch {
            repository.confirmTransaction(state.transaction)
                .onSuccess {
                    _messages.update {
                        it + ChatMessage(text = "Transaksi berhasil disimpan! ✓", isFromUser = false)
                    }
                    _uiState.value = CatatUiState.Idle
                }
                .onFailure { error ->
                    _messages.update {
                        it + ChatMessage(
                            text = error.message ?: "Gagal menyimpan transaksi.",
                            isFromUser = false,
                        )
                    }
                    _uiState.value = CatatUiState.Idle
                }
        }
    }

    fun onCancelTransaction() {
        _messages.update { it + ChatMessage(text = "Batal.", isFromUser = true) }
        _messages.update {
            it + ChatMessage(text = "Transaksi dibatalkan.", isFromUser = false)
        }
        _uiState.value = CatatUiState.Idle
    }

    fun clearChat() {
        _messages.value = emptyList()
        _inputText.value = ""
        _uiState.value = CatatUiState.Idle
    }

    fun onVoiceResult(text: String) {
        _inputText.value = text
    }

    private fun buildDraftMessage(t: TransactionData): String {
        val type = if (t.transaction_type == "income") "Pemasukan" else "Pengeluaran"
        return "$type: ${t.description}\n" +
            "Jumlah: ${t.quantity} ${t.unit}\n" +
            "Harga: ${formatRupiah(t.amount)}\n" +
            "Kategori: ${t.category}\n" +
            "Tanggal: ${t.date}\n\n" +
            "Simpan transaksi ini?"
    }

    private fun formatRupiah(amount: Long): String {
        val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
        return "Rp${formatter.format(amount)}"
    }
}
