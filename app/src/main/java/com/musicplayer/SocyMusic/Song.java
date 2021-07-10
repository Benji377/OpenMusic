package com.musicplayer.SocyMusic;


import java.io.File;
import java.io.Serializable;

public class Song implements Serializable {
    private File songFile;
    private String songTitle;

    /**
     * First custom constructor for the Song class. Creates a Song with file only.
     * @param file File to create a song from
     */
    public Song(File file) {
        // removes invalid characters from the filename before creating a song out of it
        this(file, file.getName().replaceAll("(?<!^)[.][^.]*$", ""));
    }

    /**
     * Second custom constructor for the Song class. Creates a Song with file and title.
     * @param file File to create a song from
     * @param title Name of the song
     */
    public Song(File file, String title) {
        this.songFile = file;
        this.songTitle = title;

    }

    /**
     * Returns the song as a file
     * @return The file of the song
     */
    public File getFile() {
        return songFile;
    }

    /**
     * Sets the file of a song manually without constructor
     * @param songFile The new file for the song
     */
    public void setSongFile(File songFile) {
        this.songFile = songFile;
    }

    /**
     * Returns the title/name of the song
     * @return name of the song
     */
    public String getTitle() {
        return songTitle;
    }

}
