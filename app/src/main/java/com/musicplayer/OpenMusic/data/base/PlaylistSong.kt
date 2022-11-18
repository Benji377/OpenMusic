package com.musicplayer.OpenMusic.data.base

import com.musicplayer.OpenMusic.data.Playlist
import com.musicplayer.OpenMusic.data.Song
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Ignore
import java.util.*

@Entity(
    tableName = "Playlist_Song",
    primaryKeys = ["playlist_id", "song_id", "song_index"],
    foreignKeys = [ForeignKey(
        entity = Playlist::class,
        parentColumns = arrayOf("playlist_id"),
        childColumns = arrayOf("playlist_id"),
        onDelete = CASCADE
    ), ForeignKey(
        entity = Song::class,
        parentColumns = arrayOf("song_id"),
        childColumns = arrayOf("song_id"),
        onDelete = CASCADE
    )]
)
class PlaylistSong @Ignore constructor(
    @field:ColumnInfo(name = "playlist_id") val playlistID: UUID, @field:ColumnInfo(
        name = "song_id",
        index = true
    ) val songID: UUID
) {
    @JvmField
    @ColumnInfo(name = "song_index")
    var index = 0

    constructor(playlistID: UUID, songID: UUID, index: Int) : this(playlistID, songID) {
        this.index = index
    }
}