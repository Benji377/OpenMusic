package com.musicplayer.openmusic.ui.all_songs

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.musicplayer.openmusic.data.Song
import com.musicplayer.musicplayer.R

class SongHolder(itemView: View, clickListeners: SongListAdapter.ItemClickListener) :
    RecyclerView.ViewHolder(itemView) {
    private val songTitleTextView: TextView
    private val songAlbumArtImageView: ImageView

    init {
        songTitleTextView = itemView.findViewById(R.id.textview_all_songs_item_song_title)
        songAlbumArtImageView = itemView.findViewById(R.id.imageview_all_songs_item_album_art)
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

    fun bind(song: Song) {
        songTitleTextView.text = song.title
        val album = song.album
        val artPath = album?.artPath
        Glide.with(itemView.context)
            .load(artPath)
            .placeholder(R.drawable.ic_music)
            .into(songAlbumArtImageView)
    }
}