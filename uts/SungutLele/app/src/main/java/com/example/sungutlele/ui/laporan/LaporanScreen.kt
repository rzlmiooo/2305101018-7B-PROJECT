package com.example.sungutlele.ui.laporan

import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import java.text.SimpleDateFormat
import java.util.Calendar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sungutlele.data.model.DetailReportData
import com.example.sungutlele.data.model.SimpleReportData
import com.example.sungutlele.data.model.TransactionItem
import com.example.sungutlele.ui.theme.ExpenseRed
import com.example.sungutlele.ui.theme.IncomeGreen
import java.text.NumberFormat
import java.util.Locale

private enum class ReportPeriod(val label: String) {
    DAILY("Harian"),
    WEEKLY("Mingguan"),
    MONTHLY("Bulanan"),
    YEARLY("Tahunan"),
    CUSTOM("Kustom"),
}

private enum class ReportMode(val label: String) {
    SIMPLE("Ringkasan"),
    DETAIL("Detail"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanScreen(
    viewModel: LaporanViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPeriod by remember { mutableStateOf(ReportPeriod.MONTHLY) }
    var selectedModeIndex by remember { mutableIntStateOf(0) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var customStartDate by remember { mutableStateOf<Long?>(null) }
    var customEndDate by remember { mutableStateOf<Long?>(null) }
    
    val modes = ReportMode.entries
    val context = LocalContext.current

    val periodLabel = remember(selectedPeriod, customStartDate, customEndDate) {
        getPeriodLabelString(selectedPeriod, customStartDate, customEndDate)
    }

    // Load data when mode, period, or custom dates change
    LaunchedEffect(selectedModeIndex, selectedPeriod, customStartDate, customEndDate) {
        val periodParam = selectedPeriod.name.lowercase()
        val startDateStr = if (selectedPeriod == ReportPeriod.CUSTOM) customStartDate?.let { 
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.forLanguageTag("id-ID"))
            val cal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply { timeInMillis = it }
            format.format(cal.time)
        } else null
        val endDateStr = if (selectedPeriod == ReportPeriod.CUSTOM) customEndDate?.let { 
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.forLanguageTag("id-ID"))
            val cal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply { timeInMillis = it }
            format.format(cal.time)
        } else null
        
        // Don't load if custom is selected but dates are not fully chosen
        if (selectedPeriod == ReportPeriod.CUSTOM && (customStartDate == null || customEndDate == null)) {
            return@LaunchedEffect
        }

        when (modes[selectedModeIndex]) {
            ReportMode.SIMPLE -> viewModel.loadSimpleReport(periodParam, startDateStr, endDateStr)
            ReportMode.DETAIL -> viewModel.loadDetailReport(periodParam, startDateStr, endDateStr)
        }
    }

    // Show toast on error
    LaunchedEffect(uiState) {
        if (uiState is LaporanUiState.Error) {
            Toast.makeText(context, (uiState as LaporanUiState.Error).message, Toast.LENGTH_LONG).show()
        }
    }

    if (showDatePicker) {
        val dateRangePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = customStartDate,
            initialSelectedEndDateMillis = customEndDate
        )
        DatePickerDialog(
            onDismissRequest = { 
                showDatePicker = false
                if (customStartDate == null || customEndDate == null) {
                    selectedPeriod = ReportPeriod.MONTHLY
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        customStartDate = dateRangePickerState.selectedStartDateMillis
                        customEndDate = dateRangePickerState.selectedEndDateMillis
                        showDatePicker = false
                    },
                    enabled = dateRangePickerState.selectedEndDateMillis != null
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        if (customStartDate == null || customEndDate == null) {
                            selectedPeriod = ReportPeriod.MONTHLY
                        }
                    }
                ) {
                    Text("BATAL", fontWeight = FontWeight.Bold)
                }
            }
        ) {
            val formatter = remember { SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("id-ID")) }
            val startText = dateRangePickerState.selectedStartDateMillis?.let {
                val cal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply { timeInMillis = it }
                formatter.format(cal.time)
            } ?: "Mulai"
            val endText = dateRangePickerState.selectedEndDateMillis?.let {
                val cal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply { timeInMillis = it }
                formatter.format(cal.time)
            } ?: "Selesai"

            DateRangePicker(
                state = dateRangePickerState,
                title = {
                    Text(
                        text = "Pilih Rentang Waktu",
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                headline = {
                    Text(
                        text = "$startText — $endText",
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                showModeToggle = false,
                modifier = Modifier.weight(1f)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = "Laporan",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mode toggle (Moved up)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            modes.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = selectedModeIndex == index,
                    onClick = { selectedModeIndex = index },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                ) {
                    Text(text = mode.label, style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Period selector (Moved down)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ReportPeriod.entries.forEach { period ->
                FilterChip(
                    selected = selectedPeriod == period,
                    onClick = {
                        selectedPeriod = period
                        if (period == ReportPeriod.CUSTOM) {
                            showDatePicker = true
                        }
                    },
                    label = {
                        Text(text = period.label, style = MaterialTheme.typography.labelLarge)
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Content
        when (val state = uiState) {
            is LaporanUiState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is LaporanUiState.SimpleLoaded -> SimpleReportContent(state.data, periodLabel)
            is LaporanUiState.DetailLoaded -> DetailReportContent(state.data, periodLabel)
            is LaporanUiState.Error -> {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SimpleReportContent(data: SimpleReportData, periodLabel: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = periodLabel,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(20.dp))
            SummaryRow(label = "Uang Masuk", value = formatRupiah(data.total_input), color = IncomeGreen)
            Spacer(modifier = Modifier.height(12.dp))
            SummaryRow(label = "Uang Keluar", value = formatRupiah(data.total_output), color = ExpenseRed)
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            SummaryRow(
                label = "Saldo",
                value = formatRupiah(data.balance),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun DetailReportContent(data: DetailReportData, periodLabel: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = periodLabel,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Table container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                // Table header
                Row(modifier = Modifier.width(500.dp)) {
                    Text("Tanggal", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
                    Text("Keterangan", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.width(160.dp))
                    Text("Masuk", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp), textAlign = TextAlign.End)
                    Text("Keluar", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp), textAlign = TextAlign.End)
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(modifier = Modifier.width(500.dp))

                if (data.items.isEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Belum ada data transaksi.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(500.dp),
                        textAlign = TextAlign.Center,
                    )
                } else {
                    data.items.forEach { item ->
                        TransactionRow(item)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(modifier = Modifier.width(500.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Totals
            SummaryRow(label = "Total Masuk", value = formatRupiah(data.total_input), color = IncomeGreen)
            Spacer(modifier = Modifier.height(8.dp))
            SummaryRow(label = "Total Keluar", value = formatRupiah(data.total_output), color = ExpenseRed)
            Spacer(modifier = Modifier.height(8.dp))
            SummaryRow(
                label = "Saldo",
                value = formatRupiah(data.balance),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun TransactionRow(item: TransactionItem) {
    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.width(500.dp)) {
        Text(
            text = item.date,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(100.dp),
        )
        Text(
            text = item.description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(160.dp),
        )
        Text(
            text = if (item.input > 0) formatRupiah(item.input) else "",
            style = MaterialTheme.typography.bodyMedium,
            color = IncomeGreen,
            modifier = Modifier.width(120.dp),
            textAlign = TextAlign.End,
        )
        Text(
            text = if (item.output > 0) formatRupiah(item.output) else "",
            style = MaterialTheme.typography.bodyMedium,
            color = ExpenseRed,
            modifier = Modifier.width(120.dp),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

private fun formatRupiah(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    return "Rp${formatter.format(amount)}"
}

private fun getPeriodLabelString(period: ReportPeriod, startDate: Long?, endDate: Long?): String {
    val locale = Locale.forLanguageTag("id-ID")
    val cal = Calendar.getInstance(locale)
    
    return when (period) {
        ReportPeriod.DAILY -> {
            val format = SimpleDateFormat("EEEE, d MMM yyyy", locale)
            format.format(cal.time)
        }
        ReportPeriod.WEEKLY -> {
            val format = SimpleDateFormat("d MMM yyyy", locale)
            
            // Set to beginning of week (Monday)
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            val startOfWeek = format.format(cal.time)
            
            // Set to end of week (Sunday)
            cal.add(Calendar.DAY_OF_WEEK, 6)
            val endOfWeek = format.format(cal.time)
            
            "$startOfWeek - $endOfWeek"
        }
        ReportPeriod.MONTHLY -> {
            val format = SimpleDateFormat("MMMM yyyy", locale)
            format.format(cal.time)
        }
        ReportPeriod.YEARLY -> {
            val format = SimpleDateFormat("yyyy", locale)
            format.format(cal.time)
        }
        ReportPeriod.CUSTOM -> {
            if (startDate != null && endDate != null) {
                val format = SimpleDateFormat("d MMM yyyy", locale)
                val startCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply { timeInMillis = startDate }
                val endCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply { timeInMillis = endDate }
                "${format.format(startCal.time)} - ${format.format(endCal.time)}"
            } else {
                "Pilih Tanggal"
            }
        }
    }
}
