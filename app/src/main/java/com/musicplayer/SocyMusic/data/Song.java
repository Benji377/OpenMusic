package com.musicplayer.SocyMusic.data;


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
    @ColumnInfo(name = "song_path")
    @NonNull
    private File file;
    @ColumnInfo(name = "song_title")
    private final String title;

    /**
     * First custom constructor for the Song class. Creates a Song with file only.
     *
     * @param file File to create a song from
     */
    @Ignore
    public Song(File file) {
        // removes invalid characters from the filename before creating a song out of it
        this(file, file.getName().replaceAll("(?<!^)[.][^.]*$", ""));

    }

    /**
     * Second custom constructor for the Song class. Creates a Song with file and title.
     *
     * @param file  File to create a song from
     * @param title Name of the song
     */
    public Song(File file, String title) {
        this.file = file;
        this.title = title;
    }

    /**
     * Returns the song as a file
     *
     * @return The file of the song
     */
    public File getFile() {
        return file;
    }

    /**
     * Sets the file of a song manually without constructor
     *
     * @param file The new file for the song
     */
    public void setFile(File file) {
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
        return ((Song) obj).getPath().equals(this.getPath());
    }
}
