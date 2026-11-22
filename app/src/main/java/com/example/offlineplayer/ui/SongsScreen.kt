package com.example.offlineplayer.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.example.offlineplayer.ui.theme.LiquidGreen
import com.example.offlineplayer.ui.theme.LiquidGreenLight
import com.example.offlineplayer.ui.theme.TextDim
import com.example.offlineplayer.ui.theme.TextMuted
import com.example.offlineplayer.viewmodel.PlayerViewModel
import com.example.offlineplayer.viewmodel.SortBy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun glassChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = Color(0x1AFFFFFF),
    labelColor = Color.White,
    iconColor = LiquidGreen,
    selectedContainerColor = Color(0x331ED760),
    selectedLabelColor = LiquidGreenLight
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsScreen(vm: PlayerViewModel) {
    val artists = remember(vm.songs) {
        vm.songs.map { it.artist }.distinct().sortedBy { it.lowercase() }
    }
    var sortMenu by remember { mutableStateOf(false) }
    var artistMenu by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        ScreenTitle(
            title = "Lagu Kamu",
            subtitle = if (vm.loading) "Memuat musik…" else "${vm.songs.size} lagu siap diputar"
        )

        // Search Bar Liquid Glass
        OutlinedTextField(
            value = vm.query,
            onValueChange = { vm.query = it },
            placeholder = {
                Text(
                    "Cari judul, artis, atau album…",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = null,
                    tint = LiquidGreen,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (vm.query.isNotEmpty()) {
                    IconButton(onClick = { vm.query = "" }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Hapus",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = CircleShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0x1FFFFFFF),
                unfocusedContainerColor = Color(0x14FFFFFF),
                focusedBorderColor = LiquidGreen,
                unfocusedBorderColor = Color.White.copy(alpha = 0.16f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        // Filter Chips Bar (Liquid Glass)
        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box {
                FilterChip(
                    selected = false,
                    onClick = { sortMenu = true },
                    label = {
                        Text(
                            "Urut: ${vm.sortBy.label}",
                            fontWeight = FontWeight.Medium
                        )
                    },
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.20f)),
                    colors = glassChipColors()
                )
                DropdownMenu(
                    expanded = sortMenu,
                    onDismissRequest = { sortMenu = false },
                    modifier = Modifier
                        .background(Color(0xE6141D17))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
                ) {
                    SortBy.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label, color = Color.White) },
                            onClick = { vm.changeSort(option); sortMenu = false }
                        )
                    }
                }
            }
            Box {
                FilterChip(
                    selected = vm.artistFilter != null,
                    onClick = { artistMenu = true },
                    label = {
                        Text(
                            vm.artistFilter ?: "Semua artis",
                            maxLines = 1,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    shape = CircleShape,
                    border = BorderStroke(
                        1.dp,
                        if (vm.artistFilter != null) LiquidGreen.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.20f)
                    ),
                    colors = glassChipColors()
                )
                DropdownMenu(
                    expanded = artistMenu,
                    onDismissRequest = { artistMenu = false },
                    modifier = Modifier
                        .background(Color(0xE6141D17))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
                ) {
                    DropdownMenuItem(
                        text = { Text("Semua artis", color = Color.White) },
                        onClick = { vm.artistFilter = null; artistMenu = false }
                    )
                    artists.forEach { name ->
                        DropdownMenuItem(
                            text = { Text(name, color = Color.White) },
                            onClick = { vm.artistFilter = name; artistMenu = false }
                        )
                    }
                }
            }
            FilterChip(
                selected = vm.minOneMinute,
                onClick = { vm.minOneMinute = !vm.minOneMinute },
                label = { Text("Minimal 1 menit", fontWeight = FontWeight.Medium) },
                shape = CircleShape,
                border = BorderStroke(
                    1.dp,
                    if (vm.minOneMinute) LiquidGreen.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.20f)
                ),
                colors = glassChipColors()
            )
        }

        when {
            vm.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LiquidGreen)
            }
            vm.songs.isEmpty() -> EmptyState("Belum ada file musik di perangkat ini.")
            vm.visibleSongs.isEmpty() -> EmptyState("Tidak ada lagu yang cocok dengan pencarian.")
            else -> SongList(vm.visibleSongs, vm)
        }
    }
}

@Composable
fun EmptyState(text: String) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            Modifier
                .padding(top = 40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x14FFFFFF))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Text(
                text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = TextDim
            )
        }
    }
}
