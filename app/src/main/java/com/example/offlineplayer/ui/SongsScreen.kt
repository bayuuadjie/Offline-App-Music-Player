package com.example.offlineplayer.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.example.offlineplayer.viewmodel.PlayerViewModel
import com.example.offlineplayer.viewmodel.SortBy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun appChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = MaterialTheme.colorScheme.surfaceVariant,
    labelColor = Color.White,
    iconColor = Color.White,
    selectedContainerColor = MaterialTheme.colorScheme.primary,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
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
            subtitle = if (vm.loading) "Memuat…" else "${vm.songs.size} lagu siap diputar"
        )

        OutlinedTextField(
            value = vm.query,
            onValueChange = { vm.query = it },
            placeholder = { Text("Cari judul, artis, atau album", color = Color.White.copy(alpha = 0.6f)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.8f)) },
            trailingIcon = {
                if (vm.query.isNotEmpty()) {
                    IconButton(onClick = { vm.query = "" }) {
                        Icon(Icons.Filled.Close, contentDescription = "Hapus", tint = Color.White)
                    }
                }
            },
            singleLine = true,
            shape = CircleShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        )

        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box {
                FilterChip(
                    selected = false,
                    onClick = { sortMenu = true },
                    label = { Text("Urut: ${vm.sortBy.label}") },
                    shape = CircleShape,
                    colors = appChipColors()
                )
                DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                    SortBy.values().forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = { vm.changeSort(option); sortMenu = false }
                        )
                    }
                }
            }
            Box {
                FilterChip(
                    selected = vm.artistFilter != null,
                    onClick = { artistMenu = true },
                    label = { Text(vm.artistFilter ?: "Semua artis", maxLines = 1) },
                    shape = CircleShape,
                    colors = appChipColors()
                )
                DropdownMenu(expanded = artistMenu, onDismissRequest = { artistMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Semua artis") },
                        onClick = { vm.artistFilter = null; artistMenu = false }
                    )
                    artists.forEach { name ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = { vm.artistFilter = name; artistMenu = false }
                        )
                    }
                }
            }
            FilterChip(
                selected = vm.minOneMinute,
                onClick = { vm.minOneMinute = !vm.minOneMinute },
                label = { Text("Minimal 1 menit") },
                shape = CircleShape,
                colors = appChipColors()
            )
        }

        when {
            vm.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            vm.songs.isEmpty() -> EmptyState("Belum ada musik di perangkat ini.")
            vm.visibleSongs.isEmpty() -> EmptyState("Tidak ada lagu yang cocok.")
            else -> SongList(vm.visibleSongs, vm)
        }
    }
}

@Composable
fun EmptyState(text: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.TopCenter) {
        Text(
            text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.padding(top = 48.dp)
        )
    }
}
