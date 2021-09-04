package com.musicplayer.SocyMusic.ui.album;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.musicplayer.SocyMusic.MediaPlayerUtil;
import com.musicplayer.SocyMusic.data.Song;
import com.musicplayer.musicplayer.R;

public class AlbumSongHolder extends RecyclerView.ViewHolder {
    private TextView songTitleTextView;
    private TextView songLengthTextView;

    public AlbumSongHolder(@NonNull View itemView, AlbumSongAdapter.ItemClickListener clickListener) {
        super(itemView);
        itemView.setOnClickListener(view -> clickListener.onItemClick(getBindingAdapterPosition(), view));
        itemView.setOnLongClickListener(view -> clickListener.onItemLongClick(getBindingAdapterPosition(), view));
        songTitleTextView = itemView.findViewById(R.id.textview_album_song_item_title);
        songLengthTextView = itemView.findViewById(R.id.textview_album_song_item_length);
    }

    public void bind(Song song) {
        songTitleTextView.setText(song.getTitle());
        songLengthTextView.setText(MediaPlayerUtil.createTime(song.getDuration()));
    }
}
