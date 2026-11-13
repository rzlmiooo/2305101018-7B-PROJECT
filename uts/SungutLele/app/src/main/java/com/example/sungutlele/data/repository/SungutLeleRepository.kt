package com.example.sungutlele.data.repository

import com.example.sungutlele.data.api.RetrofitClient
import com.example.sungutlele.data.model.DetailReportData
import com.example.sungutlele.data.model.ExtractRequest
import com.example.sungutlele.data.model.PreferencesData
import com.example.sungutlele.data.model.SimpleReportData
import com.example.sungutlele.data.model.TransactionData
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Single repository wrapping all backend API calls.
 * Handles error mapping to user-friendly Indonesian messages.
 */
class SungutLeleRepository {

    private val api = RetrofitClient.apiService

    suspend fun healthCheck() {
        try {
            api.healthCheck()
        } catch (e: Exception) {
            // Ignore error since this is just a warm-up ping
        }
    }

    suspend fun extractTransaction(text: String): Result<TransactionData> = apiCall {
        val response = api.extractTransaction(ExtractRequest(text))
        if (response.success && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message ?: "Gagal memproses transaksi."))
        }
    }

    suspend fun confirmTransaction(transaction: TransactionData): Result<Unit> = apiCall {
        val response = api.confirmTransaction(transaction)
        if (response.success) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.message ?: "Gagal menyimpan transaksi."))
        }
    }

    suspend fun clearAllTransactions(): Result<Unit> = apiCall {
        val response = api.clearAllTransactions()
        if (response.success) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.message ?: "Gagal menghapus data transaksi."))
        }
    }

    suspend fun getSimpleReport(
        period: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Result<SimpleReportData> = apiCall {
        val response = api.getSimpleReport(period, startDate, endDate)
        if (response.success && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message ?: "Gagal memuat laporan."))
        }
    }

    suspend fun getDetailReport(
        period: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Result<DetailReportData> = apiCall {
        val response = api.getDetailReport(period, startDate, endDate)
        if (response.success && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message ?: "Gagal memuat laporan detail."))
        }
    }

    suspend fun getPreferences(): Result<PreferencesData> = apiCall {
        val response = api.getPreferences()
        if (response.success && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message ?: "Gagal memuat pengaturan."))
        }
    }

    suspend fun updatePreferences(preferences: PreferencesData): Result<PreferencesData> = apiCall {
        val response = api.updatePreferences(preferences)
        if (response.success && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message ?: "Gagal menyimpan pengaturan."))
        }
    }

    /**
     * Wraps API calls with user-friendly error handling.
     */
    private suspend fun <T> apiCall(block: suspend () -> Result<T>): Result<T> {
        return try {
            block()
        } catch (e: ConnectException) {
            Result.failure(Exception("Tidak dapat terhubung ke server.\nPeriksa koneksi internet lalu coba lagi."))
        } catch (e: UnknownHostException) {
            Result.failure(Exception("Tidak dapat terhubung ke server.\nPeriksa koneksi internet lalu coba lagi."))
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("Koneksi ke server terlalu lama.\nSilakan coba lagi."))
        } catch (e: Exception) {
            Result.failure(Exception("Terjadi kesalahan.\nSilakan coba lagi."))
        }
    }
}
