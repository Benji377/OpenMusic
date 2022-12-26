package com.musicplayer.openmusic.ui.album

import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.musicplayer.openmusic.R
import com.musicplayer.openmusic.data.Album
import com.musicplayer.openmusic.data.Playlist
import com.musicplayer.openmusic.data.Song
import com.musicplayer.openmusic.ui.player_fragment_host.PlayerFragmentHost

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            album = intent.extras!!.getSerializable(EXTRA_ALBUM, Album::class.java)
        } else {
            @Suppress("DEPRECATION")
            album = intent.extras!!.getSerializable(EXTRA_ALBUM) as Album
        }

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
        const val EXTRA_ALBUM = "com.musicplayer.openmusic.ui.album.AlbumActivity.EXTRA_ALBUM"
        const val EXTRA_SHOW_PLAYER =
            "com.musicplayer.openmusic.ui.album.AlbumActivity.EXTRA_PLAYING"
        const val RESULT_EXTRA_QUEUE_CHANGED =
            "com.musicplayer.openmusic.ui.playlist.PlaylistActivity.EXTRA_PLAYING"
    }
}