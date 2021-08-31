package com.musicplayer.SocyMusic.data;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class Album {
    private String title;
    private Bitmap albumArt;
    private List<Song> songList;

    public Album(String title, Bitmap albumArt) {
        this.title = title;
        songList = new ArrayList<>();
        this.albumArt = albumArt;
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
    }

    public Bitmap getAlbumArt() {
        return albumArt;
    }
}
