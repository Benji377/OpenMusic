package com.musicplayer.SocyMusic.data.base;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.musicplayer.SocyMusic.data.Song;

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
}
