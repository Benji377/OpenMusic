package com.musicplayer.openmusic.data.base

import androidx.room.TypeConverter
import java.io.File

object FileConverter {
    @JvmStatic
    @TypeConverter
    fun fromFile(file: File): String {
        return file.absolutePath
    }

    @JvmStatic
    @TypeConverter
    fun fileFromString(path: String): File {
        return File(path)
    }
}