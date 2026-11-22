package com.example.offlineplayer.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.example.offlineplayer.ui.theme.AppGradient
import com.example.offlineplayer.ui.theme.LiquidGreen
import com.example.offlineplayer.ui.theme.TextDim
import com.example.offlineplayer.viewmodel.PlayerViewModel

@Composable
fun MiniPlayer(vm: PlayerViewModel, onClick: () -> Unit) {
    val song = vm.currentSong ?: return
    val shape = RoundedCornerShape(24.dp)

    Column(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .shadow(
                elevation = 14.dp,
                shape = shape,
                ambientColor = Color.Black,
                spotColor = Color(0x401ED760)
            )
            .clip(shape)
            .background(Color(0xE0111A14))
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.28f),
                        Color(0xFF1ED760).copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.08f)
                    )
                ),
                shape
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier.padding(start = 10.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlbumArt(song, Modifier.size(46.dp), corner = 14.dp)
            Spacer(Modifier.width(12.dp))
            AnimatedContent(
                targetState = song,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220)) + slideInVertically(
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
                label = "miniPlayerSongInfo"
            ) { currentSong ->
                Column {
                    Text(
                        currentSong.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        currentSong.artist,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = TextDim
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            // Play/Pause button bulat Liquid Green dengan transisi halus
            IconButton(
                onClick = { vm.togglePlay() },
                modifier = Modifier
                    .size(42.dp)
                    .background(LiquidGreen, CircleShape)
            ) {
                Crossfade(
                    targetState = vm.isPlaying,
                    animationSpec = tween(200),
                    label = "miniPlayerPlayPauseCrossfade"
                ) { playing ->
                    Icon(
                        if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Putar/jeda",
                        tint = Color(0xFF031408),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.width(4.dp))
            IconButton(
                onClick = { vm.next() },
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    Icons.Filled.SkipNext,
                    contentDescription = "Berikutnya",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        val targetFraction = if (vm.durationMs > 0)
            (vm.positionMs.toFloat() / vm.durationMs).coerceIn(0f, 1f) else 0f
        val animatedFraction by animateFloatAsState(
            targetValue = targetFraction,
            animationSpec = tween(durationMillis = 400, easing = LinearEasing),
            label = "miniPlayerProgress"
        )
        Box(
            Modifier
                .padding(horizontal = 14.dp, vertical = 2.dp)
                .fillMaxWidth()
                .height(2.5.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
        ) {
            if (animatedFraction > 0.001f) {
                Box(
                    Modifier
                        .fillMaxWidth(animatedFraction)
                        .fillMaxHeight()
                        .background(AppGradient)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}
