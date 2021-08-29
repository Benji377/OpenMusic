package com.musicplayer.SocyMusic.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.musicplayer.SocyMusic.data.Playlist;
import com.musicplayer.SocyMusic.data.Song;
import com.musicplayer.SocyMusic.data.SongsData;
import com.musicplayer.musicplayer.R;

import java.util.List;
import java.util.UUID;

public class DialogUtils {
    public static void showAddToPlaylistDialog(Context context, Song songToAdd, OnNewPlaylistCallback newCallBack,
                                               OnPlaylistUpdateCallback updateCallBack) {
        SongsData songsData = SongsData.getInstance(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        AlertDialog.Builder selectPlaylistDialogBuilder = new AlertDialog.Builder(context);
        selectPlaylistDialogBuilder.setTitle(R.string.add_to_playlist_dialog_title);

        List<Playlist> playlists = songsData.getAllPlaylists();
        String[] playlistNames = new String[playlists.size() + 1];
        playlistNames[0] = context.getString(R.string.add_to_playlist_dialog_new);
        for (int i = 0; i < playlists.size(); i++)
            playlistNames[i + 1] = playlists.get(i).getName();

        selectPlaylistDialogBuilder.setItems(playlistNames, (dialogInterface, position) -> {
            if (position == 0) {
                showNewPlaylistDialog(context, newPlaylist -> {
                    songsData.insertToPlaylist(newPlaylist, songToAdd);
                    newCallBack.onNewPlaylist(newPlaylist);
                });

            } else {
                Playlist playlistPicked = playlists.get(position - 1);
                if (playlistPicked.contains(songToAdd)) {
                    ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.dialog_custom, null, false);
                    TextView textView = new TextView(context);
                    textView.setText(R.string.add_to_playlist_dialog_add_anyway);
                    dialogView.addView(textView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                    new AlertDialog.Builder(context)
                            .setTitle(R.string.add_to_playlist_dialog_already_in_playlist)
                            .setView(dialogView)
                            .setPositiveButton(R.string.all_yes, (dialogInterface13, i) -> {
                                songsData.insertToPlaylist(playlistPicked, songToAdd);
                                updateCallBack.onPlaylistUpdate(playlistPicked);
                            })
                            .setNegativeButton(R.string.all_no, (dialogInterface13, i) -> dialogInterface13.cancel())
                            .show();
                } else {
                    songsData.insertToPlaylist(playlistPicked, songToAdd);
                    updateCallBack.onPlaylistUpdate(playlistPicked);
                }
            }
        });
        selectPlaylistDialogBuilder.create().show();
    }

    public static void showNewPlaylistDialog(Context context, OnNewPlaylistCallback newCallBack) {
        SongsData songsData = SongsData.getInstance(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.dialog_custom, null, false);
        EditText playlistNameEditText = new EditText(context);
        playlistNameEditText.setHint(R.string.new_playlist_dialog_enter_name);
        playlistNameEditText.setSingleLine();
        playlistNameEditText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.MATCH_PARENT));
        dialogView.addView(playlistNameEditText);

        AlertDialog.Builder newPlaylistDialogBuilder = new AlertDialog.Builder(context);
        newPlaylistDialogBuilder.setTitle(R.string.new_playlist_dialog_title);
        newPlaylistDialogBuilder.setView(dialogView);
        newPlaylistDialogBuilder.setPositiveButton(R.string.all_create, (dialogInterface1, i) -> {
            String name = playlistNameEditText.getText().toString().trim();
            if (name.isEmpty())
                return;
            Playlist newPlaylist = new Playlist(UUID.randomUUID(), name);
            songsData.insertPlaylist(newPlaylist);
            newCallBack.onNewPlaylist(newPlaylist);
        });
        newPlaylistDialogBuilder.setNegativeButton(android.R.string.cancel, (dialogInterface12, i) -> dialogInterface12.cancel());
        newPlaylistDialogBuilder.show();
    }

    public static void showDeletePlaylistDialog(Context context, OnPlaylistDeleteCallback deleteCallBack) {
        SongsData songsData = SongsData.getInstance(context);
        AlertDialog.Builder deletePlaylistDialogBuilder = new AlertDialog.Builder(context);
        deletePlaylistDialogBuilder.setTitle(R.string.delete_playlist_dialog_title);
        List<Playlist> playlists = songsData.getAllPlaylists();
        String[] playlistNames = new String[playlists.size()];
        for (int i = 0; i < playlists.size(); i++)
            playlistNames[i] = playlists.get(i).getName();
        deletePlaylistDialogBuilder.setItems(playlistNames, (dialogInterface, position) -> new AlertDialog.Builder(context)
                .setTitle(R.string.delete_playlist_dialog_confirm)
                .setPositiveButton(R.string.all_yes, (dialogInterface13, i) -> {
                    songsData.deletePlaylist(position);
                    deleteCallBack.onPlaylistDelete(position);
                })
                .setNegativeButton(R.string.all_no, (dialogInterface13, i) -> dialogInterface13.cancel())
                .show());
        deletePlaylistDialogBuilder.show();
    }

    public interface OnNewPlaylistCallback {
        void onNewPlaylist(Playlist newPlaylist);
    }

    public interface OnPlaylistUpdateCallback {
        void onPlaylistUpdate(Playlist playlist);
    }

    public interface OnPlaylistDeleteCallback {
        void onPlaylistDelete(int index);
    }
}

