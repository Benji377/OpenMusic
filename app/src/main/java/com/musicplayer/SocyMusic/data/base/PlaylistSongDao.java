package com.musicplayer.SocyMusic.data.base;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;

@Dao
public interface PlaylistSongDao {
    @Insert
    void insert(PlaylistSong playlistSong);

    @Delete
    void delete(PlaylistSong playlistSong);
}
