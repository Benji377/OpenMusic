package com.musicplayer.openmusic.ui.playlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.openmusic.data.Song
import com.musicplayer.musicplayer.R

class PlaylistSongAdapter(private val context: Context, private var songs: List<Song>) :
    RecyclerView.Adapter<PlaylistSongHolder>() {
    private var clickListener: ItemClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistSongHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = inflater.inflate(R.layout.list_item_playlist_song, parent, false)
        return PlaylistSongHolder(itemView, clickListener!!)
    }

    override fun onBindViewHolder(holder: PlaylistSongHolder, position: Int) {
        holder.bind(songs[position])
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    fun setSongs(songs: List<Song>) {
        this.songs = songs
    }

    fun setOnItemClickListener(clickListener: ItemClickListener) {
        this.clickListener = clickListener
    }

    interface ItemClickListener {
        fun onItemClick(position: Int, view: View)
        fun onItemLongClick(position: Int, view: View): Boolean
    }
}