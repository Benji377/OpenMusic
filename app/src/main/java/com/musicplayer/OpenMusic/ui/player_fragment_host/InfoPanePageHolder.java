package com.musicplayer.OpenMusic.ui.player_fragment_host;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.musicplayer.OpenMusic.MediaPlayerUtil;
import com.musicplayer.OpenMusic.data.Album;
import com.musicplayer.OpenMusic.data.Song;
import com.musicplayer.musicplayer.R;

class InfoPanePageHolder extends RecyclerView.ViewHolder {
    private final TextView songTitleTextView;
    private final ImageView songAlbumArtImageView;
    private final Button playPauseButton;

    public InfoPanePageHolder(@NonNull View itemView, InfoPanePagerAdapter.PaneListeners listeners) {
        super(itemView);
        songTitleTextView = itemView.findViewById(R.id.textview_song_pane_item_title);
        songAlbumArtImageView = itemView.findViewById(R.id.imageview_song_pane_item_album_art);
        playPauseButton = itemView.findViewById(R.id.button_song_pane_item_play_pause);
        playPauseButton.setOnClickListener(v -> listeners.onPauseButtonClick());
        itemView.setOnClickListener(v -> listeners.onPaneClick());
    }

    public void bind(Song song) {
        songTitleTextView.setText(song.getTitle());
        playPauseButton.setBackgroundResource(MediaPlayerUtil.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        Album album = song.getAlbum();
        String artPath = album == null ? null : album.getArtPath();
        Glide.with(itemView.getContext())
                .load(artPath)
                .placeholder(R.drawable.music_combined)
                .into(songAlbumArtImageView);
    }

}