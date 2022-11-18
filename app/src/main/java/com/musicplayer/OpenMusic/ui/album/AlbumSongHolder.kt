package com.musicplayer.OpenMusic.ui.album

import android.view.View
import com.musicplayer.OpenMusic.MediaPlayerUtil.createTime
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.musicplayer.musicplayer.R
import com.musicplayer.OpenMusic.data.Song

class AlbumSongHolder(itemView: View, clickListener: AlbumSongAdapter.ItemClickListener) :
    RecyclerView.ViewHolder(itemView) {
    private val songTitleTextView: TextView
    private val songLengthTextView: TextView

    init {
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
        songTitleTextView = itemView.findViewById(R.id.textview_album_song_item_title)
        songLengthTextView = itemView.findViewById(R.id.textview_album_song_item_length)
    }

    fun bind(song: Song) {
        songTitleTextView.text = song.title
        songLengthTextView.text = createTime(song.extractDuration())
    }
}