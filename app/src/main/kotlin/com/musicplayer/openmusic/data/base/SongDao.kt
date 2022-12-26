package com.musicplayer.openmusic.data.base

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.musicplayer.openmusic.data.Song

@Dao
interface SongDao {
    @Insert
    fun insert(song: Song)

    @Insert
    fun insertAll(song: MutableList<Song>)

    @get:Query("SELECT * FROM Song ORDER BY song_path")
    val all: MutableList<Song>

    @Query("DELETE FROM Song")
    fun clearAll()

    @Delete
    fun delete(song: Song)

    @Query("UPDATE Song SET album_id=:albumId WHERE song_id=:songID")
    fun setAlbum(songID: String, albumId: String)
}