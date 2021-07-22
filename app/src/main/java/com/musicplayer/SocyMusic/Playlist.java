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
For devs, this is the M§U file structure, please follow this carefully to avoid issues:
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
