package com.musicplayer.OpenMusic.data.base;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.musicplayer.OpenMusic.data.Album;
import com.musicplayer.OpenMusic.data.Song;

import java.util.List;

@Dao
public interface AlbumDao {
    @Insert
    void insert(Album album);

    @Query("SELECT * FROM ALBUM")
    List<Album> getAll();

    @Query("SELECT * FROM Song WHERE Song.album_id=:albumId")
    List<Song> getSongs(String albumId);

    @Delete
    void delete(Album album);
}
