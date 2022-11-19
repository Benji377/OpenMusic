package com.musicplayer.openmusic.ui.queue

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicplayer.musicplayer.R
import com.musicplayer.openmusic.MediaPlayerUtil.playCurrent
import com.musicplayer.openmusic.data.SongsData
import com.musicplayer.openmusic.data.SongsData.Companion.getInstance
import com.musicplayer.openmusic.ui.queue.ItemAdapter.OnItemClickedListener
import com.woxthebox.draglistview.DragListView
import com.woxthebox.draglistview.DragListView.DragListListener

class QueueFragment : Fragment() {
    private var listView: DragListView? = null
    private var adapter: ItemAdapter? = null
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
        val view = inflater.inflate(R.layout.fragment_queue, container, false)
        listView = view.findViewById(R.id.listview_queue_songs)
        val layoutManager = LinearLayoutManager(context)
        listView?.setLayoutManager(layoutManager)
        adapter = ItemAdapter(
            requireContext(),
            songsData!!.getPlayingQueue(),
            R.layout.list_item_queue,
            R.id.bar_visualizer_queue_item,
            false
        )
        adapter!!.setOnItemClickListener(object : OnItemClickedListener {
            override fun onItemClicked(position: Int) {
                songsData!!.playingIndex = position
                playCurrent(requireContext())
                hostCallBack!!.onSongPlayingUpdate()
            }
        })
        listView?.setAdapter(adapter, false)
        listView?.setDragListListener(object : DragListListener {
            override fun onItemDragStarted(position: Int) {}
            override fun onItemDragging(itemPosition: Int, x: Float, y: Float) {}
            override fun onItemDragEnded(fromPosition: Int, toPosition: Int) {
                songsData!!.onQueueReordered(fromPosition, toPosition)
                hostCallBack!!.onQueueReordered()
                adapter!!.releasePlayingVisualizer()
                val playingIndex = songsData!!.playingIndex
                if (fromPosition <= playingIndex || toPosition <= playingIndex) {
                    adapter!!.notifyItemRangeChanged(0, playingIndex)
                }
            }
        })
        listView?.setCanDragHorizontally(false)
        layoutManager.scrollToPosition(songsData!!.playingIndex)
        return view
    }

    override fun onDestroyView() {
        adapter!!.releasePlayingVisualizer()
        super.onDestroyView()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            hostCallBack = context as Host
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement QueueFragment.Host")
        }
    }

    fun updateQueue() {
        adapter!!.notifyDataSetChanged()
    }

    interface Host {
        fun onSongPlayingUpdate()
        fun onQueueReordered()
    }
}