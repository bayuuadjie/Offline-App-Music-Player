package com.example.offlineplayer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.media3.common.Player
import com.example.offlineplayer.ui.theme.LiquidGreen
import com.example.offlineplayer.ui.theme.LiquidGreenLight
import com.example.offlineplayer.ui.theme.TextDim
import com.example.offlineplayer.ui.theme.TextMuted
import com.example.offlineplayer.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun NowPlayingScreen(
    vm: PlayerViewModel,
    onClose: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenSleep: () -> Unit
) {
    BackHandler(onBack = onClose)
    val song = vm.currentSong
    val primary = LiquidGreen

    var dragging by remember { mutableStateOf(false) }
    var dragPos by remember { mutableStateOf(0f) }
    val maxMs = vm.durationMs.coerceAtLeast(1L).toFloat()
    val shownPos = if (dragging) dragPos else vm.positionMs.toFloat().coerceIn(0f, maxMs)

    var isSwipingDown by remember { mutableStateOf(false) }
    var swipeOffsetY by remember { mutableFloatStateOf(0f) }
    val animatedOffsetY by animateFloatAsState(
        targetValue = swipeOffsetY,
        animationSpec = if (isSwipingDown) snap() else spring(
            dampingRatio = 0.78f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "nowPlayingSwipeOffset"
    )

    // Animasi skala album: membesar saat play, mengecil halus saat pause
    val albumScale by animateFloatAsState(
        targetValue = if (vm.isPlaying) 1f else 0.88f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "albumArtScale"
    )

    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(1000)
        }
    }
    val sleepActive = vm.sleepAfterSong || vm.sleepEndAt > now
    val sleepLabel = when {
        vm.sleepAfterSong -> "Selesai lagu ini"
        vm.sleepEndAt > now -> formatTime(vm.sleepEndAt - now)
        else -> "Timer Tidur"
    }

    Box(
        Modifier
            .fillMaxSize()
            .offset { IntOffset(0, animatedOffsetY.roundToInt()) }
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF132B1B), // Dark emerald ambient
                        Color(0xFF09120D),
                        Color(0xFF060907)
                    )
                )
            )
    ) {
        Column(
            Modifier
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag Pill Handle ala iOS Bottom Sheet & gestur geser ke bawah untuk menutup bar
            Box(
                Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { isSwipingDown = true },
                            onDragEnd = {
                                isSwipingDown = false
                                if (swipeOffsetY > 160f) {
                                    swipeOffsetY = 0f
                                    onClose()
                                } else {
                                    swipeOffsetY = 0f
                                }
                            },
                            onDragCancel = {
                                isSwipingDown = false
                                swipeOffsetY = 0f
                            },
                            onVerticalDrag = { _, dragAmount ->
                                if (dragAmount > 0 || swipeOffsetY > 0) {
                                    swipeOffsetY = (swipeOffsetY + dragAmount * 0.85f).coerceAtLeast(0f)
                                }
                            }
                        )
                    }
                    .padding(top = 10.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .width(44.dp)
                        .height(4.5.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.30f))
                )
            }

            // Header Top Bar
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x22FFFFFF))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Tutup",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Text(
                    "SEDANG DIPUTAR",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextDim,
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.size(40.dp))
            }

            Spacer(Modifier.height(24.dp))

            // Cover Album Liquid Glass dengan animasi skala saat play/pause
            AlbumArt(
                song,
                Modifier
                    .fillMaxWidth(0.88f)
                    .aspectRatio(1f)
                    .scale(albumScale)
                    .shadow(
                        elevation = 28.dp,
                        shape = RoundedCornerShape(32.dp),
                        ambientColor = Color.Black,
                        spotColor = LiquidGreen.copy(alpha = 0.5f)
                    ),
                corner = 32.dp
            )

            Spacer(Modifier.height(28.dp))

            // Info Lagu dengan transisi animasi saat ganti lagu
            AnimatedContent(
                targetState = song,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(240)) + slideInVertically(
                        animationSpec = spring(
                            dampingRatio = 0.8f,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        initialOffsetY = { it / 3 }
                    )).togetherWith(
                        fadeOut(animationSpec = tween(150)) + slideOutVertically(
                            animationSpec = tween(150),
                            targetOffsetY = { -it / 3 }
                        )
                    )
                },
                label = "nowPlayingSongInfo"
            ) { currentSong ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        currentSong?.title ?: "Tidak ada lagu",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        currentSong?.artist ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextDim,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            // Seekbar Slider Liquid Glass
            Slider(
                value = shownPos,
                onValueChange = { dragging = true; dragPos = it },
                onValueChangeFinished = { vm.seekTo(dragPos.toLong()); dragging = false },
                valueRange = 0f..maxMs,
                colors = SliderDefaults.colors(
                    thumbColor = LiquidGreenLight,
                    activeTrackColor = primary,
                    inactiveTrackColor = Color.White.copy(alpha = 0.16f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    formatTime(shownPos.toLong()),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Text(
                    formatTime(vm.durationMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            Spacer(Modifier.height(18.dp))

            // Tombol Kontrol Pemutar
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { vm.toggleShuffle() }) {
                    Icon(
                        Icons.Filled.Shuffle,
                        contentDescription = "Acak",
                        tint = if (vm.shuffleOn) primary else TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = { vm.previous() },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        Icons.Filled.SkipPrevious,
                        contentDescription = "Sebelumnya",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Play / Pause Utama (Bulat Besar Hijau Liquid dengan transisi halus)
                IconButton(
                    onClick = { vm.togglePlay() },
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(16.dp, CircleShape, spotColor = LiquidGreen)
                        .background(primary, CircleShape)
                ) {
                    Crossfade(
                        targetState = vm.isPlaying,
                        animationSpec = tween(200),
                        label = "nowPlayingPlayPauseCrossfade"
                    ) { playing ->
                        Icon(
                            if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Putar/jeda",
                            tint = Color(0xFF031408),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { vm.next() },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        Icons.Filled.SkipNext,
                        contentDescription = "Berikutnya",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                IconButton(onClick = { vm.cycleRepeat() }) {
                    Icon(
                        if (vm.repeatMode == Player.REPEAT_MODE_ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                        contentDescription = "Ulangi",
                        tint = if (vm.repeatMode == Player.REPEAT_MODE_OFF) TextMuted else primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // Tombol Pill Liquid Glass di bagian bawah (Sleep Timer & Queue)
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                // Sleep Timer Button
                OutlinedButton(
                    onClick = onOpenSleep,
                    shape = CircleShape,
                    border = BorderStroke(
                        1.dp,
                        if (sleepActive) LiquidGreen.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.25f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (sleepActive) Color(0x2E1ED760) else Color(0x1AFFFFFF),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Icon(
                        Icons.Filled.Bedtime,
                        contentDescription = null,
                        tint = if (sleepActive) LiquidGreen else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        sleepLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (sleepActive) LiquidGreen else Color.White
                    )
                }

                // Queue Button
                OutlinedButton(
                    onClick = onOpenQueue,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0x1AFFFFFF),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = null,
                        tint = LiquidGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Antrian",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
