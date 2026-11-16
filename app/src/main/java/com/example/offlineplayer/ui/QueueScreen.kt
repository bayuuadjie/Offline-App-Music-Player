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
fun QueueScreen(vm: PlayerViewModel, onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (vm.currentIndex - 2).coerceAtLeast(0)
    )
    val lastIndex = vm.queue.lastIndex

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.statusBarsPadding().navigationBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(start = 8.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                }
                Spacer(Modifier.width(4.dp))
                Text("Antrian", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                Text(
                    "${vm.queue.size} lagu",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (vm.shuffleOn) {
                Text(
                    "Mode acak aktif: urutan putar diacak, daftar ini menampilkan urutan asli.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
                )
            }
            if (vm.queue.isEmpty()) EmptyState("Antrian kosong.")
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(vm.queue) { index, item ->
                    val isCurrent = index == vm.currentIndex
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (isCurrent) MaterialTheme.colorScheme.secondaryContainer
                                else Color.Transparent
                            )
                            .clickable { vm.jumpTo(index) }
                            .padding(start = 14.dp, top = 8.dp, end = 2.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                item.title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isCurrent) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                item.artist,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { vm.moveInQueue(index, index - 1) },
                            enabled = index > 0,
                            modifier = Modifier.size(38.dp)
                        ) { Icon(Icons.Filled.ArrowUpward, contentDescription = "Naik", modifier = Modifier.size(20.dp)) }
                        IconButton(
                            onClick = { vm.moveInQueue(index, index + 1) },
                            enabled = index < lastIndex,
                            modifier = Modifier.size(38.dp)
                        ) { Icon(Icons.Filled.ArrowDownward, contentDescription = "Turun", modifier = Modifier.size(20.dp)) }
                        IconButton(
                            onClick = { vm.removeFromQueue(index) },
                            modifier = Modifier.size(38.dp)
                        ) { Icon(Icons.Filled.Close, contentDescription = "Hapus", modifier = Modifier.size(20.dp)) }
                    }
                }
            }
        }
    }
}
