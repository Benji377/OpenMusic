package com.musicplayer.SocyMusic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
    Since I am struggling a lot with this, I'm just going to write down how it should work and try to
    figure it out later maybe.
    So a Playlist is generally a M3U file and an M3U file is almost like a text file which contains
    all the relative paths to the songs of a playlist.
    This means that we need a way for the user to create a playlist with a name. This will also be the
    name of the M3U file. Then when the user wants to add a song to a playlist, he can choose to create
    a new one, or add it to the existing one. The existing playlist will the add an entry to the M3U file
    with the location of the song file.

    Now there are several problems with this:
    1. Where do we store M3U files?
    2. If the user moves files, the path stored in the M3U file doesn't automatically change and
    therefore causes issues.
*/

public class Playlist {
    private String playlistName;
    private File playlistFile;
    List<String> songPathList;
    private int songCount;

    public String getPlaylistName() {
        return playlistName;
    }
    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }
    public File getPlaylistFile() {
        return playlistFile;
    }
    public void setPlaylistFile(File playlistFile) {
        this.playlistFile = playlistFile;
    }
    public List<String> getSongPathList() {
        return songPathList;
    }
    public void setSongPathList(List<String> songPathList) {
        this.songPathList = songPathList;
    }
    public int getSongCount() {
        return songCount;
    }

    public void setSongCount(int songCount) {
        this.songCount = songCount;
    }

    public Playlist(String playlistName) {
        File file = new File(getPlaylistName());
        try {
            if(file.createNewFile()) {
                System.out.println("File created at:" + file.getPath());
                FileWriter writer = new FileWriter(getPlaylistName());
                writer.write("#EXTM3U\n");
                writer.write("\n");
                writer.close();
                setPlaylistName(playlistName);
                setPlaylistFile(file);
                setSongCount(0);
                setSongPathList(new ArrayList<String>());
            } else {
                System.out.println("File already exists");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean addSong(File songFile) {
        try {
            FileWriter writer = new FileWriter(getPlaylistName(), true);
            writer.write("#EXTINF:"+songFile.getName() + "\n");
            writer.write(songFile.getPath() + "\n");
            writer.write("\n");
            writer.close();
            System.out.println("Wrote to file!");
            setSongCount(getSongCount()+1);
            getSongPathList().add(songFile.getPath());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeSong(File songFile) {
        String currentLine;
        File tempFile = new File("temp_file.m3u");
        File playlistFile = getPlaylistFile();
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new BufferedReader(new FileReader(playlistFile));
            writer = new BufferedWriter(new FileWriter(tempFile));
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.equals("#EXTINF:"+ songFile.getName())) {
                    // Skips the next two lines too
                    reader.readLine();
                    reader.readLine();
                    continue;
                }
                writer.write(currentLine + System.getProperty("line.separator"));
            }
            writer.close();
            reader.close();
            playlistFile.delete();
            if (tempFile.renameTo(playlistFile)) {
                setSongCount(getSongCount()-1);
                List<String> temp_list = getSongPathList();
                temp_list.removeIf(element -> element.contains(songFile.getPath()));
                setSongPathList(temp_list);
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
