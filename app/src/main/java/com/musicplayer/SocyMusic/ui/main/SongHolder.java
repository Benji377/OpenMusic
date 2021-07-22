package com.musicplayer.SocyMusic.ui.main;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.musicplayer.SocyMusic.Song;
import com.musicplayer.musicplayer.R;

public class SongHolder extends RecyclerView.ViewHolder {
    private TextView songTitleTextView;
    private SongListAdapter.ItemClickListener clickListener;

    public SongHolder(@NonNull View itemView, SongListAdapter.ItemClickListener clickListeners) {
        super(itemView);
        songTitleTextView = itemView.findViewById(R.id.textview_main_item_song_title);
        this.clickListener = clickListeners;
        itemView.setOnClickListener(v -> clickListeners.onItemClick(getBindingAdapterPosition(), v));
        itemView.setOnLongClickListener(v -> clickListeners.onItemLongClick(getBindingAdapterPosition(), v));
    }

    public void bind(Song song) {
        songTitleTextView.setText(song.getTitle());
    }

}