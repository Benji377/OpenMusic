package com.example.SocyMusic;

import androidx.appcompat.app.AppCompatActivity;
import java.io.File;

// Class is not being used yet
public class Song extends AppCompatActivity {
    private File song;


    public String song_name() {
        return song.getName();
    }

    public File getSong() {
        return song;
    }
    public void setSong(File file) {
        this.song = file;
    }

    public Song(File file) {
        setSong(file);
    }

}
