package com.example.offlineplayer.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.LruCache
import android.util.Size
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.example.offlineplayer.data.Song
import com.example.offlineplayer.ui.theme.AppGradient
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
fun AlbumArt(song: Song?, modifier: Modifier = Modifier, corner: Dp = 14.dp) {
    val context = LocalContext.current
    val bmp by produceState<Bitmap?>(null, song?.id) {
        value = song?.let { withContext(Dispatchers.IO) { loadArt(context, it) } }
    }
    Box(
        modifier
            .clip(RoundedCornerShape(corner))
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
            Box(Modifier.fillMaxSize().background(AppGradient), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.MusicNote, contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

fun formatTime(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    return "%d:%02d".format(total / 60, total % 60)
}

// ------------------------------------------------------------------
// Judul layar dan ubin ikon (dipakai di banyak layar)
// ------------------------------------------------------------------
@Composable
fun ScreenTitle(title: String, subtitle: String? = null, trailing: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = Color.White)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun IconTile(icon: ImageVector, gradient: Boolean = false) {
    val brush: Brush = if (gradient) AppGradient else SolidColor(MaterialTheme.colorScheme.surfaceVariant)
    Box(
        Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(brush),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon, contentDescription = null,
            tint = if (gradient) Color.White else MaterialTheme.colorScheme.primary
        )
    }
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
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isCurrent) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlbumArt(song, Modifier.size(52.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                song.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.White
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${song.artist} • ${formatTime(song.durationMs)}",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.White.copy(alpha = 0.75f)
            )
        }
        Box {
            IconButton(onClick = { menu = true }) {
                Icon(
                    Icons.Filled.MoreVert, contentDescription = "Menu",
                    tint = Color.White.copy(alpha = 0.75f)
                )
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

    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
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
        title = { Text("Tambah ke playlist", color = Color.White) },
        text = {
            Column {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nama playlist baru", color = Color.White.copy(alpha = 0.75f)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
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
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { vm.addToPlaylist(p.id, song.id); onDismiss() }
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
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
    Column(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp)) {
        IconButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp)) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
        }
        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(4.dp))
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.75f)
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onPlayAll, shape = CircleShape) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Putar semua")
                }
                OutlinedButton(onClick = onShuffle, shape = CircleShape) {
                    Icon(Icons.Filled.Shuffle, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Acak")
                }
            }
            if (extra != null) {
                Spacer(Modifier.height(4.dp))
                extra()
            }
        }
    }
}
