package com.musicplayer.OpenMusic.ui.album

import android.content.Context
import com.musicplayer.OpenMusic.data.Song
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.musicplayer.musicplayer.R

class AlbumSongAdapter(private val context: Context, private val albumSongs: List<Song>) :
    RecyclerView.Adapter<AlbumSongHolder>() {
    private var clickListener: ItemClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumSongHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = inflater.inflate(R.layout.list_item_album_song, parent, false)
        return AlbumSongHolder(itemView, clickListener!!)
    }

    override fun onBindViewHolder(holder: AlbumSongHolder, position: Int) {
        holder.bind(albumSongs[position])
    }

    override fun getItemCount(): Int {
        return albumSongs.size
    }

    fun setOnItemClickListener(itemClickListener: ItemClickListener) {
        clickListener = itemClickListener
    }

    interface ItemClickListener {
        fun onItemClick(position: Int, view: View)
        fun onItemLongClick(position: Int, view: View): Boolean
    }
}