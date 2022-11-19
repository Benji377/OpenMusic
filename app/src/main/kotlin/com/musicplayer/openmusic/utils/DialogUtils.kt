package com.musicplayer.openmusic.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.musicplayer.musicplayer.R
import com.musicplayer.openmusic.data.Playlist
import com.musicplayer.openmusic.data.Song
import com.musicplayer.openmusic.data.SongsData
import java.util.*

object DialogUtils {
    @JvmStatic
    fun showAddToPlaylistDialog(
        context: Context, songToAdd: Song, newCallBack: OnNewPlaylistCallback,
        updateCallBack: OnPlaylistUpdateCallback
    ) {
        val songsData = SongsData.getInstance(context)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val selectPlaylistDialogBuilder = AlertDialog.Builder(context)
        selectPlaylistDialogBuilder.setTitle(R.string.add_to_playlist_dialog_title)
        val playlists = songsData?.allPlaylists
        val playlistNames = arrayOfNulls<String>(playlists!!.size + 1)
        playlistNames[0] = context.getString(R.string.add_to_playlist_dialog_new)
        for (i in playlists.indices) playlistNames[i + 1] = playlists[i].name
        selectPlaylistDialogBuilder.setItems(playlistNames) { _: DialogInterface?, position: Int ->
            if (position == 0) {
                showNewPlaylistDialog(context, object : OnNewPlaylistCallback {
                    override fun onNewPlaylist(newPlaylist: Playlist) {
                        songsData.insertToPlaylist(newPlaylist, songToAdd)
                        newCallBack.onNewPlaylist(newPlaylist)
                    }
                })
            } else {
                val playlistPicked = playlists[position - 1]
                if (playlistPicked.contains(songToAdd)) {
                    val dialogView =
                        inflater.inflate(R.layout.dialog_custom, null, false) as ViewGroup
                    val textView = TextView(context)
                    textView.setText(R.string.add_to_playlist_dialog_add_anyway)
                    dialogView.addView(
                        textView,
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    )
                    AlertDialog.Builder(context)
                        .setTitle(R.string.add_to_playlist_dialog_already_in_playlist)
                        .setView(dialogView)
                        .setPositiveButton(R.string.all_yes) { _: DialogInterface?, _: Int ->
                            songsData.insertToPlaylist(playlistPicked, songToAdd)
                            updateCallBack.onPlaylistUpdate(playlistPicked)
                        }
                        .setNegativeButton(R.string.all_no) { dialogInterface13: DialogInterface, _: Int -> dialogInterface13.cancel() }
                        .show()
                } else {
                    songsData.insertToPlaylist(playlistPicked, songToAdd)
                    updateCallBack.onPlaylistUpdate(playlistPicked)
                }
            }
        }
        selectPlaylistDialogBuilder.create().show()
    }

    @JvmStatic
    fun showNewPlaylistDialog(context: Context, newCallBack: OnNewPlaylistCallback) {
        val songsData = SongsData.getInstance(context)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_custom, null, false) as ViewGroup
        val playlistNameEditText = EditText(context)
        playlistNameEditText.setHint(R.string.new_playlist_dialog_enter_name)
        playlistNameEditText.setSingleLine()
        playlistNameEditText.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialogView.addView(playlistNameEditText)
        val newPlaylistDialogBuilder = AlertDialog.Builder(context)
        newPlaylistDialogBuilder.setTitle(R.string.new_playlist_dialog_title)
        newPlaylistDialogBuilder.setView(dialogView)
        newPlaylistDialogBuilder.setPositiveButton(R.string.all_create) { _: DialogInterface?, _: Int ->
            val name = playlistNameEditText.text.toString().trim { it <= ' ' }
            if (name.isEmpty()) return@setPositiveButton
            val newPlaylist = Playlist(UUID.randomUUID(), name)
            songsData?.insertPlaylist(newPlaylist)
            newCallBack.onNewPlaylist(newPlaylist)
        }
        newPlaylistDialogBuilder.setNegativeButton(android.R.string.cancel) { dialogInterface12: DialogInterface, _: Int -> dialogInterface12.cancel() }
        newPlaylistDialogBuilder.show()
    }

    @JvmStatic
    fun showDeletePlaylistDialog(context: Context, deleteCallBack: OnPlaylistDeleteCallback) {
        val songsData = SongsData.getInstance(context)
        val deletePlaylistDialogBuilder = AlertDialog.Builder(context)
        deletePlaylistDialogBuilder.setTitle(R.string.delete_playlist_dialog_title)
        val playlists = songsData?.allPlaylists
        val playlistNames = arrayOfNulls<String>(playlists!!.size)
        for (i in playlists.indices) playlistNames[i] = playlists[i].name
        deletePlaylistDialogBuilder.setItems(playlistNames) { _: DialogInterface?, position: Int ->
            AlertDialog.Builder(context)
                .setTitle(R.string.delete_playlist_dialog_confirm)
                .setPositiveButton(R.string.all_yes) { _: DialogInterface?, _: Int ->
                    songsData.deletePlaylist(position)
                    deleteCallBack.onPlaylistDelete(position)
                }
                .setNegativeButton(R.string.all_no) { dialogInterface13: DialogInterface, _: Int -> dialogInterface13.cancel() }
                .show()
        }
        deletePlaylistDialogBuilder.show()
    }

    interface OnNewPlaylistCallback {
        fun onNewPlaylist(newPlaylist: Playlist)
    }

    interface OnPlaylistUpdateCallback {
        fun onPlaylistUpdate(playlist: Playlist)
    }

    interface OnPlaylistDeleteCallback {
        fun onPlaylistDelete(index: Int)
    }
}