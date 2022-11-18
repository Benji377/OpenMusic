package com.musicplayer.OpenMusic.data

import android.media.MediaMetadataRetriever
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.*
import androidx.room.ForeignKey.Companion.SET_NULL
import java.io.File
import java.io.Serializable
import java.util.*

@Entity(
    foreignKeys = [ForeignKey(
        entity = Album::class,
        parentColumns = arrayOf("album_id"),
        childColumns = arrayOf("album_id"),
        onDelete = SET_NULL
    )]
)
class Song
/**
 * Second custom constructor for the Song class. Creates a Song with file and title.
 *
 * @param songId ID of the song
 * @param file   File to create a song from
 * @param title  Name of the song
 */ @Ignore constructor(
    @field:ColumnInfo(
        name = "song_id",
        index = true
    ) @field:PrimaryKey var songId: UUID,
    /**
     * Sets the file of a song manually without constructor
     *
     * @param file The new file for the song
     */
    @field:ColumnInfo(name = "song_path") var file: File,
    /**
     * Returns the title/name of the song
     *
     * @return name of the song
     */
    @field:Ignore val title: String
) : Serializable {

    /**
     * Returns the song as a file
     *
     * @return The file of the song
     */

    @ColumnInfo(name = "album_id", index = true)
    var albumID: UUID? = null

    @Ignore
    var album: Album? = null

    /**
     * First custom constructor for the Song class. Creates a Song with file only.
     *
     * @param file   File to create a song from
     * @param songId ID of the song
     */
    constructor(songId: UUID, file: File) : this(
        songId,
        file,
        file.name.replace("(?<!^)[.][^.]*$".toRegex(), "")
    ) {
        // removes invalid characters from the filename before creating a song out of it
    }

    val path: String
        get() = file.absolutePath

    override fun equals(other: Any?): Boolean {
        return if (other !is Song) false else other.songId == songId
    }

    fun extractDuration(): Int {
        return metaDataReciever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!
            .toInt()
    }

    val folderName: String
        get() {
            val folders = path.split("/").toTypedArray()
            return folders[folders.size - 2]
        }

    fun extractAlbumTitle(): String {
        return metaDataReciever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)!!
    }

    fun extractAlbumArt(): Bitmap {
        val art = metaDataReciever.embeddedPicture
        return BitmapFactory.decodeByteArray(art, 0, art!!.size)
    }

    fun extractArtists(): String {
        return metaDataReciever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)!!
    }

    override fun hashCode(): Int {
        var result = songId.hashCode()
        result = 31 * result + file.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + (albumID?.hashCode() ?: 0)
        result = 31 * result + (album?.hashCode() ?: 0)
        result = 31 * result + path.hashCode()
        result = 31 * result + folderName.hashCode()
        return result
    }

    private val metaDataReciever: MediaMetadataRetriever
        get() {
            val metadataRetriever = MediaMetadataRetriever()
            metadataRetriever.setDataSource(path)
            return metadataRetriever
        }
}