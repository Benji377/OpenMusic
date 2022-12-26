package com.musicplayer.openmusic.ui.queue

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.core.content.res.ResourcesCompat
import com.gauravk.audiovisualizer.visualizer.BarVisualizer
import com.musicplayer.openmusic.MediaPlayerUtil.audioSessionId
import com.musicplayer.openmusic.R
import com.musicplayer.openmusic.data.Song
import com.musicplayer.openmusic.data.SongsData
import com.musicplayer.openmusic.data.SongsData.Companion.getInstance
import com.musicplayer.openmusic.ui.queue.ItemAdapter.OnItemClickedListener
import com.woxthebox.draglistview.DragItemAdapter

class ItemHolder(
    private val context: Context,
    itemView: View,
    @IdRes grabHandleID: Int,
    dragOnLongPress: Boolean,
    private val onItemClickedListener: OnItemClickedListener
) : DragItemAdapter.ViewHolder(itemView, grabHandleID, dragOnLongPress) {
    private val songsData: SongsData? = getInstance(context)
    private val songTitleTextView: TextView
    private val visualizer: BarVisualizer?
    private var song: Song? = null

    init {
        songTitleTextView = itemView.findViewById(R.id.textview_queue_item_song_title)
        visualizer = itemView.findViewById(R.id.bar_visualizer_queue_item)
    }

    fun bind(song: Song) {
        this.song = song

        // Gets the correct color from the theme -> Avoid hardcoding colors!
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(R.attr.colorOnPrimary, typedValue, true)
        @ColorInt val color = typedValue.data
        songTitleTextView.text = song.title
        if (bindingAdapterPosition < songsData!!.playingIndex) songTitleTextView.setTextColor(Color.GRAY) else songTitleTextView.setTextColor(
            color
        )
        visualizer!!.release()
        if (isPlaying) {
            visualizer.background = null
            val audioSessionID = audioSessionId
            if (audioSessionID != -1 && audioSessionID != 0) visualizer.setAudioSessionId(
                audioSessionID
            )
            visualizer.show()
        } else {
            visualizer.background =
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.ic_drag_handle,
                    context.theme
                )
            visualizer.hide()
        }
    }

    val isPlaying: Boolean
        get() = song != null && song!! == songsData!!.songPlaying

    override fun onItemClicked(view: View) {
        super.onItemClicked(view)
        onItemClickedListener.onItemClicked(bindingAdapterPosition)
        bind(songsData!!.songPlaying)
    }

    fun releaseVisualizer() {
        visualizer?.release()
    }
}