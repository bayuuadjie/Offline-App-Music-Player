package com.example.offlineplayer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.offlineplayer.viewmodel.PlayerViewModel

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

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                Column {
                    MiniPlayer(vm) { showNow = true }
                    NavigationBar {
                        tabs.forEachIndexed { i, (label, icon) ->
                            NavigationBarItem(
                                selected = tab == i,
                                onClick = { tab = i },
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label) }
                            )
                        }
                    }
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
