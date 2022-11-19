package com.musicplayer.openmusic.ui.all_songs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.openmusic.data.Song
import com.musicplayer.musicplayer.R
import java.util.*

/**
 * Custom adapter for SongsData related actions
 */
class SongListAdapter(private val context: Context, private var allSongs: MutableList<Song>) :
    RecyclerView.Adapter<SongHolder>(), Filterable {
    private var clickListener: ItemClickListener? = null
    private var filteredList: MutableList<Song>?

    init {
        filteredList = allSongs
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = inflater.inflate(R.layout.list_item_all_songs, parent, false)
        return SongHolder(itemView, clickListener!!)
    }

    override fun onBindViewHolder(holder: SongHolder, position: Int) {
        val song = filteredList!![position]
        holder.bind(song)
    }

    override fun getItemCount(): Int {
        return if (filteredList == null) 0 else filteredList!!.size
    }

    fun setOnItemClickListener(clickListener: ItemClickListener) {
        this.clickListener = clickListener
    }

    fun setAllSongs(allSongs: MutableList<Song>) {
        this.allSongs = allSongs
    }

    // This filter will select the searched song and display it
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    filteredList = allSongs
                } else {
                    for (data in allSongs) {
                        if (data.title.lowercase(Locale.getDefault()).contains(
                                charSequence.toString().lowercase(
                                    Locale.getDefault()
                                )
                            )
                        ) {
                            filteredList!!.add(data)
                        }
                    }
                }
                val results = FilterResults()
                results.values = filteredList
                results.count = filteredList!!.size
                return results
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                filteredList = filterResults.values as MutableList<Song>
                notifyDataSetChanged()
            }
        }
    }

    interface ItemClickListener {
        fun onItemClick(position: Int, view: View)
        fun onItemLongClick(position: Int, view: View): Boolean
    }
}