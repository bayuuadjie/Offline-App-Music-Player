package com.example.offlineplayer.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.content.Context
import android.os.Build
import android.util.LruCache
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.offlineplayer.data.Song
import com.example.offlineplayer.viewmodel.PlayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Collections

// ------------------------------------------------------------------
// Cover album (dibaca dari file lokal, tanpa internet)
// ------------------------------------------------------------------
private val artCache = object : LruCache<Long, Bitmap>(24 * 1024 * 1024) {
    override fun sizeOf(key: Long, value: Bitmap): Int = value.byteCount
}
private val artFailed: MutableSet<Long> = Collections.synchronizedSet(HashSet())

private fun artKey(song: Song): Long = if (song.albumId > 0) song.albumId else -song.id

private fun loadArt(context: Context, song: Song): Bitmap? {
    val key = artKey(song)
    artCache.get(key)?.let { return it }
    if (key in artFailed) return null
    val bmp = try {
        if (Build.VERSION.SDK_INT >= 29) {
            context.contentResolver.loadThumbnail(song.uri, Size(400, 400), null)
        } else {
            context.contentResolver.openInputStream(song.artUri)?.use { BitmapFactory.decodeStream(it) }
        }
    } catch (e: Exception) {
        null
    }
    if (bmp != null) artCache.put(key, bmp) else artFailed.add(key)
    return bmp
}

@Composable
fun AlbumArt(song: Song?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bmp by produceState<Bitmap?>(null, song?.id) {
        value = song?.let { withContext(Dispatchers.IO) { loadArt(context, it) } }
    }
    Box(
        modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        val b = bmp
        if (b != null) {
            Image(
                bitmap = b.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(Icons.Filled.MusicNote, contentDescription = null, tint = Color.Gray)
        }
    }
}

fun formatTime(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    return "%d:%02d".format(total / 60, total % 60)
}

// ------------------------------------------------------------------
// Baris lagu
// ------------------------------------------------------------------
@Composable
fun SongRow(
    song: Song,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onPlayNext: () -> Unit,
    onAddQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onRemove: (() -> Unit)? = null
) {
    var menu by remember { mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlbumArt(song, Modifier.size(48.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                song.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isCurrent) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                "${song.artist} • ${formatTime(song.durationMs)}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box {
            IconButton(onClick = { menu = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(
                    text = { Text("Putar berikutnya") },
                    onClick = { menu = false; onPlayNext() }
                )
                DropdownMenuItem(
                    text = { Text("Tambah ke antrian") },
                    onClick = { menu = false; onAddQueue() }
                )
                DropdownMenuItem(
                    text = { Text("Tambah ke playlist") },
                    onClick = { menu = false; onAddToPlaylist() }
                )
                if (onRemove != null) {
                    DropdownMenuItem(
                        text = { Text("Hapus dari playlist") },
                        onClick = { menu = false; onRemove() }
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------------
// Daftar lagu yang dipakai di semua layar
// ------------------------------------------------------------------
@Composable
fun SongList(
    songs: List<Song>,
    vm: PlayerViewModel,
    modifier: Modifier = Modifier,
    onRemove: ((Song) -> Unit)? = null,
    header: (@Composable () -> Unit)? = null
) {
    var playlistTarget by remember { mutableStateOf<Song?>(null) }
    val currentId = vm.currentSong?.id

    LazyColumn(modifier.fillMaxSize()) {
        if (header != null) {
            item { header() }
        }
        itemsIndexed(songs, key = { _, s -> s.id }) { index, s ->
            SongRow(
                song = s,
                isCurrent = s.id == currentId,
                onClick = { vm.play(songs, index) },
                onPlayNext = { vm.playNext(s) },
                onAddQueue = { vm.addToQueue(s) },
                onAddToPlaylist = { playlistTarget = s },
                onRemove = if (onRemove != null) ({ onRemove(s) }) else null
            )
        }
    }

    playlistTarget?.let { target ->
        AddToPlaylistDialog(vm, target) { playlistTarget = null }
    }
}

@Composable
fun AddToPlaylistDialog(vm: PlayerViewModel, song: Song, onDismiss: () -> Unit) {
    var newName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah ke playlist") },
        text = {
            Column {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nama playlist baru") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(onClick = {
                    if (newName.isNotBlank()) {
                        vm.createPlaylist(newName.trim(), listOf(song.id))
                        onDismiss()
                    }
                }) { Text("Buat dan tambahkan") }

                LazyColumn(Modifier.heightIn(max = 240.dp)) {
                    items(vm.playlists, key = { it.id }) { p ->
                        Text(
                            p.name,
                            Modifier
                                .fillMaxWidth()
                                .clickable { vm.addToPlaylist(p.id, song.id); onDismiss() }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

// ------------------------------------------------------------------
// Header untuk detail folder / detail playlist
// ------------------------------------------------------------------
@Composable
fun DetailHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    extra: (@Composable () -> Unit)? = null
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
        IconButton(onClick = onBack) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
        }
        Column(Modifier.padding(horizontal = 8.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.size(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onPlayAll) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Putar semua")
                }
                OutlinedButton(onClick = onShuffle) {
                    Icon(Icons.Filled.Shuffle, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Acak")
                }
            }
            extra?.invoke()
        }
    }
}
