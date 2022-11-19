package com.musicplayer.openmusic.ui.albums_tab

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.openmusic.data.Album
import com.musicplayer.musicplayer.R

class AlbumsListAdapter(private val context: Context, private var albumList: List<Album>) :
    RecyclerView.Adapter<AlbumHolder>() {
    private var clickListener: ItemClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = inflater.inflate(R.layout.card_item_album, parent, false)
        return AlbumHolder(itemView, clickListener!!)
    }

    override fun onBindViewHolder(holder: AlbumHolder, position: Int) {
        holder.bind(albumList[position])
    }

    override fun getItemCount(): Int {
        return albumList.size
    }

    fun setAlbumList(albumList: List<Album>) {
        this.albumList = albumList
    }

    fun setOnItemClickListener(clickListener: ItemClickListener) {
        this.clickListener = clickListener
    }

    interface ItemClickListener {
        fun onItemClick(position: Int, view: View)
        fun onItemLongClick(position: Int, view: View): Boolean
    }
}