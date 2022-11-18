package com.musicplayer.OpenMusic.ui.album

import com.musicplayer.OpenMusic.ui.player_fragment_host.PlayerFragmentHost
import com.musicplayer.OpenMusic.data.Album
import android.os.Bundle
import com.musicplayer.musicplayer.R
import com.musicplayer.OpenMusic.data.Song
import android.content.Intent
import com.musicplayer.OpenMusic.data.Playlist

class AlbumActivity : PlayerFragmentHost(), AlbumFragment.Host {
    private var album: Album? = null
    private var albumFragment: AlbumFragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val childView = layoutInflater.inflate(
            R.layout.content_album,
            findViewById(R.id.layout_album_content), false
        )
        super.attachContentView(childView)
        album = intent.extras!!.getSerializable(EXTRA_ALBUM) as Album
        val showPlayer = intent.extras!!.getBoolean(EXTRA_SHOW_PLAYER)
        if (showPlayer) super.startPlayer(false)
        val fragmentManager = supportFragmentManager
        val fragment = fragmentManager.findFragmentById(R.id.layout_album_container)
        if (fragment == null) {
            albumFragment = AlbumFragment.newInstance(album!!)
            fragmentManager.beginTransaction()
                .add(R.id.layout_album_container, albumFragment!!)
                .commit()
        } else albumFragment = fragment as AlbumFragment
    }

    override fun onSongClick(song: Song) {
        super.onSongClick(song)
        val resultIntent = Intent()
        resultIntent.putExtra(RESULT_EXTRA_QUEUE_CHANGED, true)
        setResult(RESULT_OK, resultIntent)
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) super.unregisterMediaReceiver()
    }

    override fun onPlaylistUpdate(playlist: Playlist) {}
    override fun onNewPlaylist(newPlaylist: Playlist) {}

    companion object {
        const val EXTRA_ALBUM = "com.musicplayer.OpenMusic.ui.album.AlbumActivity.EXTRA_ALBUM"
        const val EXTRA_SHOW_PLAYER =
            "com.musicplayer.OpenMusic.ui.album.AlbumActivity.EXTRA_PLAYING"
        const val RESULT_EXTRA_QUEUE_CHANGED =
            "com.musicplayer.OpenMusic.ui.playlist.PlaylistActivity.EXTRA_PLAYING"
    }
}