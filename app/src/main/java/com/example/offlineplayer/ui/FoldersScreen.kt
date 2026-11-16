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
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.example.offlineplayer.viewmodel.PlayerViewModel

@Composable
fun FoldersScreen(vm: PlayerViewModel) {
    val folders = remember(vm.songs) {
        vm.songs.groupBy { it.folderPath }.toList().sortedBy { it.first.lowercase() }
    }
    val open = vm.openFolder

    if (open == null) {
        Column(Modifier.fillMaxSize()) {
            ScreenTitle("Folder", "${folders.size} folder jadi playlist otomatis")
            if (folders.isEmpty()) EmptyState("Belum ada folder berisi musik.")
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(folders, key = { it.first }) { entry ->
                    val path = entry.first
                    val list = entry.second
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { vm.openFolder = path }
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconTile(Icons.Filled.Folder)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                list.first().folderName,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "${list.size} lagu • ${path.removePrefix("/storage/emulated/0/")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.75f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    } else {
        BackHandler { vm.openFolder = null }
        val list = folders.firstOrNull { it.first == open }?.second.orEmpty()
        SongList(
            songs = list,
            vm = vm,
            header = {
                DetailHeader(
                    title = list.firstOrNull()?.folderName ?: "Folder",
                    subtitle = "${list.size} lagu",
                    onBack = { vm.openFolder = null },
                    onPlayAll = { vm.play(list, 0) },
                    onShuffle = { vm.play(list.shuffled(), 0) },
                    extra = {
                        TextButton(onClick = {
                            vm.createPlaylist(
                                list.firstOrNull()?.folderName ?: "Folder",
                                list.map { it.id }
                            )
                        }) {
                            Icon(Icons.Filled.PlaylistAdd, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Simpan sebagai playlist")
                        }
                    }
                )
            }
        )
    }
}
