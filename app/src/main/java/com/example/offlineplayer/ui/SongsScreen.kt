package com.example.offlineplayer.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.offlineplayer.viewmodel.PlayerViewModel
import com.example.offlineplayer.viewmodel.SortBy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsScreen(vm: PlayerViewModel) {
    val artists = remember(vm.songs) {
        vm.songs.map { it.artist }.distinct().sortedBy { it.lowercase() }
    }
    var sortMenu by remember { mutableStateOf(false) }
    var artistMenu by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Text(
            "Lagu",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 8.dp)
        )

        OutlinedTextField(
            value = vm.query,
            onValueChange = { vm.query = it },
            placeholder = { Text("Cari judul, artis, atau album") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (vm.query.isNotEmpty()) {
                    IconButton(onClick = { vm.query = "" }) {
                        Icon(Icons.Filled.Close, contentDescription = "Hapus")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box {
                FilterChip(
                    selected = false,
                    onClick = { sortMenu = true },
                    label = { Text("Urut: ${vm.sortBy.label}") }
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
                    label = { Text(vm.artistFilter ?: "Semua artis") }
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
                label = { Text("Minimal 1 menit") }
            )
        }

        when {
            vm.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            vm.songs.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Tidak ada musik ditemukan di perangkat.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            vm.visibleSongs.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Tidak ada lagu yang cocok.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> SongList(vm.visibleSongs, vm)
        }
    }
}
