package com.musicplayer.SocyMusic.data.base;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;

import com.musicplayer.SocyMusic.data.Playlist;
import com.musicplayer.SocyMusic.data.Song;

import java.util.UUID;

@Entity(tableName = "Playlist_Song", primaryKeys = {"playlist_id", "song_id", "song_index"}
        , foreignKeys = {@ForeignKey(entity = Playlist.class,
        parentColumns = "playlist_id",
        childColumns = "playlist_id",
        onDelete = ForeignKey.CASCADE)
        , @ForeignKey(entity = Song.class,
        parentColumns = "song_id",
        childColumns = "song_id",
        onDelete = ForeignKey.CASCADE)})
public class PlaylistSong {
    @NonNull
    @ColumnInfo(name = "playlist_id")
    public final UUID playlistID;
    @NonNull
    @ColumnInfo(name = "song_id")
    public final UUID songID;
    @ColumnInfo(name = "song_index")
    public int index;

    @Ignore
    public PlaylistSong(@NonNull UUID playlistID, @NonNull UUID songID) {
        this.playlistID = playlistID;
        this.songID = songID;
    }

    public PlaylistSong(@NonNull UUID playlistID, @NonNull UUID songID, int index) {
        this(playlistID, songID);
        this.index = index;
    }
}
