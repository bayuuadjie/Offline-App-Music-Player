package com.example.offlineplayer.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.offlineplayer.viewmodel.PlayerViewModel

@Composable
private fun FloatingNav(
    tabs: List<Pair<String, ImageVector>>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEachIndexed { i, (label, icon) ->
            val sel = i == selected
            Row(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(26.dp))
                    .background(if (sel) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onSelect(i) }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon, contentDescription = label,
                    tint = if (sel) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                if (sel) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        label,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(vm: PlayerViewModel = viewModel()) {
    var tab by rememberSaveable { mutableStateOf(0) }
    var showNow by rememberSaveable { mutableStateOf(false) }
    var showQueue by rememberSaveable { mutableStateOf(false) }
    var showSleep by remember { mutableStateOf(false) }

    val tabs = listOf(
        "Lagu" to Icons.Filled.MusicNote,
        "Folder" to Icons.Filled.Folder,
        "Playlist" to Icons.Filled.LibraryMusic
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF2B1A55), MaterialTheme.colorScheme.background),
                    startY = 0f, endY = 1300f
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
                ) {
                    MiniPlayer(vm) { showNow = true }
                    FloatingNav(tabs, tab) { tab = it }
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                when (tab) {
                    0 -> SongsScreen(vm)
                    1 -> FoldersScreen(vm)
                    else -> PlaylistsScreen(vm)
                }
            }
        }

        if (showNow) {
            NowPlayingScreen(
                vm = vm,
                onClose = { showNow = false },
                onOpenQueue = { showQueue = true },
                onOpenSleep = { showSleep = true }
            )
        }
        if (showQueue) {
            QueueScreen(vm) { showQueue = false }
        }
    }

    if (showSleep) {
        SleepTimerDialog(vm) { showSleep = false }
    }
}
