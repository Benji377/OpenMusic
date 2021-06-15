package com.example.SocyMusic;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SongsData {
    public static SongsData data;
    private ArrayList<Song> allSongs;
    private ArrayList<Song> playingQueue;
    private int playingQueueIndex;
    private boolean repeat;

    private SongsData() {
        reloadSongs();
        playingQueue = new ArrayList<>();
    }

    public Song getSongPlaying() {
        return playingQueue.get(playingQueueIndex);
    }

    public Song playNext() {
        setPlaying(playingQueueIndex + 1);
        return getSongPlaying();
    }

    public Song playPrev() {
        setPlaying(playingQueueIndex - 1);
        return getSongPlaying();
    }

    public void setPlaying(int playingIndex) {
        playingQueueIndex = playingIndex;
        if (playingQueueIndex < 0 || playingQueueIndex > playingQueue.size() - 1 && repeat)
            playingQueueIndex = 0;
    }

    public void playAllFrom(int position) {
        playingQueue.clear();
        for (int i = 0; i < allSongs.size(); i++)
            playingQueue.add(allSongs.get((i + position) % allSongs.size()));

        playingQueueIndex = 0;
    }

    public void addToQueue(Song song) {
        playingQueue.add(song);
    }

    public void addToQueue(int position) {
        playingQueue.add(allSongs.get(position));
    }


    public static SongsData getInstance() {
        if (data == null)
            data = new SongsData();
        return data;
    }

    public void reloadSongs() {
        allSongs = loadSongs(Environment.getExternalStorageDirectory());
    }


    public ArrayList<Song> loadSongs(File dir) {
        File[] files = dir.listFiles();
        ArrayList<Song> songsFound = new ArrayList<>();
        if (files != null) {
            for (File singlefile : files) {
                if (singlefile.isDirectory() && !singlefile.isHidden()) {
                    songsFound.addAll(loadSongs(singlefile));
                } else {
                    if (singlefile.getName().endsWith(".mp3") || singlefile.getName().endsWith(".wav")) {
                        songsFound.add(new Song(singlefile));
                    }
                }
            }
        }

        return songsFound;
    }

    public void addSongs(File[] filedirs) {

        for (File filedir : filedirs) {
            allSongs.addAll(loadSongs(filedir));
        }
    }

    public int getPlayingQueueCount() {
        return playingQueue.size();
    }

    public boolean songExists(int position) {
        return allSongs.get(position).getFile().exists();
    }

    public Song getSongAt(int position) {
        return allSongs.get(position);
    }

    public Song getSongFromQueueAt(int position) {
        return playingQueue.get(position);
    }

    public int songsCount() {
        return allSongs.size();
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public boolean lastInQueue() {
        return playingQueueIndex == playingQueue.size() - 1;
    }

    public boolean firstInQueue() {
        return playingQueueIndex == 0;
    }

    public List<Song> getPlayingQueue() {
        return playingQueue;
    }

    public void onQueueReordered(int from, int to) {
        if (playingQueueIndex == from)
            playingQueueIndex = to;
        else {
            if (from < to && playingQueueIndex > from && playingQueueIndex <= to)
                playingQueueIndex--;
            else if (from > to && playingQueueIndex < from && playingQueueIndex >= to)
                playingQueueIndex++;
        }
    }
}
