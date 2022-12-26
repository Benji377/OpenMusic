package com.musicplayer.openmusic.data.base

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.musicplayer.openmusic.data.Album
import com.musicplayer.openmusic.data.Song

@Dao
interface AlbumDao {
    @Insert
    fun insert(album: Album)

    @get:Query("SELECT * FROM ALBUM")
    val all: MutableList<Album>

    @Query("SELECT * FROM Song WHERE Song.album_id=:albumId")
    fun getSongs(albumId: String): MutableList<Song>

    @Delete
    fun delete(album: Album)
}