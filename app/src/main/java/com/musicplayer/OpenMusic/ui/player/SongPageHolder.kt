package com.musicplayer.OpenMusic.ui.player

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.musicplayer.musicplayer.R
import android.view.View.OnLayoutChangeListener
import android.widget.ImageView
import com.musicplayer.OpenMusic.data.Song
import com.bumptech.glide.Glide

class SongPageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val songTitleTextView: TextView
    private val songAlbumArtImageView: ImageView

    init {
        songTitleTextView = itemView.findViewById(R.id.textview_player_song_title)
        songAlbumArtImageView = itemView.findViewById(R.id.imageview_player_album_art)
        // This is necessary to fix the marquee, which was lagging sometimes
        songTitleTextView.isEnabled = true
        songTitleTextView.isSelected = true
        songTitleTextView.addOnLayoutChangeListener(object : OnLayoutChangeListener {
            // Manually sets the width and height of the TextView to fix the marquee issue
            override fun onLayoutChange(
                v: View, left: Int, top: Int, right: Int, bottom: Int,
                oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
            ) {
                val params = v.layoutParams
                params.width = right - left
                params.height = bottom - top
                v.removeOnLayoutChangeListener(this)
                v.layoutParams = params
            }
        })
    }

    fun bind(song: Song) {
        songTitleTextView.text = song.title
        val album = song.album
        val artPath = album?.artPath
        Glide.with(itemView.context)
            .load(artPath)
            .placeholder(R.drawable.music_combined)
            .into(songAlbumArtImageView)
    }
}