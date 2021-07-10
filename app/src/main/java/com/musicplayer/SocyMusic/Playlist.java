package com.musicplayer.SocyMusic;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.Console;

// STILL IN DEVELOPMENT!!
// Code from: https://stackoverflow.com/questions/3182937/android-create-playlist
public class Playlist {
    private int playlistId;

    public Playlist(int playlistId) {
        this.playlistId = playlistId;
    }

    public void createPlaylist(ContentResolver resolver, String pName) {
        Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.NAME, pName);
        Uri newPlaylistUri = resolver.insert(uri, values);
        Log.e("Create playlist", "newPlaylistUri:" + newPlaylistUri);
    }


    public void addToPlaylist(ContentResolver resolver, int audioId) {

        String[] cols = new String[] {
                "count(*)"
        };
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        Cursor cur = resolver.query(uri, cols, null, null, null);
        cur.moveToFirst();
        final int base = cur.getInt(0);
        cur.close();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + audioId);
        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioId);
        resolver.insert(uri, values);
    }

    public void removeFromPlaylist(ContentResolver resolver, int audioId) {
        Log.v("made it to add",""+audioId);
        String[] cols = new String[] {
                "count(*)"
        };
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        Cursor cur = resolver.query(uri, cols, null, null, null);
        cur.moveToFirst();
        final int base = cur.getInt(0);
        cur.close();
        ContentValues values = new ContentValues();

        resolver.delete(uri, MediaStore.Audio.Playlists.Members.AUDIO_ID +" = "+audioId, null);
    }
}
