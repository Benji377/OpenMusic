package com.musicplayer.OpenMusic.data.base

import androidx.room.Dao
import com.musicplayer.OpenMusic.data.Album
import com.musicplayer.OpenMusic.data.Song
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AlbumDao {
    @Insert
    fun insert(album: Album)

    @get:Query("SELECT * FROM ALBUM")
    val all: ArrayList<Album>

    @Query("SELECT * FROM Song WHERE Song.album_id=:albumId")
    fun getSongs(albumId: String): ArrayList<Song>

    @Delete
    fun delete(album: Album)
}