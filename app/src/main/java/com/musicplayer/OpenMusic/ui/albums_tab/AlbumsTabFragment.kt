package com.musicplayer.OpenMusic.ui.albums_tab

import com.musicplayer.OpenMusic.data.SongsData.Companion.getInstance
import com.musicplayer.OpenMusic.custom_views.CustomRecyclerView
import com.musicplayer.OpenMusic.data.SongsData
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.musicplayer.musicplayer.R
import android.content.Intent
import android.app.Activity
import android.content.Context
import android.view.View
import com.musicplayer.OpenMusic.ui.album.AlbumActivity
import androidx.recyclerview.widget.GridLayoutManager
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import java.lang.ClassCastException

class AlbumsTabFragment : Fragment() {
    private var albumsRecyclerView: CustomRecyclerView? = null
    private var songsData: SongsData? = null
    private var hostCallback: Host? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        songsData = getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_albums_tab, container, false)
        albumsRecyclerView = view.findViewById(R.id.recyclerview_albums_tab_all)
        val adapter = AlbumsListAdapter(requireContext(), songsData!!.allAlbums!!)
        val launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult -> if (result.resultCode != Activity.RESULT_CANCELED) hostCallback!!.onQueueChanged() }
        adapter.setOnItemClickListener(object : AlbumsListAdapter.ItemClickListener {
            override fun onItemClick(position: Int, view: View) {
                val intent = Intent(requireContext(), AlbumActivity::class.java)
                intent.putExtra(AlbumActivity.EXTRA_ALBUM, songsData!!.allAlbums!![position])
                intent.putExtra(AlbumActivity.EXTRA_SHOW_PLAYER, hostCallback!!.isShowingPlayer)
                hostCallback!!.onSongListClick()
                launcher.launch(intent)
            }

            override fun onItemLongClick(position: Int, view: View): Boolean {
                return false
            }
        })
        albumsRecyclerView?.adapter = adapter
        albumsRecyclerView?.layoutManager = GridLayoutManager(requireContext(), 2)
        val emptyTextview = view.findViewById<TextView>(R.id.textview_albums_tab_empty)
        albumsRecyclerView?.setEmptyView(emptyTextview)
        return view
    }

    fun invalidateAlbumList() {
        albumsRecyclerView!!.adapter!!.notifyDataSetChanged()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            hostCallback = context as Host
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + "must implement AlbumsTabFragment.Host")
        }
    }

    interface Host {
        val isShowingPlayer: Boolean
        fun onSongListClick()
        fun onQueueChanged()
    }
}