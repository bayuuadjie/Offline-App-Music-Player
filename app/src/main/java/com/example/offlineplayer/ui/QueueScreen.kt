package com.example.offlineplayer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.offlineplayer.viewmodel.PlayerViewModel

@Composable
fun QueueScreen(vm: PlayerViewModel, onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (vm.currentIndex - 2).coerceAtLeast(0)
    )
    val lastIndex = vm.queue.lastIndex

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.statusBarsPadding().navigationBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                }
                Text("Antrian (${vm.queue.size})", style = MaterialTheme.typography.titleLarge)
            }
            if (vm.shuffleOn) {
                Text(
                    "Mode acak aktif: urutan putar diacak, daftar di bawah menampilkan urutan asli.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            if (vm.queue.isEmpty()) {
                Text(
                    "Antrian kosong.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                itemsIndexed(vm.queue) { index, item ->
                    val isCurrent = index == vm.currentIndex
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { vm.jumpTo(index) }
                            .padding(start = 16.dp, top = 4.dp, end = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                item.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isCurrent) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                item.artist,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { vm.moveInQueue(index, index - 1) },
                            enabled = index > 0,
                            modifier = Modifier.size(40.dp)
                        ) { Icon(Icons.Filled.ArrowUpward, contentDescription = "Naik") }
                        IconButton(
                            onClick = { vm.moveInQueue(index, index + 1) },
                            enabled = index < lastIndex,
                            modifier = Modifier.size(40.dp)
                        ) { Icon(Icons.Filled.ArrowDownward, contentDescription = "Turun") }
                        IconButton(
                            onClick = { vm.removeFromQueue(index) },
                            modifier = Modifier.size(40.dp)
                        ) { Icon(Icons.Filled.Close, contentDescription = "Hapus") }
                    }
                }
            }
        }
    }
}
