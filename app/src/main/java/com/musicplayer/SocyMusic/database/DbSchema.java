package com.musicplayer.SocyMusic.database;

/*
 * This class (and its inner classes) defines the schema of the database. This will be used a lot
 * in SQLite queries. The inner classes each represent a table in the Schema, with NAME as the table
 *  name and each field in the inner inner class Cols storing a column name.
 */
public class DbSchema {
    public static final class SongTable {
        public static final String NAME = "Song";

        public static final class Cols {
            public static final String ID = "id";
            public static final String PATH = "path";
        }
    }

    public static final class PlaylistTable {
        public static final String NAME = "Playlist";

        public static final class Cols {
            public static final String ID = "id";
            public static final String NAME = "name";
        }
    }

    public static final class PlaylistSongTable {
        public static final String NAME = "Playlist_Song";

        public static final class Cols {
            public static final String PLAYLIST_ID = "playlist_id";
            public static final String SONG_ID = "song_id";
            public static final String INDEX = "index";

        }
    }
}
