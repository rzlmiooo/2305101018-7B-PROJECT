package com.example.sungutlele

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.LaunchedEffect
import com.example.sungutlele.data.repository.SungutLeleRepository
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.sungutlele.ui.catat.CatatScreen
import com.example.sungutlele.ui.laporan.LaporanScreen
import com.example.sungutlele.ui.pengaturan.PengaturanScreen

/**
 * Tab identifiers for the bottom navigation.
 * Order matches AGENTS.md layout: Pengaturan | Catat | Laporan
 */
private enum class Tab {
    PENGATURAN,
    CATAT,
    LAPORAN,
}

@Composable
fun SungutLeleApp() {
    // Default to Catat (UC1) — the primary action
    var currentTab by rememberSaveable { mutableStateOf(Tab.CATAT) }
    
    // Warm up the serverless backend on app start
    LaunchedEffect(Unit) {
        val repo = SungutLeleRepository()
        // Calling a lightweight endpoint to wake up the server instance
        // We don't need to handle the result, just let it fire and forget
        repo.healthCheck()
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentTab) {
                Tab.PENGATURAN -> PengaturanScreen()
                Tab.CATAT -> CatatScreen()
                Tab.LAPORAN -> LaporanScreen()
            }
        }
    }
}

/**
 * Custom bottom navigation with a visually prominent center Catat button.
 *
 * Layout matches AGENTS.md:
 * ┌────────────┬──────────┬─────────┐
 * │ Pengaturan │   Catat  │ Laporan │
 * │    UC3     │   UC1    │   UC2   │
 * └────────────┴──────────┴─────────┘
 */
@Composable
private fun BottomNavBar(
    currentTab: Tab,
    onTabSelected: (Tab) -> Unit,
) {
    Surface(
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Pengaturan (UC3) — left
            NavItem(
                icon = Icons.Default.Settings,
                label = "Pengaturan",
                selected = currentTab == Tab.PENGATURAN,
                onClick = { onTabSelected(Tab.PENGATURAN) },
                modifier = Modifier.weight(1f),
            )

            // Catat (UC1) — center, visually prominent
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onTabSelected(Tab.CATAT) }
                        .padding(4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Catat",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Catat",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // Laporan (UC2) — right
            NavItem(
                icon = Icons.Default.DateRange,
                label = "Laporan",
                selected = currentTab == Tab.LAPORAN,
                onClick = { onTabSelected(Tab.LAPORAN) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Navigation item for the side tabs (Pengaturan, Laporan).
 * Large touch targets, icons with labels — not icon-only.
 */
@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(28.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = tint,
        )
    }
}
