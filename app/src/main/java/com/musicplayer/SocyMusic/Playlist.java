package com.musicplayer.SocyMusic;


import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Since the MediaStore.playlist is deprecated, it suggests to use M3U files instead.
// This is a very simple playlist class for now.
// m3u is a regular text file that can be read line by line. Just remember the lines that start with # are comments.
// TODO: Test this class, expand functionality, make PlaylistFragment and fragment_playlist work
public class Playlist {
    List<String> mp3;
    int next;


    public Playlist(File f){
        mp3=new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                addMP3(line);
            }
        } catch (IOException ex) {
            Log.e("FileReading", "Error reading the file");
        }
        next=-1;
    }

    private void addMP3(String line){
        if(line==null)return;
        if(!Character.isUpperCase(line.charAt(0)))return;
        if(line.indexOf(":\\")!=1)return;
        if(line.indexOf(".mp3", line.length()-4)==-1)return;
        mp3.add(line);
    }

    public String getNext(){
        next++;
        if(mp3.size()<=next)next=0;
        return mp3.get(next);
    }
}
