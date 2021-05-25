package com.example.SocyMusic.SocyMusic;

import android.widget.ArrayAdapter;

import java.io.File;
import java.util.ArrayList;
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

    public int size(){
        return this.songs.size();
    }

    public Vector<Song> getSongs(){
        return this.songs;
    }

    public Song at(int i){
        return this.songs.elementAt(i);
    }

    //Should only be here temporary
    public ArrayList<File> toArrayList(){
        ArrayList<File> files = new ArrayList<>();

        for (Song s : this.songs){
            files.add(s.getFile());
        }

        return files;
    }
}
