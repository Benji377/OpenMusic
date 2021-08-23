package com.musicplayer.SocyMusic.data.base;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

import java.util.UUID;

@Entity(tableName = "Playlist_Song", primaryKeys = {"playlist_id", "song_id", "song_index"})
public class PlaylistSong {
    @NonNull
    @ColumnInfo(name = "playlist_id")
    public UUID playlistID;
    @NonNull
    @ColumnInfo(name = "song_id")
    public String songID;
    @ColumnInfo(name = "song_index")
    public int index;

    @Ignore
    public PlaylistSong(@NonNull UUID playlistID, @NonNull String songID) {
        this.playlistID = playlistID;
        this.songID = songID;
    }

    public PlaylistSong(@NonNull UUID playlistID, @NonNull String songID, int index) {
        this(playlistID, songID);
        this.index = index;
    }
}
