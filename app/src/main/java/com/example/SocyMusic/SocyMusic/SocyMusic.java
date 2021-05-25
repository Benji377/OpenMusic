package com.example.SocyMusic.SocyMusic;

import java.io.File;
import java.util.Vector;

public class SocyMusic {
    Vector<Song> songs;

    public SocyMusic(){
        songs = new Vector<>();
    }

    public SocyMusic(File f){
        this();
        this.songs = SongScanner.scanFilesystem(f);
    }

    public void scanForSongs(File f){
        this.songs = SongScanner.scanFilesystem(f);
    }
}
