package com.musicplayer.openmusic.data.base

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.musicplayer.openmusic.data.Album
import com.musicplayer.openmusic.data.Playlist
import com.musicplayer.openmusic.data.Song

@Database(entities = [Song::class, Playlist::class, Album::class, PlaylistSong::class], version = 2)
@TypeConverters(FileConverter::class, UUIDConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistSongDao(): PlaylistSongDao
    abstract fun albumDao(): AlbumDao

    companion object {
        const val DATABASE_NAME = "openmusic.sqlite3"
    }
}