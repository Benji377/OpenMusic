package com.musicplayer.openmusic.ui.playlist

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.musicplayer.openmusic.MediaPlayerUtil.createTime
import com.musicplayer.openmusic.R
import com.musicplayer.openmusic.custom_views.CustomRecyclerView
import com.musicplayer.openmusic.data.Playlist
import com.musicplayer.openmusic.data.Song
import com.musicplayer.openmusic.data.SongsData
import com.musicplayer.openmusic.data.SongsData.Companion.getInstance

class PlaylistFragment : Fragment() {
    private var playlist: Playlist? = null
    private var songsRecyclerView: CustomRecyclerView? = null
    private var songsData: SongsData? = null
    private var hostCallback: Host? = null
    private var playlistNameTextview: TextView? = null
    private var songCountTextview: TextView? = null
    private var totalDurationTextview: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                playlist = requireArguments().getSerializable(KEY_PLAYLIST, Playlist::class.java)
            } else {
                @Suppress("DEPRECATION")
                playlist = requireArguments().getSerializable(KEY_PLAYLIST) as Playlist
            }
        }
        songsData = getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_playlist, container, false)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_playlist)
        toolbar.setNavigationIcon(R.drawable.ic_playlist)
        val toolbarLayout =
            view.findViewById<CollapsingToolbarLayout>(R.id.layout_playlist_collapsing_toolbar)
        toolbarLayout.title = playlist!!.name
        val appBarLayout = view.findViewById<AppBarLayout>(R.id.layout_playlist_appbar)
        val toolbarContent = view.findViewById<View>(R.id.layout_playlist_toolbar_content)
        appBarLayout.addOnOffsetChangedListener(object : OnOffsetChangedListener {
            var scrollRange = -1
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.totalScrollRange
                }
                val offsetFactor = (-verticalOffset).toFloat() / scrollRange.toFloat()
                toolbarContent.alpha = 1 - offsetFactor
                if (scrollRange + verticalOffset == 0) {
                    toolbarLayout.title = playlist!!.name
                    toolbar.setNavigationIcon(R.drawable.ic_playlist)
                } else {
                    toolbarLayout.title = ""
                    toolbar.navigationIcon = null
                }
            }
        })
        playlistNameTextview = view.findViewById(R.id.textview_playlist_name)
        playlistNameTextview?.text = playlist!!.name
        songCountTextview = view.findViewById(R.id.textview_playlist_song_count)
        songCountTextview?.text = requireContext().getString(
            R.string.all_song_count,
            playlist!!.songCount
        )
        totalDurationTextview = view.findViewById(R.id.textview_playlist_total_duration)
        totalDurationTextview?.text = createTime(playlist!!.calculateTotalDuration())
        val adapter = PlaylistSongAdapter(requireContext(), playlist!!.songList)
        adapter.setOnItemClickListener(object : PlaylistSongAdapter.ItemClickListener {
            override fun onItemClick(position: Int, view: View) {
                songsData!!.playPlaylistFrom(playlist!!, position)
                hostCallback!!.onSongClick(playlist!!.getSongAt(position))
            }

            override fun onItemLongClick(position: Int, view: View): Boolean {
                return false
            }
        })
        songsRecyclerView = view.findViewById(R.id.recyclerview_playlist_songs)
        songsRecyclerView?.adapter = adapter
        songsRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
        //TODO: get this empty textview to work with the collapsing toolbar layout
//        TextView listEmptyTextview = view.findViewById(R.id.textview_playlist_empty);
//        songsRecyclerView.setEmptyView(listEmptyTextview);
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            hostCallback = context as Host
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement PlaylistFragment.Host")
        }
    }

    fun onPlaylistUpdate(newPlaylist: Playlist?) {
        playlist = newPlaylist
        (songsRecyclerView!!.adapter as PlaylistSongAdapter?)!!.setSongs(playlist!!.songList)
        songsRecyclerView!!.adapter!!.notifyDataSetChanged()
        songCountTextview!!.text =
            requireContext().getString(R.string.all_song_count, playlist!!.songCount)
        totalDurationTextview!!.text = createTime(playlist!!.calculateTotalDuration())
    }

    internal interface Host {
        fun onSongClick(song: Song)
    }

    companion object {
        private const val KEY_PLAYLIST =
            "com.musicplayer.openmusic.ui.playlist.PlaylistFragment.playlist"

        fun newInstance(playlist: Playlist): PlaylistFragment {
            val args = Bundle()
            args.putSerializable(KEY_PLAYLIST, playlist)
            val fragment = PlaylistFragment()
            fragment.arguments = args
            return fragment
        }
    }
}