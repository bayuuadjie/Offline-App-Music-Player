package com.example.offlineplayer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.example.offlineplayer.ui.theme.LiquidGreen
import com.example.offlineplayer.ui.theme.TextDim
import com.example.offlineplayer.viewmodel.PlayerViewModel

@Composable
fun FoldersScreen(vm: PlayerViewModel) {
    val folders = remember(vm.songs) {
        vm.songs.groupBy { it.folderPath }.toList().sortedBy { it.first.lowercase() }
    }
    val open = vm.openFolder

    AnimatedContent(
        targetState = open,
        transitionSpec = {
            if (targetState != null) {
                (slideInHorizontally(
                    animationSpec = spring(
                        dampingRatio = 0.85f,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    initialOffsetX = { it / 3 }
                ) + fadeIn(animationSpec = tween(250))).togetherWith(
                    slideOutHorizontally(
                        animationSpec = spring(
                            dampingRatio = 0.85f,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        targetOffsetX = { -it / 3 }
                    ) + fadeOut(animationSpec = tween(180))
                )
            } else {
                (slideInHorizontally(
                    animationSpec = spring(
                        dampingRatio = 0.85f,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    initialOffsetX = { -it / 3 }
                ) + fadeIn(animationSpec = tween(250))).togetherWith(
                    slideOutHorizontally(
                        animationSpec = spring(
                            dampingRatio = 0.85f,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        targetOffsetX = { it / 3 }
                    ) + fadeOut(animationSpec = tween(180))
                )
            }
        },
        label = "foldersNavigation"
    ) { currentFolder ->
        if (currentFolder == null) {
            Column(Modifier.fillMaxSize()) {
                ScreenTitle("Folder", "${folders.size} folder musik otomatis")
                if (folders.isEmpty()) EmptyState("Belum ada folder berisi musik.")

                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 140.dp)
                ) {
                    items(folders, key = { it.first }) { entry ->
                        val path = entry.first
                        val list = entry.second
                        val shape = RoundedCornerShape(20.dp)

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(shape)
                                .background(Color(0x14FFFFFF))
                                .border(
                                    1.dp,
                                    Brush.linearGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.22f),
                                            Color.White.copy(alpha = 0.05f)
                                        )
                                    ),
                                    shape
                                )
                                .clickable { vm.openFolder = path }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconTile(Icons.Filled.Folder, gradient = false)
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    list.first().folderName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    "${list.size} lagu • ${path.removePrefix("/storage/emulated/0/")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextDim,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        } else {
            BackHandler { vm.openFolder = null }
            val list = folders.firstOrNull { it.first == currentFolder }?.second.orEmpty()
            val folderTitle = list.firstOrNull()?.folderName ?: "Folder"

            SongList(
                songs = list,
                vm = vm,
                header = {
                    DetailHeader(
                        title = folderTitle,
                        subtitle = "${list.size} lagu di folder ini",
                        onBack = { vm.openFolder = null },
                        onPlayAll = { vm.play(list, 0) },
                        onShuffle = { vm.play(list.shuffled(), 0) },
                        extra = {
                            OutlinedButton(
                                onClick = {
                                    vm.createPlaylist(folderTitle, list.map { it.id })
                                },
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, LiquidGreen.copy(alpha = 0.6f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color(0x1F1ED760),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.PlaylistAdd,
                                    contentDescription = null,
                                    tint = LiquidGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Simpan sebagai playlist",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    )
                }
            )
        }
    }
}
