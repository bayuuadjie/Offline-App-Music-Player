package com.example.offlineplayer.data

import android.content.ContentUris
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val uri: Uri,
    val folderPath: String,
    val albumId: Long,
    val dateAdded: Long
) {
    val folderName: String
        get() = folderPath.substringAfterLast('/').ifEmpty { "Penyimpanan" }

    val artUri: Uri
        get() = ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/albumart"), albumId
        )
}

data class UserPlaylist(
    val id: Long,
    val name: String,
    val songIds: List<Long>
)

fun Song.toMediaItem(): MediaItem =
    MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setArtworkUri(artUri)
                .build()
        )
        .build()
