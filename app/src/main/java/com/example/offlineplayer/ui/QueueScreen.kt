package com.example.offlineplayer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.example.offlineplayer.ui.theme.DarkScreenGradient
import com.example.offlineplayer.ui.theme.LiquidGreen
import com.example.offlineplayer.ui.theme.LiquidGreenLight
import com.example.offlineplayer.ui.theme.TextDim
import com.example.offlineplayer.ui.theme.TextMuted
import com.example.offlineplayer.viewmodel.PlayerViewModel

@Composable
fun QueueScreen(vm: PlayerViewModel, onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (vm.currentIndex - 2).coerceAtLeast(0)
    )
    val lastIndex = vm.queue.lastIndex

    Box(
        Modifier
            .fillMaxSize()
            .background(DarkScreenGradient)
    ) {
        Column(Modifier.statusBarsPadding().navigationBarsPadding()) {
            // Header Bar
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 20.dp, top = 10.dp, bottom = 12.dp),
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
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Antrian Musik",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "${vm.queue.size} lagu siap diputar",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDim
                    )
                }
            }

            if (vm.shuffleOn) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x1F1ED760))
                        .border(1.dp, LiquidGreen.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Shuffle, contentDescription = null, tint = LiquidGreen, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Mode acak aktif: urutan putar diacak, daftar ini menampilkan urutan asli.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }

            if (vm.queue.isEmpty()) EmptyState("Antrian kosong.")

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 40.dp)
            ) {
                itemsIndexed(vm.queue) { index, item ->
                    val isCurrent = index == vm.currentIndex
                    val shape = RoundedCornerShape(18.dp)

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(shape)
                            .background(
                                if (isCurrent) Color(0x2B1ED760) else Color(0x0FFFFFFF)
                            )
                            .border(
                                1.dp,
                                if (isCurrent) {
                                    Brush.horizontalGradient(
                                        listOf(LiquidGreen.copy(alpha = 0.7f), Color.White.copy(alpha = 0.25f))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(Color.White.copy(alpha = 0.1f), Color.Transparent)
                                    )
                                },
                                shape
                            )
                            .clickable { vm.jumpTo(index) }
                            .padding(start = 14.dp, top = 8.dp, end = 6.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                item.title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isCurrent) LiquidGreenLight else Color.White,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                item.artist,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = TextMuted
                            )
                        }

                        IconButton(
                            onClick = { vm.moveInQueue(index, index - 1) },
                            enabled = index > 0,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Filled.ArrowUpward,
                                contentDescription = "Naik",
                                modifier = Modifier.size(18.dp),
                                tint = if (index > 0) Color.White else Color.White.copy(alpha = 0.25f)
                            )
                        }

                        IconButton(
                            onClick = { vm.moveInQueue(index, index + 1) },
                            enabled = index < lastIndex,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Filled.ArrowDownward,
                                contentDescription = "Turun",
                                modifier = Modifier.size(18.dp),
                                tint = if (index < lastIndex) Color.White else Color.White.copy(alpha = 0.25f)
                            )
                        }

                        IconButton(
                            onClick = { vm.removeFromQueue(index) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Hapus",
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFFFF5252).copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}
