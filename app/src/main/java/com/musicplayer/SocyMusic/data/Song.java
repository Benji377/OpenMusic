package com.musicplayer.SocyMusic.data;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.File;
import java.io.Serializable;
import java.util.UUID;

@Entity
public class Song implements Serializable {
    @PrimaryKey
    @ColumnInfo(name = "song_id")
    @NonNull
    private UUID songId;
    @ColumnInfo(name = "song_path")
    @NonNull
    private File file;
    @Ignore
    private final String title;

    /**
     * First custom constructor for the Song class. Creates a Song with file only.
     *
     * @param file   File to create a song from
     * @param songId
     */
    public Song(@NonNull UUID songId, File file) {
        // removes invalid characters from the filename before creating a song out of it
        this(songId, file, file.getName().replaceAll("(?<!^)[.][^.]*$", ""));

    }

    /**
     * Second custom constructor for the Song class. Creates a Song with file and title.
     *
     * @param songId
     * @param file   File to create a song from
     * @param title  Name of the song
     */
    @Ignore
    public Song(@NonNull UUID songId, @NonNull File file, String title) {
        this.songId = songId;
        this.file = file;
        this.title = title;
    }

    /**
     * Returns the song as a file
     *
     * @return The file of the song
     */
    @NonNull
    public File getFile() {
        return file;
    }

    /**
     * Sets the file of a song manually without constructor
     *
     * @param file The new file for the song
     */
    public void setFile(@NonNull File file) {
        this.file = file;
    }

    /**
     * Returns the title/name of the song
     *
     * @return name of the song
     */
    public String getTitle() {
        return title;
    }

    public String getPath() {
        return file.getAbsolutePath();
    }

    @Override
    public boolean equals(@Nullable @org.jetbrains.annotations.Nullable Object obj) {
        if (!(obj instanceof Song))
            return false;
        return ((Song) obj).getSongId().equals(this.getSongId());
    }

    public int getDuration() {
        return Integer.parseInt(getMetaDataReciever().extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
    }

    public UUID getSongId() {
        return songId;
    }

    public void setSongId(@NonNull UUID songId) {
        this.songId = songId;
    }

    public String getFolderName() {
        String[] folders = getPath().split("/");
        return folders[folders.length - 2];
    }

    public String getAlbumTitle() {
        return getMetaDataReciever().extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
    }

    public Bitmap getAlbumArt() {
        byte[] art = getMetaDataReciever().getEmbeddedPicture();
        if (art != null)
            return BitmapFactory.decodeByteArray(art, 0, art.length);
        return null;
    }

    private MediaMetadataRetriever getMetaDataReciever() {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(getPath());
        return metadataRetriever;
    }
}
