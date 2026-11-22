package com.example.offlineplayer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.example.offlineplayer.data.Song
import com.example.offlineplayer.data.UserPlaylist
import com.example.offlineplayer.ui.theme.LiquidGreen
import com.example.offlineplayer.ui.theme.LiquidGreenLight
import com.example.offlineplayer.ui.theme.TextDim
import com.example.offlineplayer.ui.theme.TextMuted
import com.example.offlineplayer.viewmodel.PlayerViewModel

@Composable
fun PlaylistsScreen(vm: PlayerViewModel) {
    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<UserPlaylist?>(null) }

    val open = vm.playlists.firstOrNull { it.id == vm.openPlaylistId }

    if (open == null) {
        Column(Modifier.fillMaxSize()) {
            ScreenTitle(
                title = "Playlist",
                subtitle = "${vm.playlists.size} playlist buatanmu",
                trailing = {
                    IconButton(
                        onClick = { newName = ""; showCreate = true },
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(8.dp, CircleShape, spotColor = LiquidGreen)
                            .background(LiquidGreen, CircleShape)
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Playlist baru",
                            tint = Color(0xFF031408),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )

            if (vm.playlists.isEmpty()) {
                EmptyState("Belum ada playlist. Tekan tombol + untuk membuat, atau buka tab Folder lalu pilih \"Simpan sebagai playlist\".")
            }

            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 140.dp)
            ) {
                items(vm.playlists, key = { it.id }) { p ->
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
                            .clickable { vm.openPlaylistId = p.id }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconTile(Icons.Filled.LibraryMusic, gradient = true)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                p.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                "${p.songIds.size} lagu",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextDim
                            )
                        }
                        IconButton(
                            onClick = { deleteTarget = p },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x1AFFFFFF))
                        ) {
                            Icon(
                                Icons.Filled.DeleteOutline,
                                contentDescription = "Hapus playlist",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Layar Detail Playlist
        BackHandler { vm.openPlaylistId = null }
        val playlistSongs = vm.playlistSongs(open)
        val unaddedSongs = remember(open.songIds, vm.songs) {
            vm.songs.filter { it.id !in open.songIds }
        }
        var addSearchQuery by remember { mutableStateOf("") }

        val filteredUnadded = remember(unaddedSongs, addSearchQuery) {
            val q = addSearchQuery.trim()
            if (q.isEmpty()) unaddedSongs
            else unaddedSongs.filter {
                it.title.contains(q, ignoreCase = true) ||
                it.artist.contains(q, ignoreCase = true) ||
                it.album.contains(q, ignoreCase = true)
            }
        }

        var addedSongIdForFeedback by remember { mutableStateOf<Long?>(null) }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            // Header Playlist
            item(key = "header") {
                DetailHeader(
                    title = open.name,
                    subtitle = "${playlistSongs.size} lagu dalam playlist ini",
                    onBack = { vm.openPlaylistId = null },
                    onPlayAll = { vm.play(playlistSongs, 0) },
                    onShuffle = { vm.play(playlistSongs.shuffled(), 0) }
                )
            }

            // Daftar Lagu yang ada di dalam playlist
            if (playlistSongs.isEmpty()) {
                item(key = "empty_in_playlist") {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x12FFFFFF))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Belum ada lagu di playlist ini.\nTambahkan dari rekomendasi di bawah!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextDim,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                itemsIndexed(playlistSongs, key = { _, s -> "in_${s.id}" }) { index, s ->
                    SongRow(
                        song = s,
                        isCurrent = s.id == vm.currentSong?.id,
                        onClick = { vm.play(playlistSongs, index) },
                        onPlayNext = { vm.playNext(s) },
                        onAddQueue = { vm.addToQueue(s) },
                        onAddToPlaylist = { /* sudah ada di playlist ini */ },
                        onRemove = { vm.removeFromPlaylist(open.id, s.id) }
                    )
                }
            }

            // ==========================================================
            // FITUR REKOMENDASI LAGU / TAMBAHKAN KE PLAYLIST ALA SPOTIFY
            // ==========================================================
            item(key = "recommendation_header") {
                Spacer(Modifier.height(24.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    // Glass Card Header untuk Rekomendasi
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0x2B1ED760)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = LiquidGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Rekomendasi Lagu",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                if (unaddedSongs.isEmpty()) "Semua lagu sudah ditambahkan"
                                else "${unaddedSongs.size} lagu belum masuk ke playlist ini",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextDim
                            )
                        }
                    }

                    if (unaddedSongs.isNotEmpty() && unaddedSongs.size > 5) {
                        Spacer(Modifier.height(12.dp))
                        // Search bar mini untuk mencari lagu rekomendasi
                        OutlinedTextField(
                            value = addSearchQuery,
                            onValueChange = { addSearchQuery = it },
                            placeholder = { Text("Cari lagu untuk ditambahkan…", color = TextMuted, style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = LiquidGreen, modifier = Modifier.size(20.dp)) },
                            trailingIcon = {
                                if (addSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { addSearchQuery = "" }) {
                                        Icon(Icons.Filled.Close, contentDescription = "Hapus", tint = TextDim, modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = CircleShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0x1FFFFFFF),
                                unfocusedContainerColor = Color(0x12FFFFFF),
                                focusedBorderColor = LiquidGreen,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

            if (unaddedSongs.isEmpty()) {
                item(key = "all_songs_added") {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x141ED760))
                            .border(1.dp, LiquidGreen.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = LiquidGreen, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Semua lagu di perangkatmu sudah masuk ke playlist ini!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else if (filteredUnadded.isEmpty()) {
                item(key = "no_search_match") {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Tidak ada lagu rekomendasi yang cocok dengan \"$addSearchQuery\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextDim,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // List lagu yang belum masuk, dengan tombol ADD ala Spotify
                items(filteredUnadded, key = { "unadded_${it.id}" }) { s ->
                    val isJustAdded = addedSongIdForFeedback == s.id
                    val shape = RoundedCornerShape(16.dp)

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 3.dp)
                            .clip(shape)
                            .background(Color(0x0FFFFFFF))
                            .border(
                                1.dp,
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.12f),
                                        Color.Transparent
                                    )
                                ),
                                shape
                            )
                            .padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AlbumArt(s, Modifier.size(46.dp), corner = 12.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                s.title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "${s.artist} • ${formatTime(s.durationMs)}",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = TextMuted
                            )
                        }

                        // Tombol Tambah (+) ala Spotify
                        Button(
                            onClick = {
                                vm.addToPlaylist(open.id, s.id)
                                addedSongIdForFeedback = s.id
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isJustAdded) LiquidGreenLight else Color(0x2EFFFFFF),
                                contentColor = if (isJustAdded) Color(0xFF031408) else Color.White
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isJustAdded) LiquidGreen else Color.White.copy(alpha = 0.35f)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                if (isJustAdded) Icons.Filled.Check else Icons.Filled.Add,
                                contentDescription = "Tambah lagu",
                                modifier = Modifier.size(16.dp),
                                tint = if (isJustAdded) Color(0xFF031408) else LiquidGreen
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                if (isJustAdded) "Ditambahkan" else "Tambah",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog Pembuatan Playlist Baru
    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            containerColor = Color(0xEB131B15),
            modifier = Modifier.border(
                1.dp,
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.25f), LiquidGreen.copy(alpha = 0.2f))
                ),
                RoundedCornerShape(28.dp)
            ),
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    "Playlist Baru",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nama playlist", color = TextDim) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = LiquidGreen,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedContainerColor = Color(0x1AFFFFFF),
                        unfocusedContainerColor = Color(0x0DFFFFFF)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) vm.createPlaylist(newName.trim())
                        showCreate = false
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LiquidGreen,
                        contentColor = Color(0xFF031408)
                    )
                ) {
                    Text("Buat", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreate = false }) {
                    Text("Batal", color = TextDim)
                }
            }
        )
    }

    // Dialog Konfirmasi Hapus Playlist
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = Color(0xEB1C1315),
            modifier = Modifier.border(
                1.dp,
                Color(0xFFFF5252).copy(alpha = 0.35f),
                RoundedCornerShape(28.dp)
            ),
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    "Hapus Playlist?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    "Playlist \"${target.name}\" akan dihapus. File musik di perangkatmu tidak akan terhapus.",
                    color = TextDim,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.deletePlaylist(target.id)
                        deleteTarget = null
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5252),
                        contentColor = Color.White
                    )
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Batal", color = TextDim)
                }
            }
        )
    }
}
