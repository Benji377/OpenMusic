package com.musicplayer.OpenMusic.data.base;

import androidx.room.TypeConverter;

import java.io.File;

public class FileConverter {

    @TypeConverter
    public static String fromFile(File file) {
        return file.getAbsolutePath();
    }

    @TypeConverter
    public static File fileFromString(String path) {
        return new File(path);
    }
}
