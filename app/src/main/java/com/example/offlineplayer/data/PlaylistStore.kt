package com.example.offlineplayer.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Menyimpan playlist buatan sendiri di penyimpanan internal aplikasi (JSON). Tanpa internet. */
class PlaylistStore(context: Context) {
    private val prefs = context.getSharedPreferences("playlists", Context.MODE_PRIVATE)

    fun load(): List<UserPlaylist> = try {
        val arr = JSONArray(prefs.getString("data", "[]"))
        List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            val ids = o.getJSONArray("songs")
            UserPlaylist(
                id = o.getLong("id"),
                name = o.getString("name"),
                songIds = List(ids.length()) { ids.getLong(it) }
            )
        }
    } catch (e: Exception) {
        emptyList()
    }

    fun save(list: List<UserPlaylist>) {
        val arr = JSONArray()
        list.forEach { p ->
            val ids = JSONArray()
            p.songIds.forEach { ids.put(it) }
            arr.put(
                JSONObject()
                    .put("id", p.id)
                    .put("name", p.name)
                    .put("songs", ids)
            )
        }
        prefs.edit().putString("data", arr.toString()).apply()
    }
}
