package com.musicplayer.SocyMusic.utils;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
}
