package com.example.offlineplayer.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MusicRepository(private val context: Context) {

    @Suppress("DEPRECATION")
    suspend fun loadSongs(): List<Song> = withContext(Dispatchers.IO) {
        val list = mutableListOf<Song>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_ADDED
        )
        val selection =
            "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > 1000"

        try {
            context.contentResolver.query(
                collection, projection, selection, null,
                "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
            )?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val albumIdCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val addedCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val path = c.getString(dataCol) ?: ""
                    val artist = c.getString(artistCol)
                    list += Song(
                        id = id,
                        title = c.getString(titleCol) ?: "Tanpa judul",
                        artist = if (artist.isNullOrBlank() || artist == "<unknown>")
                            "Artis tidak diketahui" else artist,
                        album = c.getString(albumCol) ?: "",
                        durationMs = c.getLong(durCol),
                        uri = ContentUris.withAppendedId(collection, id),
                        folderPath = File(path).parent ?: "",
                        albumId = c.getLong(albumIdCol),
                        dateAdded = c.getLong(addedCol)
                    )
                }
            }
        } catch (e: SecurityException) {
            // izin belum diberikan
        }
        list
    }
}
