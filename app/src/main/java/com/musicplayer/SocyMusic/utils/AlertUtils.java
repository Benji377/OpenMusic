package com.musicplayer.SocyMusic.utils;

import android.app.AlertDialog;
import android.content.Context;
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

public class AlertUtils {
    public static void showAddToPlaylistDialog(Context context, Song songToAdd, OnNewPlaylistCallback newCallBack,
                                               OnPlaylistUpdateCallback updateCallBack) {
        SongsData songsData = SongsData.getInstance(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        AlertDialog.Builder selectPlaylistDialogBuilder = new AlertDialog.Builder(context);
        selectPlaylistDialogBuilder.setTitle(R.string.player_select_playlist);

        List<Playlist> playlists = songsData.getAllPlaylists();
        String[] playlistNames = new String[playlists.size() + 1];
        playlistNames[0] = context.getString(R.string.player_create_name_playlist);
        for (int i = 0; i < playlists.size(); i++)
            playlistNames[i + 1] = playlists.get(i).getName();

        selectPlaylistDialogBuilder.setItems(playlistNames, (dialogInterface, position) -> {
            if (position == 0) {
                ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.dialog_custom, null, false);
                EditText playlistNameEditText = new EditText(context);
                playlistNameEditText.setHint(R.string.new_playlist_enter_name);
                playlistNameEditText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                        , ViewGroup.LayoutParams.MATCH_PARENT));
                dialogView.addView(playlistNameEditText);

                AlertDialog.Builder newPlaylistDialogBuilder = new AlertDialog.Builder(context);
                newPlaylistDialogBuilder.setTitle(R.string.new_playlist);
                newPlaylistDialogBuilder.setView(dialogView);
                newPlaylistDialogBuilder.setPositiveButton(R.string.all_create, (dialogInterface1, i) -> {
                    String name = playlistNameEditText.getText().toString().trim();
                    if (name.isEmpty())
                        return;
                    Playlist newPlaylist = new Playlist(UUID.randomUUID(), name);
                    songsData.insertPlaylist(newPlaylist);
                    songsData.insertToPlaylist(newPlaylist, songToAdd);
                    newCallBack.onNewPlaylist();
                });
                newPlaylistDialogBuilder.setNegativeButton(android.R.string.cancel, (dialogInterface12, i) -> dialogInterface12.cancel());
                newPlaylistDialogBuilder.show();
            } else {
                Playlist playlistPicked = playlists.get(position - 1);
                if (playlistPicked.contains(songToAdd)) {
                    ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.dialog_custom, null, false);
                    TextView textView = new TextView(context);
                    textView.setText(R.string.playlist_dialog_add_anyway);
                    dialogView.addView(textView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                    new AlertDialog.Builder(context)
                            .setTitle(R.string.playlist_dialog_already_in_playlist)
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

    public interface OnNewPlaylistCallback {
        void onNewPlaylist();
    }

    public interface OnPlaylistUpdateCallback {
        void onPlaylistUpdate(Playlist playlist);
    }
}

