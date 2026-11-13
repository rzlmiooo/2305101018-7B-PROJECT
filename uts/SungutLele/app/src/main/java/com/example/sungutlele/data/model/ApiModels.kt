package com.example.sungutlele.data.model

/**
 * Generic API response wrapper.
 * All backend responses use: {"success": boolean, "data": T, "message": string|null}
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?,
)

/** Request body for POST /transactions/extract */
data class ExtractRequest(
    val text: String,
)

/** Transaction data from /transactions/extract and sent to /transactions/confirm */
data class TransactionData(
    val description: String,
    val transaction_type: String,
    val category: String,
    val quantity: Int,
    val unit: String,
    val amount: Long,
    val date: String,
)

/** Simple report from GET /reports/simple */
data class SimpleReportData(
    val period: String,
    val total_input: Long,
    val total_output: Long,
    val balance: Long,
)

/** Detail report from GET /reports/detail */
data class DetailReportData(
    val period: String,
    val total_input: Long,
    val total_output: Long,
    val balance: Long,
    val items: List<TransactionItem>,
)

/** A single transaction entry in the detail report */
data class TransactionItem(
    val date: String,
    val description: String,
    val input: Long,
    val output: Long,
)

/** User preferences from GET/PUT /preferences/ */
data class PreferencesData(
    val user_name: String?,
    val farm_name: String?,
)
