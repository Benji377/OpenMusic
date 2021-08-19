package com.musicplayer.SocyMusic.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class OpenHelper extends SQLiteOpenHelper {
    /**
     * The current version of the database. Update with each change to the Schema as defined in
     * DbSchema. Handle clashes in onUpgrade()
     */
    public static final int VERSION = 1;
    public static final String DATABASE_NAME = "socyMusic.db";

    public OpenHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }
}
