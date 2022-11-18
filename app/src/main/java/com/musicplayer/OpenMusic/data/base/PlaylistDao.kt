package com.musicplayer.OpenMusic.data.base

import androidx.room.*
import com.musicplayer.OpenMusic.data.Playlist
import com.musicplayer.OpenMusic.data.Song

@Dao
interface PlaylistDao {
    @get:Query("SELECT * FROM Playlist")
    val all: ArrayList<Playlist>

    @Insert
    fun insert(playlist: Playlist)

    @Query("SELECT song_id,song_path,album_id FROM Song NATURAL JOIN Playlist_Song WHERE Playlist_Song.playlist_id = :playlist_id ORDER BY song_index")
    fun getSongs(playlist_id: String): ArrayList<Song>

    @Query("SELECT * FROM Playlist WHERE playlist_id=:playlist_id")
    operator fun get(playlist_id: String): Playlist

    @Update
    fun update(playlist: Playlist)

    @Delete
    fun delete(playlist: Playlist)
}