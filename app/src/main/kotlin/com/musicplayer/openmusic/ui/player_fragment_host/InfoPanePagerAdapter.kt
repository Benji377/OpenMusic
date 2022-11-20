package com.musicplayer.openmusic.ui.player_fragment_host

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.openmusic.R
import com.musicplayer.openmusic.data.Song

class InfoPanePagerAdapter(private val context: Context, var queue: List<Song>) :
    RecyclerView.Adapter<InfoPanePageHolder>() {
    private var listeners: PaneListeners? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoPanePageHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = inflater.inflate(R.layout.pager_item_song_pane, parent, false)
        return InfoPanePageHolder(itemView, listeners!!)
    }

    override fun onBindViewHolder(holder: InfoPanePageHolder, position: Int) {
        holder.itemView.tag = position
        val song = queue[position]
        holder.bind(song)
    }

    override fun getItemCount(): Int {
        return queue.size
    }

    fun setPaneListeners(listeners: PaneListeners) {
        this.listeners = listeners
    }

    interface PaneListeners {
        fun onPaneClick()
        fun onPauseButtonClick()
    }
}