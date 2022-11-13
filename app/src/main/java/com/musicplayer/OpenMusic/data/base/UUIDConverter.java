package com.musicplayer.OpenMusic.data.base;

import androidx.annotation.Nullable;
import androidx.room.TypeConverter;

import java.util.UUID;

public class UUIDConverter {

    @TypeConverter
    @Nullable
    public static String fromUUID(@Nullable UUID uuid) {
        return uuid == null ? null : uuid.toString();
    }

    @TypeConverter
    @Nullable
    public static UUID uuidFromString(@Nullable String string) {
        return string == null ? null : UUID.fromString(string);
    }
}
