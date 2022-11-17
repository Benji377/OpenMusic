package com.musicplayer.OpenMusic.utils;

import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;

import com.musicplayer.OpenMusic.data.Playlist;
import com.musicplayer.OpenMusic.data.Song;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;
import timber.log.Timber;

/**
 * Class to export and import
 * settings, playlistDB, etc...
 * WARNING! Is not accessible by the user right now because there is no
 * button or something for this yet.
 */
public class ImportExportUtils {

    // Use it to export and import files to a given path
    // Example: OpenMusic.sqlite3, preferences.xml, etc...
    // WARNING! Replaces existing files
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void exportImportFile(Path source, Path destination) {
        try {
            // create stream for `source`
            Stream<Path> files = Files.walk(source);

            // copy all files and folders from `source` to `destination`
            files.forEach(file -> {
                try {
                    Files.copy(file, destination.resolve(source.relativize(file)),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // close the stream
            files.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Extracts data from a given Playlist and writes it to a temporary file for later exportation
     * The data is written in m3u format!
     *
     * @param m3uFile       File to write data to
     * @param playlist_id   UUID of the playlist
     * @param playlist_name Name of the playlist
     * @return Filled file
     */
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
            Timber.e("M3U-Error: %s", e.getMessage());
        }
        return m3uFile;
    }

    /**
     * Converts a given playlist in M3U format and exports it to a file
     *
     * @param export_location Where the m3u file should be exported to
     * @param playlist_id     UUID of the playlist
     * @param playlist_name   Name of the playlist
     */
    public void exportM3UPlaylist(Path export_location, UUID playlist_id, String playlist_name) {
        File temporary = new File(Environment.getDataDirectory() + "/data/com.musicplayer.OpenMusic/files", "temporary.m3u");
        temporary = m3uConverter(temporary, playlist_id, playlist_name);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            exportImportFile(temporary.toPath(), export_location);
        } else {
            Timber.e("Version too low for export");
        }
        temporary.delete();
    }

    /**
     * Converts a file from xml to json and saves it in the destination file
     *
     * @param xmlFile  File to read from
     * @param jsonFile File to save XML in
     */
    public void convertXMLtoJSON(File xmlFile, File jsonFile) {
        try {
            InputStream inputStream = new FileInputStream(xmlFile);
            StringBuilder builder = new StringBuilder();
            int ptr;
            while ((ptr = inputStream.read()) != -1) {
                builder.append((char) ptr);
            }

            String xml = builder.toString();
            XmlToJson xmlToJson = new XmlToJson.Builder(xml).build();
            JSONObject jsonObj = xmlToJson.toJson();
            FileWriter fileWriter = new FileWriter(jsonFile);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            if (jsonObj != null) {
                for (int i = 0; i < jsonObj.toString().split(",").length; i++) {
                    //DEBUG: System.out.println(jsonObj.toString().split(",")[i]);
                    bufferedWriter.write(jsonObj.toString().split(",")[i]);
                    bufferedWriter.write("\n");
                }
            }

            bufferedWriter.close();
        } catch (IOException ex) {
            System.out.println("Error writing to file '" + jsonFile.getName() + "'");
        }
    }
}
