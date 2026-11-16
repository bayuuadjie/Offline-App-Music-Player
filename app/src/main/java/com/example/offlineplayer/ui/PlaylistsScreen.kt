package com.example.offlineplayer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*
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
            ScreenTitle(
                title = "Playlist",
                subtitle = "${vm.playlists.size} playlist buatanmu",
                trailing = {
                    IconButton(
                        onClick = { newName = ""; showCreate = true },
                        modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            Icons.Filled.Add, contentDescription = "Playlist baru",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
            if (vm.playlists.isEmpty()) {
                EmptyState("Belum ada playlist. Tekan + untuk membuat, atau buka tab Folder lalu pilih \"Simpan sebagai playlist\".")
            }
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(vm.playlists, key = { it.id }) { p ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { vm.openPlaylistId = p.id }
                            .padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconTile(Icons.Filled.LibraryMusic, gradient = true)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.name, style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "${p.songIds.size} lagu",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                        IconButton(onClick = { deleteTarget = p }) {
                            Icon(
                                Icons.Filled.Delete, contentDescription = "Hapus playlist",
                                tint = Color.White.copy(alpha = 0.75f)
                            )
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
            title = { Text("Playlist baru", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nama playlist", color = Color.White.copy(alpha = 0.75f)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
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
            title = { Text("Hapus playlist?", color = Color.White) },
            text = { Text("Playlist \"${target.name}\" akan dihapus. File musiknya tidak ikut terhapus.", color = Color.White.copy(alpha = 0.85f)) },
            confirmButton = {
                TextButton(onClick = { vm.deletePlaylist(target.id); deleteTarget = null }) { Text("Hapus") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Batal") } }
        )
    }
}
