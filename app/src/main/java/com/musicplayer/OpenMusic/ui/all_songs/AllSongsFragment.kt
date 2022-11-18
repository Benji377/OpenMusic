package com.musicplayer.OpenMusic.ui.all_songs

import com.musicplayer.OpenMusic.data.SongsData.Companion.getInstance
import com.musicplayer.OpenMusic.custom_views.CustomRecyclerView
import com.musicplayer.OpenMusic.data.SongsData
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.musicplayer.musicplayer.R
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.TextView
import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.musicplayer.OpenMusic.data.Song
import java.lang.ClassCastException

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
        val customAdapter = SongListAdapter(requireContext(),
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