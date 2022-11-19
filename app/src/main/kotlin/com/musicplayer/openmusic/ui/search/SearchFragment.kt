package com.musicplayer.openmusic.ui.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicplayer.musicplayer.R
import com.musicplayer.openmusic.custom_views.CustomRecyclerView
import com.musicplayer.openmusic.data.Song
import com.musicplayer.openmusic.data.SongsData
import com.musicplayer.openmusic.ui.all_songs.SongListAdapter
import java.util.*
import java.util.stream.Collectors


class SearchFragment: Fragment() {
    private var songsData: SongsData? = null
    private var searchView: SearchView? = null
    private var recyclerView: CustomRecyclerView? = null
    private var list: List<String>? = null
    private var hostCallBack: Host? = null
    private var songListAdapter: SearchViewAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        songsData = SongsData.getInstance(requireContext());
        // Gets the list of songs, but we only need the title,
        // so thats what we are building the list with
        list = songsData!!.getAllSongs()?.stream().map(Song.title).collect(Collectors.toList())
        list = list?.stream()?.map(String::toLowerCase)?.collect(Collectors.toList())
        songListAdapter = SearchViewAdapter(requireContext(), songsData?.getAllSongs())

        songListAdapter?.setOnItemClickListener(object : SongListAdapter.ItemClickListener {
            override fun onItemClick(position: Int, view: View) {
                if (songsData.songExists(position)) {
                    Toast.makeText(requireContext(), getText(R.string.main_err_file_gone), Toast.LENGTH_LONG).show();
                    try {
                        songsData.loadFromDatabase(requireContext()).join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    songListAdapter.notifyDataSetChanged();
                    return;
                }
                songsData.playAllFrom(position);
                Timber.e("Song at %s = %s", position, songsData.getSongAt(position));
                hostCallBack.onSongClick(songsData.getSongAt(position));
            }

            override fun onItemLongClick(position: Int, view: View): Boolean {
                return false;
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_search, container, false)
        searchView = view.findViewById(R.id.search_view)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView?.setAdapter(songListAdapter)
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (list!!.contains(query.lowercase(Locale.getDefault()))) {
                    songListAdapter?.filter?.filter(query)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "No song found on " + songListAdapter.getItemCount(),
                        Toast.LENGTH_SHORT
                    ).show()
                    songListAdapter?.filter?.filter("")
                }
                songListAdapter?.setAllSongs(songsData?.getAllSongs() as MutableList<Song>)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                Toast.makeText(
                    requireContext(),
                    "Amount of songs: " + songListAdapter?.itemCount,
                    Toast.LENGTH_SHORT
                ).show()
                songListAdapter?.filter?.filter(newText)
                songListAdapter?.setAllSongs(songsData?.getAllSongs() as MutableList<Song>)
                return false
            }
        })
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            hostCallBack = context as Host
            // If implementation is missing
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement SearchFragment.Host")
        }
    }

    interface Host {
        fun onSongClick(songClicked: Song?)
    }

}