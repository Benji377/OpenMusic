package com.musicplayer.SocyMusic.ui.playlist;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.musicplayer.SocyMusic.MediaPlayerUtil;
import com.musicplayer.SocyMusic.data.Song;
import com.musicplayer.musicplayer.R;

public class PlaylistSongHolder extends RecyclerView.ViewHolder {
    private TextView songTitleTextview;
    private TextView songLengthTextview;

    public PlaylistSongHolder(@NonNull View itemView, PlaylistSongAdapter.ItemClickListener clickListener) {
        super(itemView);
        songTitleTextview = itemView.findViewById(R.id.textview_playlist_song_item_title);
        songLengthTextview = itemView.findViewById(R.id.textview_playlist_song_item_length);
        itemView.setOnClickListener(view -> clickListener.onItemClick(getBindingAdapterPosition(), view));
        itemView.setOnLongClickListener(view -> clickListener.onItemLongClick(getBindingAdapterPosition(), view));
    }

    public void bind(Song song) {
        songTitleTextview.setText(song.getTitle());
        songLengthTextview.setText(MediaPlayerUtil.createTime(song.extractDuration()));
    }
}
