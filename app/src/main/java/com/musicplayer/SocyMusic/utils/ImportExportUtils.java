package com.musicplayer.SocyMusic.utils;

import com.musicplayer.SocyMusic.data.Playlist;
import com.musicplayer.SocyMusic.data.Song;

import android.content.Context;
import android.os.Environment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import timber.log.Timber;

/**
 * Class to export and import
 * settings, playlistDB, etc...
 */
public class ImportExportUtils {
    Context context;
    public String database_path = context.getFilesDir().getPath()+"/databases/socyMusic.sqlite3";

    public ImportExportUtils(Context context) {
        this.context = context;
    }

    // Exports the whole app database
    public void exportDB(File export_location) {
        try {
            File dbFile = new File(database_path);
            FileInputStream fis = new FileInputStream(dbFile);

            String outFileName = export_location + "socyMusic.db";

            // Open the empty db as the output stream
            OutputStream output = new FileOutputStream(outFileName);

            // Transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            // Close the streams
            output.flush();
            output.close();
            fis.close();

        } catch (IOException e) {
            Timber.e("dbBackup: %s", e.getMessage());
        }
    }

    public File m3uConverter(File m3uFile, UUID playlist_id, String playlist_name) {
        Playlist playlist = new Playlist(playlist_id, playlist_name);
        ArrayList<Song> songList = playlist.getSongList();
        try {
            FileWriter writer = new FileWriter(m3uFile);
            writer.write("#EXTM3U\n\n");
            for (Song s : songList) {
                writer.write("#EXTINF:");
                writer.write(songList.indexOf(s) + ", ");
                writer.write(s.getTitle() + "\n");
                writer.write(s.getPath());
                writer.write("\n\n");
            }
            writer.close();
        } catch (IOException e) {
            Timber.e("M3U-Error: %s",e.getMessage());
        }
        return m3uFile;
    }

    public void exportM3UPlaylist(File export_location, UUID playlist_id, String playlist_name) {
        File temporary = new File(Environment.getDataDirectory()+"/data/com.musicplayer.socymusic/files", "temporary.m3u");
        temporary = m3uConverter(temporary,playlist_id,playlist_name);
        try {
            FileInputStream input = new FileInputStream(temporary);
            OutputStream output = new FileOutputStream(export_location);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            output.flush();
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
