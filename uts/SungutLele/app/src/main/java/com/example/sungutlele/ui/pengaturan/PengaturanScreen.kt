package com.example.sungutlele.ui.pengaturan

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PengaturanScreen(
    viewModel: PengaturanViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val farmName by viewModel.farmName.collectAsState()
    val context = LocalContext.current

    // Show toast for save success or error
    LaunchedEffect(uiState) {
        when (uiState) {
            is PengaturanUiState.Saved -> {
                Toast.makeText(context, (uiState as PengaturanUiState.Saved).message, Toast.LENGTH_SHORT).show()
                viewModel.consumeMessage()
            }
            is PengaturanUiState.Error -> {
                Toast.makeText(context, (uiState as PengaturanUiState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.consumeMessage()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = "Pengaturan",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(24.dp))

        when (uiState) {
            is PengaturanUiState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                // Profile section — editable
                SectionTitle("Profil")

                OutlinedTextField(
                    value = userName,
                    onValueChange = viewModel::onUserNameChanged,
                    label = { Text("Nama Pengguna") },
                    placeholder = { Text("Masukkan nama Anda") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = farmName,
                    onValueChange = viewModel::onFarmNameChanged,
                    label = { Text("Nama Usaha / Kolam") },
                    placeholder = { Text("Masukkan nama usaha") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = viewModel::savePreferences,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    Text("Simpan", style = MaterialTheme.typography.labelLarge)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // App info — read only
                SectionTitle("Aplikasi")
                SettingsRow(title = "Versi", value = "1.0")

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Hapus Data
                SectionTitle("Hapus Data")

                var showDeleteDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFFD32F2F), // Strong red
                        contentColor = androidx.compose.ui.graphics.Color.White
                    )
                ) {
                    Text("Hapus Semua Data Transaksi", style = MaterialTheme.typography.labelLarge)
                }

                if (showDeleteDialog) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Konfirmasi Hapus Data") },
                        text = { Text("Apakah Anda yakin ingin menghapus SEMUA data transaksi? Data yang sudah dihapus tidak dapat dikembalikan.") },
                        confirmButton = {
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    showDeleteDialog = false
                                    viewModel.clearAllData()
                                }
                            ) {
                                Text("Hapus", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(
                                onClick = { showDeleteDialog = false }
                            ) {
                                Text("Batal")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@Composable
private fun SettingsRow(title: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
