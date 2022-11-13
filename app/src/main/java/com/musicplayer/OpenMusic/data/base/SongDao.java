package com.musicplayer.OpenMusic.data.base;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.musicplayer.OpenMusic.data.Song;

import java.util.List;

@Dao
public interface SongDao {
    @Insert
    void insert(Song song);

    @Insert
    void insertAll(List<Song> song);

    @Query("SELECT * FROM Song ORDER BY song_path")
    List<Song> getAll();

    @Query("DELETE FROM Song")
    void clearAll();

    @Delete
    void delete(Song song);

    @Query("UPDATE Song SET album_id=:albumId WHERE song_id=:songID")
    void setAlbum(String songID, String albumId);
}
