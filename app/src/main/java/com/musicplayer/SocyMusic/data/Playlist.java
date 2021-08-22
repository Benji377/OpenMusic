package com.musicplayer.SocyMusic.data;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

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

    public Playlist(@NonNull UUID id, String name, ArrayList<Song> songList) {
        this.id = id;
        this.name = name;
        this.songList = songList;
    }

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

    public int calculateTotalDuration() {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        int totalDuration = 0;
        for (Song song : songList) {
            metadataRetriever.setDataSource(song.getPath());
            totalDuration += Integer.parseInt(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        }
        return totalDuration;
    }

}
