package com.musicplayer.SocyMusic.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

@Entity
public class Album implements Serializable {
    @PrimaryKey
    @ColumnInfo(name = "album_id")
    @NonNull
    private UUID id;
    @ColumnInfo(name = "album_title")
    private String title;
    @ColumnInfo(name = "album_art_path")
    private String artPath;
    @Ignore
    private ArrayList<Song> songList;

    public Album(@NonNull UUID id, String title, String artPath) {
        this.id = id;
        this.title = title;
        this.artPath = artPath;
        songList = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getSongCount() {
        return songList == null ? 0 : songList.size();
    }

    public void addSong(Song song) {
        songList.add(song);
        song.setAlbum(this);
    }

    public String getArtPath() {
        return artPath;
    }

    public void setArtPath(String artPath) {
        this.artPath = artPath;
    }

    @NonNull
    public UUID getId() {
        return id;
    }

    public void setId(@NonNull UUID id) {
        this.id = id;
    }

    public void setSongList(ArrayList<Song> songList) {
        this.songList = songList;
        if (songList != null) {
            for (Song song : songList)
                song.setAlbum(this);
        }
    }

    public boolean containsSong(Song song) {
        return songList.contains(song);
    }

    public boolean isEmpty() {
        return songList.isEmpty();
    }

    public ArrayList<Song> getSongList() {
        return songList;
    }

    public Song getSongAt(int position) {
        return songList.get(position);
    }
}
