package com.musicplayer.openmusic.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity
class Album(
    @field:ColumnInfo(name = "album_id") @field:PrimaryKey var id: UUID, @field:ColumnInfo(
        name = "album_title"
    ) var title: String, @field:ColumnInfo(name = "album_art_path") var artPath: String?
) : Serializable {

    @Ignore
    private var songList: MutableList<Song>?

    init {
        songList = mutableListOf()
    }

    val songCount: Int
        get() = if (songList == null) 0 else songList!!.size

    fun addSong(song: Song) {
        songList!!.add(song)
        song.album = this
    }

    fun containsSong(song: Song): Boolean {
        return songList!!.contains(song)
    }

    val isEmpty: Boolean
        get() = songList!!.isEmpty()

    fun getSongList(): MutableList<Song>? {
        return songList
    }

    fun setSongList(songList: MutableList<Song>?) {
        this.songList = songList
        if (songList != null) {
            for (song in songList) song.album = this
        }
    }

    fun getSongAt(position: Int): Song {
        return songList!![position]
    }
}