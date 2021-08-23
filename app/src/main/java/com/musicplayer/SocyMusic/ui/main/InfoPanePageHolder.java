package com.musicplayer.SocyMusic.ui.main;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.musicplayer.SocyMusic.MediaPlayerUtil;
import com.musicplayer.SocyMusic.data.Song;
import com.musicplayer.musicplayer.R;

class InfoPanePageHolder extends RecyclerView.ViewHolder {
    private final TextView songTitleTextView;
    private final Button playPauseButton;

    public InfoPanePageHolder(@NonNull View itemView, InfoPanePagerAdapter.PaneListeners listeners) {
        super(itemView);
        songTitleTextView = itemView.findViewById(R.id.textview_song_pane_item_title);
        playPauseButton = itemView.findViewById(R.id.button_song_pane_item_play_pause);
        playPauseButton.setOnClickListener(v -> listeners.onPauseButtonClick());
        itemView.setOnClickListener(v -> listeners.onPaneClick());
    }

    public void bind(Song song) {
        songTitleTextView.setText(song.getTitle());
        playPauseButton.setBackgroundResource(MediaPlayerUtil.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
    }

}