package com.example.offlineplayer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.example.offlineplayer.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay

@Composable
fun NowPlayingScreen(
    vm: PlayerViewModel,
    onClose: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenSleep: () -> Unit
) {
    BackHandler(onBack = onClose)
    val song = vm.currentSong
    val primary = MaterialTheme.colorScheme.primary
    val dim = MaterialTheme.colorScheme.onSurfaceVariant

    var dragging by remember { mutableStateOf(false) }
    var dragPos by remember { mutableStateOf(0f) }
    val maxMs = vm.durationMs.coerceAtLeast(1L).toFloat()
    val shownPos = if (dragging) dragPos else vm.positionMs.toFloat().coerceIn(0f, maxMs)

    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(1000)
        }
    }
    val sleepLabel = when {
        vm.sleepAfterSong -> "Setelah lagu ini"
        vm.sleepEndAt > now -> formatTime(vm.sleepEndAt - now)
        else -> "Timer tidur"
    }
    val sleepActive = vm.sleepAfterSong || vm.sleepEndAt > now

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Tutup", modifier = Modifier.size(32.dp))
                }
                Text(
                    "Sedang diputar",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall
                )
                IconButton(onClick = onOpenQueue) {
                    Icon(Icons.Filled.QueueMusic, contentDescription = "Antrian")
                }
            }

            Spacer(Modifier.height(16.dp))
            AlbumArt(song, Modifier.fillMaxWidth().aspectRatio(1f))
            Spacer(Modifier.height(24.dp))

            Text(
                song?.title ?: "-",
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                song?.artist ?: "",
                color = dim,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
            Slider(
                value = shownPos,
                onValueChange = { dragging = true; dragPos = it },
                onValueChangeFinished = { vm.seekTo(dragPos.toLong()); dragging = false },
                valueRange = 0f..maxMs
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime(shownPos.toLong()), style = MaterialTheme.typography.bodySmall, color = dim)
                Text(formatTime(vm.durationMs), style = MaterialTheme.typography.bodySmall, color = dim)
            }

            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { vm.toggleShuffle() }) {
                    Icon(
                        Icons.Filled.Shuffle, contentDescription = "Acak",
                        tint = if (vm.shuffleOn) primary else dim
                    )
                }
                IconButton(onClick = { vm.previous() }, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = "Sebelumnya", modifier = Modifier.size(36.dp))
                }
                IconButton(
                    onClick = { vm.togglePlay() },
                    modifier = Modifier.size(72.dp).background(Color.White, CircleShape)
                ) {
                    Icon(
                        if (vm.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Putar/jeda",
                        tint = Color.Black,
                        modifier = Modifier.size(40.dp)
                    )
                }
                IconButton(onClick = { vm.next() }, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "Berikutnya", modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = { vm.cycleRepeat() }) {
                    Icon(
                        if (vm.repeatMode == Player.REPEAT_MODE_ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                        contentDescription = "Ulangi",
                        tint = if (vm.repeatMode == Player.REPEAT_MODE_OFF) dim else primary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onOpenSleep) {
                Icon(
                    Icons.Filled.Bedtime, contentDescription = null,
                    tint = if (sleepActive) primary else dim
                )
                Spacer(Modifier.width(8.dp))
                Text(sleepLabel, color = if (sleepActive) primary else dim)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
