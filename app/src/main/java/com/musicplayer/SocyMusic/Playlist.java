package com.musicplayer.SocyMusic;

/*
For devs, this is the M3U file structure, please follow this carefully to avoid issues:
- The first line defines the type of file
- The next line pairs are as follow:
    - The keyword #EXTINF: followed by the information about a song
    - The path to the song
Here is an example:

#EXTM3U

#EXTINF:111, Sample artist name - Sample track title
C:\Music\SampleMusic.mp3

#EXTINF:222,Example Artist name - Example track title
C:\Music\ExampleMusic.mp3

To know more about it, check out this link: https://docs.fileformat.com/audio/m3u/#extended-m3u
*/

import com.musicplayer.SocyMusic.Song;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class Playlist implements Serializable {
    private File file;
    private String name;
    private ArrayList<Song> songList;

    public Playlist(File file, String name) {
        this.file = file;
        this.name = name;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSongCount() {
        return songList.size();
    }
    public ArrayList<Song> getSongList() {
        return songList;
    }

    public void setSongList(ArrayList<Song> songList) {
        this.songList = songList;
    }
}
