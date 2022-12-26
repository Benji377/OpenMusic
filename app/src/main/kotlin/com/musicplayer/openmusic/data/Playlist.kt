package com.musicplayer.openmusic.data

import android.media.MediaMetadataRetriever
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity
class Playlist : Serializable {
    @PrimaryKey
    @ColumnInfo(name = "playlist_id")
    val id: UUID

    @ColumnInfo(name = "playlist_name")
    var name: String

    @Ignore
    var songList: MutableList<Song>

    constructor(id: UUID, name: String, songList: MutableList<Song>) {
        this.id = id
        this.name = name
        this.songList = songList
    }

    constructor(id: UUID, name: String) {
        this.id = id
        this.name = name
        songList = mutableListOf()
    }

    val songCount: Int
        get() = songList.size

    fun getSongAt(index: Int): Song {
        return songList[index]
    }

    val isFavorites: Boolean
        get() = id == SongsData.FAVORITES_PLAYLIST_ID

    operator fun contains(song: Song): Boolean {
        return songList.contains(song)
    }

    fun addSong(song: Song) {
        songList.add(song)
    }

    fun removeSong(song: Song) {
        songList.remove(song)
    }

    fun removeSongAt(index: Int) {
        songList.removeAt(index)
    }

    fun calculateTotalDuration(): Int {
        val metadataRetriever = MediaMetadataRetriever()
        var totalDuration = 0
        for (song in songList) {
            metadataRetriever.setDataSource(song.path)
            totalDuration += metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!
                .toInt()
        }
        return totalDuration
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is Playlist) false else other.id == id
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + songList.hashCode()
        result = 31 * result + songCount
        result = 31 * result + isFavorites.hashCode()
        return result
    }
}