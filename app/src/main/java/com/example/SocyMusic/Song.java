package com.example.SocyMusic;


import java.io.File;
import java.io.Serializable;

public class Song implements Serializable {
    private File songFile;
    private String songTitle;

    public Song(File file) {
        this(file, file.getName().replaceAll("(?<!^)[.][^.]*$", ""));
    }

    public Song(File file, String title) {
        this.songFile = file;
        this.songTitle = title;
    }

    public File getFile() {
        return songFile;
    }

    public void setSongFile(File songFile) {
        this.songFile = songFile;
    }

    public String getTitle() {
        return songTitle;
    }

}
