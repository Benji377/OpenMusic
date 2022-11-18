package com.musicplayer.OpenMusic.ui.player

import android.content.Context
import com.musicplayer.OpenMusic.data.Song
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import com.musicplayer.musicplayer.R

class SongPagerAdapter(private val context: Context, private var queue: List<Song>) :
    RecyclerView.Adapter<SongPageHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongPageHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = inflater.inflate(R.layout.pager_item_song, parent, false)
        return SongPageHolder(itemView)
    }

    override fun onBindViewHolder(holder: SongPageHolder, position: Int) {
        val song = queue[position]
        holder.bind(song)
    }

    override fun getItemCount(): Int {
        return queue.size
    }

    fun setQueue(queue: List<Song>) {
        this.queue = queue
    }
}