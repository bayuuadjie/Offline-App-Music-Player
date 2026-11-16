package com.example.offlineplayer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.media3.common.Player
import com.example.offlineplayer.ui.theme.Pink
import com.example.offlineplayer.ui.theme.Violet
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
    val sleepActive = vm.sleepAfterSong || vm.sleepEndAt > now
    val sleepLabel = when {
        vm.sleepAfterSong -> "Setelah lagu ini"
        vm.sleepEndAt > now -> formatTime(vm.sleepEndAt - now)
        else -> "Timer tidur"
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF3B2278), MaterialTheme.colorScheme.background),
                    startY = 0f, endY = 1700f
                )
            )
    ) {
        Column(
            Modifier
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Tutup", modifier = Modifier.size(32.dp))
                }
                Text(
                    "SEDANG DIPUTAR",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall,
                    color = dim
                )
                Spacer(Modifier.size(48.dp))
            }

            Spacer(Modifier.height(24.dp))
            AlbumArt(
                song,
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .shadow(24.dp, RoundedCornerShape(28.dp), ambientColor = Violet, spotColor = Pink),
                corner = 28.dp
            )

            Spacer(Modifier.height(32.dp))
            Text(
                song?.title ?: "-",
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            Text(
                song?.artist ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = dim,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))
            Slider(
                value = shownPos,
                onValueChange = { dragging = true; dragPos = it },
                onValueChangeFinished = { vm.seekTo(dragPos.toLong()); dragging = false },
                valueRange = 0f..maxMs,
                colors = SliderDefaults.colors(
                    thumbColor = primary,
                    activeTrackColor = primary,
                    inactiveTrackColor = Color.White.copy(alpha = 0.18f)
                )
            )
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatTime(shownPos.toLong()), style = MaterialTheme.typography.bodySmall, color = dim)
                Text(formatTime(vm.durationMs), style = MaterialTheme.typography.bodySmall, color = dim)
            }

            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
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
                    modifier = Modifier.size(76.dp).background(primary, CircleShape)
                ) {
                    Icon(
                        if (vm.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Putar/jeda",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(42.dp)
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

            Spacer(Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(onClick = onOpenSleep, shape = CircleShape) {
                    Icon(
                        Icons.Filled.Bedtime, contentDescription = null,
                        tint = if (sleepActive) primary else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(sleepLabel, color = if (sleepActive) primary else Color.White)
                }
                FilledTonalButton(onClick = onOpenQueue, shape = CircleShape) {
                    Icon(Icons.Filled.QueueMusic, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Antrian")
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}
