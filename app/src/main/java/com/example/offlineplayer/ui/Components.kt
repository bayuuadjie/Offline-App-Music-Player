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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.example.offlineplayer.data.Song
import com.example.offlineplayer.ui.theme.AppGradient
import com.example.offlineplayer.ui.theme.LiquidGreen
import com.example.offlineplayer.ui.theme.LiquidGreenLight
import com.example.offlineplayer.ui.theme.TextDim
import com.example.offlineplayer.ui.theme.TextMuted
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
    val shape = RoundedCornerShape(corner)
    Box(
        modifier
            .clip(shape)
            .background(Color(0x22FFFFFF))
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.28f),
                        Color(0xFF1ED760).copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.06f)
                    )
                ),
                shape
            ),
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
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF142E1F), Color(0xFF0C1911), Color(0xFF070F0A))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = LiquidGreen.copy(alpha = 0.85f),
                    modifier = Modifier.size(24.dp)
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
// Judul layar dan ubin ikon Liquid Glass
// ------------------------------------------------------------------
@Composable
fun ScreenTitle(
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            if (subtitle != null) {
                Spacer(Modifier.height(3.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDim
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun IconTile(icon: ImageVector, gradient: Boolean = false) {
    val shape = RoundedCornerShape(16.dp)
    val backgroundBrush = if (gradient) {
        AppGradient
    } else {
        Brush.linearGradient(
            listOf(Color(0x33FFFFFF), Color(0x1AFFFFFF))
        )
    }

    Box(
        Modifier
            .size(48.dp)
            .clip(shape)
            .background(backgroundBrush)
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.08f)
                    )
                ),
                shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (gradient) Color(0xFF031408) else LiquidGreen,
            modifier = Modifier.size(24.dp)
        )
    }
}

// ------------------------------------------------------------------
// Baris lagu (Tema Liquid Glass)
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
    val shape = RoundedCornerShape(18.dp)

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 3.dp)
            .clip(shape)
            .background(
                if (isCurrent) Color(0x241ED760) else Color(0x0AFFFFFF)
            )
            .border(
                1.dp,
                if (isCurrent) {
                    Brush.horizontalGradient(
                        listOf(
                            LiquidGreen.copy(alpha = 0.65f),
                            Color.White.copy(alpha = 0.25f)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                },
                shape
            )
            .clickable(onClick = onClick)
            .padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlbumArt(song, Modifier.size(48.dp), corner = 12.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                song.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isCurrent) LiquidGreenLight else Color.White,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${song.artist} • ${formatTime(song.durationMs)}",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isCurrent) Color.White.copy(alpha = 0.85f) else TextMuted
            )
        }
        Box {
            IconButton(onClick = { menu = true }) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = "Menu",
                    tint = TextDim
                )
            }
            DropdownMenu(
                expanded = menu,
                onDismissRequest = { menu = false },
                modifier = Modifier
                    .background(Color(0xE6141C16))
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.18f),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                DropdownMenuItem(
                    text = { Text("Putar berikutnya", color = Color.White) },
                    leadingIcon = { Icon(Icons.Filled.QueueMusic, contentDescription = null, tint = LiquidGreen) },
                    onClick = { menu = false; onPlayNext() }
                )
                DropdownMenuItem(
                    text = { Text("Tambah ke antrian", color = Color.White) },
                    leadingIcon = { Icon(Icons.Filled.PlaylistPlay, contentDescription = null, tint = LiquidGreen) },
                    onClick = { menu = false; onAddQueue() }
                )
                DropdownMenuItem(
                    text = { Text("Tambah ke playlist", color = Color.White) },
                    leadingIcon = { Icon(Icons.Filled.PlaylistAdd, contentDescription = null, tint = LiquidGreen) },
                    onClick = { menu = false; onAddToPlaylist() }
                )
                if (onRemove != null) {
                    DropdownMenuItem(
                        text = { Text("Hapus dari playlist", color = Color(0xFFFF5252)) },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = Color(0xFFFF5252)) },
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
    header: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null
) {
    var playlistTarget by remember { mutableStateOf<Song?>(null) }
    val currentId = vm.currentSong?.id

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 140.dp) // Ruang lega untuk floating player & dock
    ) {
        if (header != null) {
            item(key = "list_header") { header() }
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
        if (footer != null) {
            item(key = "list_footer") { footer() }
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
        containerColor = Color(0xEB131B15),
        modifier = Modifier.border(
            1.dp,
            Brush.linearGradient(
                listOf(Color.White.copy(alpha = 0.25f), Color(0xFF1ED760).copy(alpha = 0.2f))
            ),
            RoundedCornerShape(28.dp)
        ),
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                "Tambah ke playlist",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nama playlist baru", color = TextMuted) },
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
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            vm.createPlaylist(newName.trim(), listOf(song.id))
                            onDismiss()
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LiquidGreen,
                        contentColor = Color(0xFF031408)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Buat & Tambahkan", fontWeight = FontWeight.Bold)
                }

                if (vm.playlists.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Pilih playlist yang ada:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Spacer(Modifier.height(6.dp))
                    LazyColumn(Modifier.heightIn(max = 200.dp)) {
                        items(vm.playlists, key = { it.id }) { p ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x14FFFFFF))
                                    .clickable { vm.addToPlaylist(p.id, song.id); onDismiss() }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.LibraryMusic,
                                    contentDescription = null,
                                    tint = LiquidGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    p.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextDim)
            }
        }
    )
}

// ------------------------------------------------------------------
// Header untuk detail folder / detail playlist (Tema Liquid Glass)
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
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 14.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(start = 12.dp)
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
        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextDim
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Tombol Putar Semua (Liquid Green)
                Button(
                    onClick = onPlayAll,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LiquidGreen,
                        contentColor = Color(0xFF031408)
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Putar Semua", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }

                // Tombol Acak (Liquid Glass Frosted)
                OutlinedButton(
                    onClick = onShuffle,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(
                        1.dp,
                        Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.4f), Color.White.copy(alpha = 0.1f))
                        )
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0x24FFFFFF),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Filled.Shuffle, contentDescription = null, tint = LiquidGreen, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Acak", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (extra != null) {
                Spacer(Modifier.height(6.dp))
                extra()
            }
        }
    }
}
