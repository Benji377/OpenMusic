package com.musicplayer.OpenMusic.data.base

import androidx.room.Database
import com.musicplayer.OpenMusic.data.Song
import com.musicplayer.OpenMusic.data.Playlist
import com.musicplayer.OpenMusic.data.Album
import androidx.room.TypeConverters
import androidx.room.RoomDatabase

@Database(entities = [Song::class, Playlist::class, Album::class, PlaylistSong::class], version = 2)
@TypeConverters(FileConverter::class, UUIDConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistSongDao(): PlaylistSongDao
    abstract fun albumDao(): AlbumDao

    companion object {
        const val DATABASE_NAME = "OpenMusic.sqlite3"
    }
}