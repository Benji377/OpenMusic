package com.musicplayer.SocyMusic.data.base;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.musicplayer.SocyMusic.data.Playlist;
import com.musicplayer.SocyMusic.data.Song;

import java.util.List;

@Dao
public interface PlaylistDao {

    @Query("SELECT * FROM Playlist")
    List<Playlist> getAll();

    @Insert
    void insert(Playlist playlist);

    @Query("SELECT song_id,song_path FROM Song NATURAL JOIN Playlist_Song WHERE Playlist_Song.playlist_id = :playlist_id ORDER BY song_index")
    List<Song> getSongs(String playlist_id);

    @Query("SELECT * FROM Playlist WHERE playlist_id=:playlist_id")
    Playlist get(String playlist_id);

    @Update
    void update(Playlist playlist);

}
