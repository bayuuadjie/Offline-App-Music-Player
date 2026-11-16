package com.example.offlineplayer.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.offlineplayer.data.MusicRepository
import com.example.offlineplayer.data.PlaylistStore
import com.example.offlineplayer.data.Song
import com.example.offlineplayer.data.UserPlaylist
import com.example.offlineplayer.data.toMediaItem
import com.example.offlineplayer.player.PlaybackService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SortBy(val label: String) {
    TITLE("Judul"), ARTIST("Artis"), DURATION("Durasi"), NEWEST("Terbaru")
}

data class QueueItem(val index: Int, val mediaId: String, val title: String, val artist: String)

class PlayerViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = MusicRepository(app)
    private val store = PlaylistStore(app)
    private val prefs = app.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private var controller: MediaController? = null

    // ===================== DAFTAR LAGU =====================
    var songs by mutableStateOf<List<Song>>(emptyList()); private set
    var loading by mutableStateOf(true); private set
    private val songsById by derivedStateOf { songs.associateBy { it.id } }

    // ===================== PENCARIAN / FILTER / SORTIR =====================
    var query by mutableStateOf("")
    var artistFilter by mutableStateOf<String?>(null)
    var minOneMinute by mutableStateOf(false)
    var sortBy by mutableStateOf(
        try { SortBy.valueOf(prefs.getString("sort", "TITLE") ?: "TITLE") } catch (e: Exception) { SortBy.TITLE }
    ); private set

    val visibleSongs: List<Song> by derivedStateOf {
        val q = query.trim()
        val filtered = songs.filter { s ->
            (q.isEmpty() ||
                s.title.contains(q, ignoreCase = true) ||
                s.artist.contains(q, ignoreCase = true) ||
                s.album.contains(q, ignoreCase = true)) &&
                (artistFilter == null || s.artist == artistFilter) &&
                (!minOneMinute || s.durationMs >= 60_000)
        }
        when (sortBy) {
            SortBy.TITLE -> filtered.sortedBy { it.title.lowercase() }
            SortBy.ARTIST -> filtered.sortedBy { it.artist.lowercase() }
            SortBy.DURATION -> filtered.sortedBy { it.durationMs }
            SortBy.NEWEST -> filtered.sortedByDescending { it.dateAdded }
        }
    }

    fun changeSort(s: SortBy) {
        sortBy = s
        prefs.edit().putString("sort", s.name).apply()
    }

    // ===================== STATE PEMUTAR =====================
    var currentSong by mutableStateOf<Song?>(null); private set
    var currentIndex by mutableStateOf(0); private set
    var isPlaying by mutableStateOf(false); private set
    var shuffleOn by mutableStateOf(false); private set
    var repeatMode by mutableStateOf(Player.REPEAT_MODE_OFF); private set
    var positionMs by mutableStateOf(0L); private set
    var durationMs by mutableStateOf(0L); private set
    var queue by mutableStateOf<List<QueueItem>>(emptyList()); private set

    // ===================== SLEEP TIMER =====================
    var sleepEndAt by mutableStateOf(0L); private set          // 0 = tidak aktif
    var sleepAfterSong by mutableStateOf(false); private set
    private var sleepMediaId: String? = null

    // ===================== NAVIGASI FOLDER / PLAYLIST =====================
    var openFolder by mutableStateOf<String?>(null)
    var openPlaylistId by mutableStateOf<Long?>(null)
    var playlists by mutableStateOf(store.load()); private set

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            syncState(events.contains(Player.EVENT_TIMELINE_CHANGED))
        }
    }

    init {
        loadSongs()

        val token = SessionToken(app, ComponentName(app, PlaybackService::class.java))
        val future = MediaController.Builder(app, token).buildAsync()
        future.addListener({
            try {
                controller = future.get()
                controller?.addListener(listener)
                syncState(true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(app))

        viewModelScope.launch {
            while (true) {
                controller?.let { c ->
                    positionMs = c.currentPosition
                    val d = c.duration
                    if (d != C.TIME_UNSET && d != durationMs) durationMs = d
                }
                if (sleepEndAt > 0 && System.currentTimeMillis() > sleepEndAt + 5000) sleepEndAt = 0
                delay(500)
            }
        }
    }

    fun loadSongs() {
        viewModelScope.launch {
            loading = true
            songs = repo.loadSongs()
            loading = false
            syncState(true)
        }
    }

    private fun syncState(rebuildQueue: Boolean) {
        val c = controller ?: return
        isPlaying = c.isPlaying
        shuffleOn = c.shuffleModeEnabled
        repeatMode = c.repeatMode
        currentIndex = c.currentMediaItemIndex
        val mediaId = c.currentMediaItem?.mediaId
        currentSong = mediaId?.toLongOrNull()?.let { songsById[it] }
        val d = c.duration
        durationMs = if (d == C.TIME_UNSET) (currentSong?.durationMs ?: 0L) else d
        positionMs = c.currentPosition
        if (sleepAfterSong && mediaId != sleepMediaId) sleepAfterSong = false
        if (rebuildQueue) {
            queue = List(c.mediaItemCount) { i ->
                val m = c.getMediaItemAt(i)
                QueueItem(
                    index = i,
                    mediaId = m.mediaId,
                    title = m.mediaMetadata.title?.toString() ?: "",
                    artist = m.mediaMetadata.artist?.toString() ?: ""
                )
            }
        }
    }

    // ===================== KONTROL PEMUTAR =====================
    fun play(list: List<Song>, index: Int) {
        if (list.isEmpty()) return
        controller?.apply {
            setMediaItems(list.map { it.toMediaItem() }, index.coerceIn(0, list.lastIndex), 0L)
            prepare()
            play()
        }
    }

    fun togglePlay() {
        controller?.let {
            if (it.isPlaying) it.pause() else {
                if (it.playbackState == Player.STATE_ENDED) it.seekToDefaultPosition()
                it.play()
            }
        }
    }

    fun next() { controller?.seekToNext() }
    fun previous() { controller?.seekToPrevious() }
    fun seekTo(ms: Long) { controller?.seekTo(ms); positionMs = ms }

    fun toggleShuffle() {
        controller?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled }
    }

    fun cycleRepeat() {
        controller?.let {
            it.repeatMode = when (it.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
        }
    }

    // ===================== ANTRIAN =====================
    fun addToQueue(song: Song) {
        val c = controller ?: return
        if (c.mediaItemCount == 0) play(listOf(song), 0) else c.addMediaItem(song.toMediaItem())
    }

    fun playNext(song: Song) {
        val c = controller ?: return
        if (c.mediaItemCount == 0) play(listOf(song), 0)
        else c.addMediaItem(c.currentMediaItemIndex + 1, song.toMediaItem())
    }

    fun moveInQueue(from: Int, to: Int) {
        val c = controller ?: return
        if (to in 0 until c.mediaItemCount) c.moveMediaItem(from, to)
    }

    fun removeFromQueue(index: Int) { controller?.removeMediaItem(index) }

    fun jumpTo(index: Int) {
        controller?.apply {
            seekToDefaultPosition(index)
            play()
        }
    }

    // ===================== SLEEP TIMER =====================
    fun startSleepTimer(minutes: Int) {
        val c = controller ?: return
        c.sendCustomCommand(
            PlaybackService.CMD_SLEEP_START,
            Bundle().apply { putInt(PlaybackService.KEY_MINUTES, minutes) }
        )
        sleepAfterSong = false
        sleepEndAt = System.currentTimeMillis() + minutes * 60_000L
    }

    fun sleepAfterCurrentSong() {
        val c = controller ?: return
        c.sendCustomCommand(PlaybackService.CMD_SLEEP_END_OF_SONG, Bundle.EMPTY)
        sleepMediaId = c.currentMediaItem?.mediaId
        sleepEndAt = 0
        sleepAfterSong = true
    }

    fun cancelSleep() {
        controller?.sendCustomCommand(PlaybackService.CMD_SLEEP_CANCEL, Bundle.EMPTY)
        sleepEndAt = 0
        sleepAfterSong = false
    }

    // ===================== PLAYLIST BUATAN SENDIRI =====================
    fun createPlaylist(name: String, songIds: List<Long> = emptyList()) {
        playlists = playlists + UserPlaylist(System.currentTimeMillis(), name, songIds.distinct())
        store.save(playlists)
    }

    fun addToPlaylist(playlistId: Long, songId: Long) {
        playlists = playlists.map {
            if (it.id == playlistId && songId !in it.songIds) it.copy(songIds = it.songIds + songId) else it
        }
        store.save(playlists)
    }

    fun removeFromPlaylist(playlistId: Long, songId: Long) {
        playlists = playlists.map {
            if (it.id == playlistId) it.copy(songIds = it.songIds - songId) else it
        }
        store.save(playlists)
    }

    fun deletePlaylist(playlistId: Long) {
        playlists = playlists.filter { it.id != playlistId }
        if (openPlaylistId == playlistId) openPlaylistId = null
        store.save(playlists)
    }

    fun playlistSongs(p: UserPlaylist): List<Song> = p.songIds.mapNotNull { songsById[it] }

    override fun onCleared() {
        controller?.removeListener(listener)
        controller?.release()   // melepas koneksi saja; musik tetap jalan di service
        controller = null
        super.onCleared()
    }
}
