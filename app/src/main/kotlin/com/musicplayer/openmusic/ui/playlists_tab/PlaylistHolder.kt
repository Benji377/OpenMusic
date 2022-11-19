package com.musicplayer.openmusic.ui.playlists_tab

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.openmusic.data.Playlist
import com.musicplayer.musicplayer.R

class PlaylistHolder(itemView: View, clickListeners: PlaylistsAdapter.ItemClickListener) :
    RecyclerView.ViewHolder(itemView) {
    private val playlistTitleTextView: TextView
    private val songCountTextView: TextView
    private val clickListener: PlaylistsAdapter.ItemClickListener

    init {
        playlistTitleTextView = itemView.findViewById(R.id.textview_playlist_item_name)
        songCountTextView = itemView.findViewById(R.id.textview_playlist_item_songcount)
        clickListener = clickListeners
        itemView.setOnClickListener { v: View ->
            clickListeners.onItemClick(
                bindingAdapterPosition, v
            )
        }
        itemView.setOnLongClickListener { v: View ->
            clickListeners.onItemLongClick(
                bindingAdapterPosition, v
            )
        }
    }

    fun bind(playlist: Playlist) {
        playlistTitleTextView.text = playlist.name
        songCountTextView.text =
            itemView.context.getString(R.string.all_song_count, playlist.songCount)
    }
}