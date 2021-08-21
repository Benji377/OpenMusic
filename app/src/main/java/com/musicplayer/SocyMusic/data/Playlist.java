package com.musicplayer.SocyMusic.data;

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

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.musicplayer.SocyMusic.SocyMusicApp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

@Entity
public class Playlist implements Serializable {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "playlist_id")
    private final UUID id;
    @ColumnInfo(name = "playlist_name")
    private String name;
    @Ignore
    private ArrayList<Song> songList;

    public Playlist(@NonNull UUID id, String name) {
        this.id = id;
        this.name = name;
        songList = new ArrayList<>();
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

    @NonNull
    public UUID getId() {
        return id;
    }

    public boolean isFavorites() {
        return id.equals(SongsData.FAVORITES_PLAYLIST_ID);
    }

    public boolean contains(Song song) {
        return songList.contains(song);
    }

    public void addSong(Song song) {
        songList.add(song);
    }

    public void removeSong(Song song) {
        songList.remove(song);
    }
}
