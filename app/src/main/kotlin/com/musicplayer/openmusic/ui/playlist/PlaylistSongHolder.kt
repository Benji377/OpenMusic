package com.musicplayer.openmusic.ui.playlist

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.openmusic.MediaPlayerUtil.createTime
import com.musicplayer.openmusic.data.Song
import com.musicplayer.musicplayer.R

class PlaylistSongHolder(itemView: View, clickListener: PlaylistSongAdapter.ItemClickListener) :
    RecyclerView.ViewHolder(itemView) {
    private val songTitleTextview: TextView
    private val songLengthTextview: TextView

    init {
        songTitleTextview = itemView.findViewById(R.id.textview_playlist_song_item_title)
        songLengthTextview = itemView.findViewById(R.id.textview_playlist_song_item_length)
        itemView.setOnClickListener { view: View ->
            clickListener.onItemClick(
                bindingAdapterPosition, view
            )
        }
        itemView.setOnLongClickListener { view: View ->
            clickListener.onItemLongClick(
                bindingAdapterPosition, view
            )
        }
    }

    fun bind(song: Song) {
        songTitleTextview.text = song.title
        songLengthTextview.text = createTime(song.extractDuration())
    }
}