package com.musicplayer.openmusic.ui.all_songs

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicplayer.openmusic.R
import com.musicplayer.openmusic.custom_views.CustomRecyclerView
import com.musicplayer.openmusic.data.Song
import com.musicplayer.openmusic.data.SongsData
import com.musicplayer.openmusic.data.SongsData.Companion.getInstance

class AllSongsFragment : Fragment() {
    private var songsRecyclerView: CustomRecyclerView? = null
    private var hostCallBack: Host? = null
    private var songsData: SongsData? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        songsData = getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_all_songs, container, false)
        songsRecyclerView = view.findViewById(R.id.recyclerview_all_songs)
        val customAdapter = SongListAdapter(
            requireContext(),
            songsData!!.getAllSongs() as MutableList<Song>
        )
        songsRecyclerView?.adapter = customAdapter
        songsRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
        val emptyText = view.findViewById<TextView>(R.id.textview_all_songs_list_empty)
        songsRecyclerView?.setEmptyView(emptyText)
        customAdapter.setOnItemClickListener(object : SongListAdapter.ItemClickListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onItemClick(position: Int, view: View) {
                // Error occured
                if (songsData!!.songExists(position)) {
                    Toast.makeText(
                        requireContext(),
                        getText(R.string.main_err_file_gone),
                        Toast.LENGTH_LONG
                    ).show()
                    try {
                        songsData!!.loadFromDatabase(requireContext()).join()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    customAdapter.notifyDataSetChanged()
                    return
                }
                songsData!!.playAllFrom(position)
                hostCallBack!!.onSongClick(songsData!!.getSongAt(position))
            }

            override fun onItemLongClick(position: Int, view: View): Boolean {
                return true
            }
        })
        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    fun invalidateSongList() {
        val adapter = songsRecyclerView!!.adapter as SongListAdapter
        adapter.setAllSongs(songsData!!.getAllSongs() as MutableList<Song>)
        adapter.notifyDataSetChanged()
    }

    /**
     * If the fragment is being attached to another activity
     *
     * @param context The context of the app
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            hostCallBack = context as Host
            // If implementation is missing
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement AllSongsFragment.Host")
        }
    }

    interface Host {
        fun onSongClick(songClicked: Song)
    }
}