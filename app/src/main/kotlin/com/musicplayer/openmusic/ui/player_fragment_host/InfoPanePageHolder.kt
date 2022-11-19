package com.musicplayer.openmusic.ui.player_fragment_host

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.musicplayer.musicplayer.R
import com.musicplayer.openmusic.MediaPlayerUtil.isPlaying
import com.musicplayer.openmusic.data.Song
import com.musicplayer.openmusic.ui.player_fragment_host.InfoPanePagerAdapter.PaneListeners

class InfoPanePageHolder(itemView: View, listeners: PaneListeners) :
    RecyclerView.ViewHolder(itemView) {
    private val songTitleTextView: TextView
    private val songAlbumArtImageView: ImageView
    private val playPauseButton: Button

    init {
        songTitleTextView = itemView.findViewById(R.id.textview_song_pane_item_title)
        songAlbumArtImageView = itemView.findViewById(R.id.imageview_song_pane_item_album_art)
        playPauseButton = itemView.findViewById(R.id.button_song_pane_item_play_pause)
        playPauseButton.setOnClickListener { listeners.onPauseButtonClick() }
        itemView.setOnClickListener { listeners.onPaneClick() }
    }

    fun bind(song: Song) {
        songTitleTextView.text = song.title
        playPauseButton.setBackgroundResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
        val album = song.album
        val artPath = album?.artPath
        Glide.with(itemView.context)
            .load(artPath)
            .placeholder(R.drawable.music_combined)
            .into(songAlbumArtImageView)
    }
}