package com.example.SocyMusic.SocyMusic;

import java.io.File;

public class Song {
    File path;

    public Song(File f){
        this.path = f;
    }

    public String getName(){
        //Cut the file extension
        return this.path.getName().substring(0, this.path.getName().lastIndexOf('.'));
    }

    public String getFullName(){
        return this.path.getName();
    }

    public File getFile() {
        return path;
    }
}
