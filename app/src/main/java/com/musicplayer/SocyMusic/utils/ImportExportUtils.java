package com.musicplayer.SocyMusic.utils;

import com.musicplayer.SocyMusic.data.Playlist;
import com.musicplayer.SocyMusic.data.Song;
import com.musicplayer.musicplayer.BuildConfig;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import timber.log.Timber;

/**
 * Class to export and import
 * settings, playlistDB, etc...
 */
public class ImportExportUtils {
    public Context context;
    public String database_path = context.getFilesDir().getPath()+"/databases/socyMusic.sqlite3";
    private SharedPreferences _settings;

    public ImportExportUtils(Context context) {
        this.context = context;
    }

    public ImportExportUtils(SharedPreferences preferences) {
        this._settings = preferences;
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

    /**
     * Serialize all preferences into an output stream
     * @param os OutputStream to write to
     * @return True if successful
     */
    public boolean exportSettings(final @NonNull OutputStream os) {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(os);
            oos.writeObject(_settings.getAll());
            oos.close();
        } catch (IOException e) {
            Timber.e("Error serializing preferences %s", BuildConfig.DEBUG ? e : null);
            return false;
        }
        return true;
    }

    /**
     * Read all preferences from an input stream.
     * Schedules a full preference clean, then deserializes the options present in the given stream.
     * If the given object contains an unknown class, the deserialization is aborted and the underlying
     * preferences are not changed by this method
     * @param is Input stream to load the preferences from
     * @return True if the new values were successfully written to persistent storage
     * @throws IllegalArgumentException
     */
    public boolean importSettings(final @NonNull InputStream is) {
        ObjectInputStream ois;
        Map<String, Object> map;
        try {
            ois = new ObjectInputStream(is);
            map = (Map) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Timber.e("Error deserializing preferences %s", BuildConfig.DEBUG ? e : null);
            return false;
        }

        SharedPreferences.Editor editor = _settings.edit();
        editor.clear();

        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (e.getValue() instanceof Boolean) {
                editor.putBoolean(e.getKey(), (Boolean)e.getValue());
            } else if (e.getValue() instanceof String) {
                editor.putString(e.getKey(), (String)e.getValue());
            } else if (e.getValue() instanceof Integer) {
                editor.putInt(e.getKey(), (int)e.getValue());
            } else if (e.getValue() instanceof Float) {
                editor.putFloat(e.getKey(), (float)e.getValue());
            } else if (e.getValue() instanceof Long) {
                editor.putLong(e.getKey(), (Long) e.getValue());
            } else if (e.getValue() instanceof Set) {
                editor.putStringSet(e.getKey(), (Set<String>) e.getValue());
            } else {
                throw new IllegalArgumentException("Type " + e.getValue().getClass().getName() + " is unknown");
            }
        }
        return editor.commit();
    }


}
