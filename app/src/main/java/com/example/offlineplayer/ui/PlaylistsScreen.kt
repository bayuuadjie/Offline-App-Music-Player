package com.example.offlineplayer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.offlineplayer.data.UserPlaylist
import com.example.offlineplayer.viewmodel.PlayerViewModel

@Composable
fun PlaylistsScreen(vm: PlayerViewModel) {
    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<UserPlaylist?>(null) }

    val open = vm.playlists.firstOrNull { it.id == vm.openPlaylistId }

    if (open == null) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Playlist",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { newName = ""; showCreate = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Playlist baru")
                }
            }
            if (vm.playlists.isEmpty()) {
                Text(
                    "Belum ada playlist. Tekan + untuk membuat, atau buka tab Folder lalu pilih \"Simpan sebagai playlist\".",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
            LazyColumn(Modifier.fillMaxSize()) {
                items(vm.playlists, key = { it.id }) { p ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { vm.openPlaylistId = p.id }
                            .padding(start = 16.dp, top = 8.dp, end = 4.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.LibraryMusic, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.name)
                            Text(
                                "${p.songIds.size} lagu",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { deleteTarget = p }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Hapus playlist")
                        }
                    }
                }
            }
        }
    } else {
        BackHandler { vm.openPlaylistId = null }
        val list = vm.playlistSongs(open)
        SongList(
            songs = list,
            vm = vm,
            onRemove = { song -> vm.removeFromPlaylist(open.id, song.id) },
            header = {
                DetailHeader(
                    title = open.name,
                    subtitle = "${list.size} lagu",
                    onBack = { vm.openPlaylistId = null },
                    onPlayAll = { vm.play(list, 0) },
                    onShuffle = { vm.play(list.shuffled(), 0) }
                )
            }
        )
    }

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("Playlist baru") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nama playlist") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank()) vm.createPlaylist(newName.trim())
                    showCreate = false
                }) { Text("Buat") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Batal") } }
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Hapus playlist?") },
            text = { Text("Playlist \"${target.name}\" akan dihapus. File musiknya tidak ikut terhapus.") },
            confirmButton = {
                TextButton(onClick = { vm.deletePlaylist(target.id); deleteTarget = null }) { Text("Hapus") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Batal") } }
        )
    }
}
