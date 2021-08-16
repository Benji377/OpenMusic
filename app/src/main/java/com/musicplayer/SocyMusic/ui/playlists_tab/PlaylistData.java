//package com.musicplayer.SocyMusic.ui.playlists_tab;
//
//import android.content.Context;
//
//import com.musicplayer.SocyMusic.Playlist;
//import com.musicplayer.SocyMusic.Song;
//
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.ArrayList;
//
//public class PlaylistData {
//    public static PlaylistData data;
//    private ArrayList<Playlist> playlistList;
//    private final String playlistDir;
//
//
//    public PlaylistData(Context context) {
//        playlistDir = context.getFilesDir().getAbsolutePath();
//        reloadPlaylists();
//    }
//
//    public static PlaylistData getInstance(Context context) {
//        if (data == null) {
//            data = new PlaylistData(context);
//        }
//        return data;
//    }
//
//    public ArrayList<Playlist> getPlaylistList() {
//        return playlistList;
//    }
//
//    public void setPlaylistList(ArrayList<Playlist> playlistList) {
//        this.playlistList = playlistList;
//    }
//
//    public String getPlaylistDir() {
//        return playlistDir;
//    }
//
//    public boolean playlistExists(int position) {
//        try {
//            return playlistList.get(position).getFile().exists();
//        } catch (IndexOutOfBoundsException ex) {
//            return false;
//        }
//    }
//
//    public void reloadPlaylists() {
//        playlistList = new ArrayList<>();
//        File file = new File(playlistDir);
//        if (!file.exists() || !file.canRead())
//            return;
//        playlistList.addAll(loadPlaylists(file));
//    }
//
//    public ArrayList<Playlist> loadPlaylists(File dir) {
//        File[] files = dir.listFiles();
//        ArrayList<Playlist> playlistFound = new ArrayList<>();
//        if (files != null) {
//            for (File singleFile : files) {
//                if (singleFile.isDirectory() && !singleFile.isHidden()) {
//                    playlistFound.addAll(loadPlaylists(singleFile));
//                } else {
//                    if (singleFile.getName().endsWith(".m3u")) {
//                        playlistFound.add(new Playlist(singleFile));
//                    }
//                }
//            }
//        }
//        return playlistFound;
//    }
//
//    public Playlist createPlaylist(String playlistName) {
//        String pName = playlistName + ".m3u";
//        File file = new File(playlistDir, pName);
//        try {
//            if (file.createNewFile()) {
//                FileWriter writer = new FileWriter(file);
//                writer.write("#EXTM3U\n");
//                writer.write("\n");
//                writer.close();
//            } else {
//                // Playlist exists
//            }
//            return new Playlist(file, playlistName);
//        } catch (IOException e) {
//            // Error
//            return null;
//        }
//
//    }
//
//    public void addSong(Playlist playlist, Song song) {
//        try {
//            FileWriter writer = new FileWriter(playlist.getFile(), true);
//            writer.write("#EXTINF:" + song.getTitle() + "\n");
//            writer.write(song.getFile().getPath() + "\n");
//            writer.write("\n");
//            writer.close();
//            playlist.setSongCount(playlist.getSongCount() + 1);
//            playlist.getSongList().add(song);
//        } catch (IOException e) {
//            // Error
//        }
//    }
//
//    // Needs to do a copy of the current file, delete the content there ans replace the original file
//    public void removeSong(Playlist playlist, Song song) {
//        String currentLine;
//        File tempFile = new File(playlistDir, "temp_file.m3u");
//        File playlistFile = playlist.getFile();
//        BufferedReader reader;
//        BufferedWriter writer;
//        try {
//            reader = new BufferedReader(new FileReader(playlistFile));
//            writer = new BufferedWriter(new FileWriter(tempFile));
//            while ((currentLine = reader.readLine()) != null) {
//                if (currentLine.equals("#EXTINF:" + song.getFile().getName())) {
//                    // Skips the next two lines too
//                    reader.readLine();
//                    reader.readLine();
//                    continue;
//                }
//                writer.write(currentLine + System.getProperty("line.separator"));
//            }
//            writer.close();
//            reader.close();
//            playlistFile.delete();
//            if (tempFile.renameTo(playlistFile)) {
//                playlist.setSongCount(playlist.getSongCount() - 1);
//                ArrayList<Song> temp_list = playlist.getSongList();
//                temp_list.remove(song);
//                playlist.setSongList(temp_list);
//            } else {
//                // Error
//            }
//        } catch (IOException e) {
//            // Error
//        }
//    }
//
//    public void removePlaylist(Playlist playlist) {
//        File todelete = playlist.getFile();
//        if (todelete.delete()) {
//            // File deleted
//        } else {
//            // Error
//        }
//    }
//}
