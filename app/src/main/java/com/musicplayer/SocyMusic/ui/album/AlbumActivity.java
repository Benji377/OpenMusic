package com.musicplayer.SocyMusic.ui.album;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.musicplayer.SocyMusic.data.Album;
import com.musicplayer.SocyMusic.data.Playlist;
import com.musicplayer.SocyMusic.data.Song;
import com.musicplayer.SocyMusic.ui.player_fragment_host.PlayerFragmentHost;
import com.musicplayer.musicplayer.R;

public class AlbumActivity extends PlayerFragmentHost implements AlbumFragment.Host {
    public static final String EXTRA_ALBUM = "com.musicplayer.SocyMusic.ui.album.AlbumActivity.EXTRA_ALBUM";
    public static final String EXTRA_SHOW_PLAYER = "com.musicplayer.SocyMusic.ui.album.AlbumActivity.EXTRA_PLAYING";
    public static final String RESULT_EXTRA_QUEUE_CHANGED = "com.musicplayer.SocyMusic.ui.playlist.PlaylistActivity.EXTRA_PLAYING";
    private Album album;
    private AlbumFragment albumFragment;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        View childView = getLayoutInflater().inflate(R.layout.content_album,
                findViewById(R.id.layout_album_content), false);
        super.attachContentView(childView);
        album = (Album) getIntent().getExtras().getSerializable(EXTRA_ALBUM);
        boolean showPlayer = getIntent().getExtras().getBoolean(EXTRA_SHOW_PLAYER);
        if (showPlayer)
            super.startPlayer(false);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.layout_album_container);
        if (fragment == null) {
            albumFragment = AlbumFragment.newInstance(album);
            fragmentManager.beginTransaction()
                    .add(R.id.layout_album_container, albumFragment)
                    .commit();
        } else
            albumFragment = (AlbumFragment) fragment;
    }

    @Override
    public void onSongClick(Song songClicked) {
        super.onSongClick(songClicked);
        Intent resultIntent = new Intent();
        resultIntent.putExtra(RESULT_EXTRA_QUEUE_CHANGED, true);
        setResult(Activity.RESULT_OK, resultIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing())
            super.unregisterMediaReceiver();
    }


    @Override
    public void onPlaylistUpdate(Playlist playlist) {

    }

    @Override
    public void onNewPlaylist(Playlist newPlaylist) {

    }
}
