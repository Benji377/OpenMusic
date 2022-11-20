package com.musicplayer.openmusic.ui.album

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.musicplayer.openmusic.R
import com.musicplayer.openmusic.custom_views.CustomRecyclerView
import com.musicplayer.openmusic.data.Album
import com.musicplayer.openmusic.data.Song
import com.musicplayer.openmusic.data.SongsData
import com.musicplayer.openmusic.data.SongsData.Companion.getInstance

class AlbumFragment : Fragment() {
    private var album: Album? = null
    private var songsRecyclerView: CustomRecyclerView? = null
    private var songsData: SongsData? = null
    private var hostCallback: Host? = null
    private var albumTitleTextview: TextView? = null
    private var albumArtImageView: ImageView? = null
    private var songCountTextview: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                album = requireArguments().getSerializable(KEY_ALBUM, Album::class.java)
            } else {
                @Suppress("DEPRECATION")
                album = requireArguments().getSerializable(KEY_ALBUM) as Album
            }
        }
        songsData = getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_album, container, false)
        val toolbarLayout =
            view.findViewById<CollapsingToolbarLayout>(R.id.layout_album_collapsing_toolbar)
        toolbarLayout.title = album!!.title
        val appBarLayout = view.findViewById<AppBarLayout>(R.id.layout_album_appbar)
        val toolbarContent = view.findViewById<View>(R.id.layout_album_toolbar_content)
        appBarLayout.addOnOffsetChangedListener(object : OnOffsetChangedListener {
            var scrollRange = -1
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) scrollRange = appBarLayout.totalScrollRange
                val offsetFactor = (-verticalOffset).toFloat() / scrollRange.toFloat()
                toolbarContent.alpha = 1 - offsetFactor
                if (scrollRange + verticalOffset == 0) toolbarLayout.title =
                    album!!.title else toolbarLayout.title = ""
            }
        })
        albumTitleTextview = view.findViewById(R.id.textview_album_name)
        albumTitleTextview?.text = album!!.title
        albumArtImageView = view.findViewById(R.id.imageview_album_art)
        Glide.with(requireContext())
            .load(album!!.artPath)
            .placeholder(R.drawable.music_combined)
            .into(albumArtImageView!!)
        songCountTextview = view.findViewById(R.id.textview_album_song_count)
        songCountTextview?.text = requireContext().getString(
            R.string.all_song_count,
            album!!.songCount
        )
        val adapter = AlbumSongAdapter(requireContext(), album?.getSongList()!!)
        adapter.setOnItemClickListener(object : AlbumSongAdapter.ItemClickListener {
            override fun onItemClick(position: Int, view: View) {
                songsData!!.playAlbumFrom(album!!, position)
                hostCallback!!.onSongClick(album!!.getSongAt(position))
            }

            override fun onItemLongClick(position: Int, view: View): Boolean {
                return false
            }
        })
        songsRecyclerView = view.findViewById(R.id.recyclerview_album_songs)
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
            throw ClassCastException("$context must implement AlbumFragment.Host")
        }
    }

    internal interface Host {
        fun onSongClick(song: Song)
    }

    companion object {
        private const val KEY_ALBUM = "com.musicplayer.openmusic.ui.album.AlbumFragment.album"
        fun newInstance(album: Album): AlbumFragment {
            val args = Bundle()
            args.putSerializable(KEY_ALBUM, album)
            val fragment = AlbumFragment()
            fragment.arguments = args
            return fragment
        }
    }
}