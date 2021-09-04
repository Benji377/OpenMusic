package com.musicplayer.SocyMusic.data.base;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.musicplayer.SocyMusic.data.Album;
import com.musicplayer.SocyMusic.data.Playlist;
import com.musicplayer.SocyMusic.data.Song;

@Database(entities = {Song.class, Playlist.class, Album.class, PlaylistSong.class}, version = 2)
@TypeConverters({FileConverter.class, UUIDConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public static final String DATABASE_NAME = "socyMusic.sqlite3";

    public abstract SongDao songDao();

    public abstract PlaylistDao playlistDao();

    public abstract PlaylistSongDao playlistSongDao();

    public abstract AlbumDao albumDao();

}
