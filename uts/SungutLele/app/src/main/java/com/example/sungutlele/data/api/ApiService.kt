package com.example.sungutlele.data.api

import com.example.sungutlele.data.model.ApiResponse
import com.example.sungutlele.data.model.DetailReportData
import com.example.sungutlele.data.model.ExtractRequest
import com.example.sungutlele.data.model.PreferencesData
import com.example.sungutlele.data.model.SimpleReportData
import com.example.sungutlele.data.model.TransactionData
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.Response

interface ApiService {

    @GET("/health")
    suspend fun healthCheck(): Response<Unit>

    // UC1 — Catat Transaksi

    @POST("transactions/extract")
    suspend fun extractTransaction(@Body request: ExtractRequest): ApiResponse<TransactionData>

    @POST("transactions/confirm")
    suspend fun confirmTransaction(@Body transaction: TransactionData): ApiResponse<Any>

    @retrofit2.http.DELETE("transactions/clear-all")
    suspend fun clearAllTransactions(): ApiResponse<Any>

    // UC2 — Laporan

    @GET("reports/simple")
    suspend fun getSimpleReport(
        @Query("period") period: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): ApiResponse<SimpleReportData>

    @GET("reports/detail")
    suspend fun getDetailReport(
        @Query("period") period: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): ApiResponse<DetailReportData>

    // UC3 — Pengaturan

    @GET("preferences/")
    suspend fun getPreferences(): ApiResponse<PreferencesData>

    @PUT("preferences/")
    suspend fun updatePreferences(@Body preferences: PreferencesData): ApiResponse<PreferencesData>
}
