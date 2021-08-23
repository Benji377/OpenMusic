package com.musicplayer.SocyMusic.data.base;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface PlaylistSongDao {
    @Insert
    void insert(PlaylistSong playlistSong);

    @Delete
    void delete(PlaylistSong playlistSong);

    @Query("DELETE FROM Playlist_Song WHERE song_id=:songId")
    void deleteSong(String songId);
}
