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
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.offlineplayer.viewmodel.PlayerViewModel

@Composable
fun FoldersScreen(vm: PlayerViewModel) {
    val folders = remember(vm.songs) {
        vm.songs.groupBy { it.folderPath }.toList().sortedBy { it.first.lowercase() }
    }
    val open = vm.openFolder

    if (open == null) {
        Column(Modifier.fillMaxSize()) {
            Text(
                "Folder",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 8.dp)
            )
            if (folders.isEmpty()) {
                Text(
                    "Belum ada folder berisi musik.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
            LazyColumn(Modifier.fillMaxSize()) {
                items(folders, key = { it.first }) { entry ->
                    val path = entry.first
                    val list = entry.second
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { vm.openFolder = path }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Folder, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(list.first().folderName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                "${list.size} lagu • ${path.removePrefix("/storage/emulated/0/")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            Spacer(Modifier.width(4.dp))
                            Text("Simpan sebagai playlist")
                        }
                    }
                )
            }
        )
    }
}
