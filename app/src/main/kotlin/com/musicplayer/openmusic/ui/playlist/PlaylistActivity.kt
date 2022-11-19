package com.musicplayer.openmusic.ui.playlist

import android.content.Intent
import android.os.Bundle
import com.musicplayer.openmusic.data.Playlist
import com.musicplayer.openmusic.data.Song
import com.musicplayer.openmusic.ui.player_fragment_host.PlayerFragmentHost
import com.musicplayer.musicplayer.R

class PlaylistActivity : PlayerFragmentHost(), PlaylistFragment.Host {
    private var playlistFragment: PlaylistFragment? = null
    private var playlist: Playlist? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val childView = layoutInflater.inflate(
            R.layout.content_playlist,
            findViewById(R.id.layout_playlist_content), false
        )
        super.attachContentView(childView)
        playlist = intent.extras!!.getSerializable(EXTRA_PLAYLIST) as Playlist?
        val showPlayer = intent.extras!!.getBoolean(EXTRA_SHOW_PLAYER)
        if (showPlayer) super.startPlayer(false)
        val fragmentManager = supportFragmentManager
        val fragment = fragmentManager.findFragmentById(R.id.layout_playlist_container)
        if (fragment == null) {
            playlistFragment = PlaylistFragment.newInstance(playlist!!)
            fragmentManager.beginTransaction()
                .add(R.id.layout_playlist_container, playlistFragment!!)
                .commit()
        } else playlistFragment = fragment as PlaylistFragment?
    }

    override fun onSongClick(song: Song) {
        super.onSongClick(song)
        val resultIntent = Intent()
        resultIntent.putExtra(RESULT_EXTRA_QUEUE_CHANGED, true)
        setResult(RESULT_OK, resultIntent)
    }

    override fun onPlaylistUpdate(playlist: Playlist) {
        if (this.playlist!! == playlist && playlistFragment != null) {
            this.playlist = playlist
            playlistFragment!!.onPlaylistUpdate(playlist)
        }
    }

    override fun onNewPlaylist(newPlaylist: Playlist) {}
    override fun onPause() {
        super.onPause()
        if (isFinishing) super.unregisterMediaReceiver()
    }

    companion object {
        const val EXTRA_PLAYLIST =
            "com.musicplayer.openmusic.ui.playlist.PlaylistActivity.EXTRA_PLAYLIST"
        const val EXTRA_SHOW_PLAYER =
            "com.musicplayer.openmusic.ui.playlist.PlaylistActivity.EXTRA_PLAYING"
        const val RESULT_EXTRA_QUEUE_CHANGED =
            "com.musicplayer.openmusic.ui.playlist.PlaylistActivity.EXTRA_PLAYING"
    }
}