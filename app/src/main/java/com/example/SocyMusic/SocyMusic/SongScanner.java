package com.example.SocyMusic.SocyMusic;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

public class SongScanner {
    private Vector<Song> songs;

    /**
     * Scans the directory from this file recursively for song files
     * @param file      The directory to start in
     * @return          A Vector containing the songs
     */
    public static Vector<Song> scanFilesystem(File file){
        Vector<Song> songlist = new Vector<>();

        //Get a list of all files
        File[] files = file.listFiles();

        if (files != null){
            for (File curFile : files){
                //If the file is a directory, scan in too
                if (curFile.isDirectory() && !curFile.isHidden()){
                    songlist.addAll(scanFilesystem(curFile));
                }
                //Else parse the song!
                else{
                    //Check if it is a valid song
                    if (    curFile.getName().endsWith(".mp3") ||
                            curFile.getName().endsWith(".wav")){

                        songlist.add(new Song(curFile));

                    }
                }
            }
        }

        return songlist;
    }
}
