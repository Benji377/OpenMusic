package com.musicplayer.openmusic.ui.playlists_tab

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.openmusic.data.Playlist
import com.musicplayer.musicplayer.R

class PlaylistsAdapter(private val context: Context, private var allPlaylist: List<Playlist>) :
    RecyclerView.Adapter<PlaylistHolder>() {
    private var clickListener: ItemClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = inflater.inflate(R.layout.list_item_playlist, parent, false)
        return PlaylistHolder(itemView, clickListener!!)
    }

    override fun onBindViewHolder(holder: PlaylistHolder, position: Int) {
        val playlist = allPlaylist[position]
        holder.bind(playlist)
    }

    override fun getItemCount(): Int {
        return allPlaylist.size
    }

    fun setOnItemClickListener(clickListener: ItemClickListener) {
        this.clickListener = clickListener
    }

    fun setAllPlaylist(allPlaylist: List<Playlist>) {
        this.allPlaylist = allPlaylist
    }

    interface ItemClickListener {
        fun onItemClick(position: Int, view: View)
        fun onItemLongClick(position: Int, view: View): Boolean
    }
}