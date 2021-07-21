package com.musicplayer.SocyMusic;

import android.content.Context;
import android.util.Log;
import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

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
    // TODO: Implement playlist!
    File playlistFile;
    ArrayList<Song> songList;

    public Playlist(File file) {
        this.playlistFile = file;
    }
    public Playlist() {
        // Trying an empty constructor
    }

    public void createPlaylist(Context context, String filename) {
        // Doesn't create files for some strange reason
        String filename_complete = filename + ".m3u";
        try (FileOutputStream fos = context.openFileOutput(filename_complete, Context.MODE_PRIVATE)){
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("IOException", "File creation failed");
        }
    }

    public void addSong(Song songfile, String playlistname) {
        playlistname = playlistname + ".m3u";
        File f = new File(playlistname);
        if (f.exists() && !f.isDirectory()) {
            // Write data to M3U file
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(playlistname));
                writer.write(songfile.getFile().getPath() + "\n");
                writer.close();
            } catch(IOException e) {
                Log.e("Exceptionfile", "Filewriter issue");
            }
        } else {
            // Create the playlist file first and then add the song to it
            //createPlaylist(context, playlistname);
            //addSong(songfile, playlistname);
        }
    }

    public void removeSong(File songfile, String playlistname) throws IOException {
        playlistname = playlistname + ".m3u";
        File playlistfile = new File(playlistname);
        File temp = File.createTempFile("playlistfile", ".m3u", playlistfile.getParentFile());
        String delete = songfile.getPath();
        BufferedReader reader = new BufferedReader(new FileReader(playlistfile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
        String line;
        while((line = reader.readLine()) != null) {
            line = line.replace(delete, "");
            writer.write(line);
        }
        reader.close();
        writer.close();
        playlistfile.delete();
        temp.renameTo(playlistfile);
    }

    public void listSongs(String playlistName) {
        playlistName = playlistName + ".m3u";
        songList = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(playlistName));
            String line;
            while ((line = reader.readLine()) != null) {
                File songfile = new File(line);
                Song song = new Song(songfile);
                songList.add(song);
            }
            reader.close();
        } catch (IOException e) {
            Log.e("Exceptionfile", "Error reading playlistfile");
        }
    }
}
