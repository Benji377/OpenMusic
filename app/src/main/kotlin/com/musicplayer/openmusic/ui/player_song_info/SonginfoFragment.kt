package com.musicplayer.openmusic.ui.player_song_info

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.musicplayer.openmusic.MediaPlayerUtil.createTime
import com.musicplayer.openmusic.R
import com.musicplayer.openmusic.data.Song
import com.musicplayer.openmusic.data.SongsData
import com.musicplayer.openmusic.data.SongsData.Companion.getInstance

class SonginfoFragment : Fragment() {
    private var sthumbnail: ImageView? = null
    private var stitle: TextView? = null
    private var slength: TextView? = null
    private var sartists: TextView? = null
    private var salbums: TextView? = null
    private var splaylists: TextView? = null
    private var stags: TextView? = null
    private var currentSong = SongsData.data!!.songPlaying

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_songinfo, container, false)
        sthumbnail = view.findViewById(R.id.imageview_song_art)
        stitle = view.findViewById(R.id.textview_song_title)
        slength = view.findViewById(R.id.textview_song_length)
        sartists = view.findViewById(R.id.textview_song_artists)
        salbums = view.findViewById(R.id.textview_song_albums)
        splaylists = view.findViewById(R.id.textview_song_playlists)
        stags = view.findViewById(R.id.textview_song_tags)
        updateFields(null)
        return view
    }

    /**
     * A helper method to update all layout fields. This method can also be called from the outside
     *
     * @param song If the song is null, it will try to take the currently playing song,
     * if that is null too it gives an error.
     */
    @SuppressLint("DefaultLocale", "SetTextI18n")
    fun updateFields(song: Song?) {
        sthumbnail!!.setImageBitmap(song?.extractAlbumArt())
        stitle!!.text = String.format("Title: %s", song?.title)
        val duration = song?.extractDuration()
        val durations = createTime(duration!!)
        slength!!.text = String.format("Duration: %s", durations)
        sartists!!.text =
            String.format("Artists: %s", song.extractArtists())
        salbums!!.text =
            String.format("Album: %s", song.extractAlbumTitle())
        val playlists = getInstance(requireContext())!!.getPlaylistsOfSong(song)
        splaylists!!.text = String.format("Playlist(s): %s", playlists)
        stags!!.text = "Tags: Coming soon"
    }
}