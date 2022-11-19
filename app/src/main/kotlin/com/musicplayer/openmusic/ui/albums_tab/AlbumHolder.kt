package com.musicplayer.openmusic.ui.albums_tab

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.musicplayer.openmusic.data.Album
import com.musicplayer.musicplayer.R

class AlbumHolder(itemView: View, clickListener: AlbumsListAdapter.ItemClickListener) :
    RecyclerView.ViewHolder(itemView) {
    private val albumTitleTextview: TextView
    private val songCountTextview: TextView
    private val albumArtImageView: ImageView

    init {
        albumTitleTextview = itemView.findViewById(R.id.textview_album_card_title)
        songCountTextview = itemView.findViewById(R.id.textview_album_card_song_count)
        albumArtImageView = itemView.findViewById(R.id.imageview_album_card_art)
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
        setIsRecyclable(false)
    }

    fun bind(album: Album) {
        albumTitleTextview.text = album.title
        songCountTextview.text =
            itemView.context.getString(R.string.all_song_count, album.songCount)
        Glide.with(itemView.context)
            .load(album.artPath)
            .placeholder(R.drawable.music_combined)
            .into(albumArtImageView)
    }
}