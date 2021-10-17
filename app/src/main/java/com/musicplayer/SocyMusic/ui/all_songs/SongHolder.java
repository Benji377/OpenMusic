package com.musicplayer.SocyMusic.ui.all_songs;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.musicplayer.SocyMusic.data.Album;
import com.musicplayer.SocyMusic.data.Song;
import com.musicplayer.musicplayer.R;

public class SongHolder extends RecyclerView.ViewHolder {
    private final TextView songTitleTextView;
    private final ImageView songAlbumArtImageView;

    public SongHolder(@NonNull View itemView, SongListAdapter.ItemClickListener clickListeners) {
        super(itemView);
        songTitleTextView = itemView.findViewById(R.id.textview_all_songs_item_song_title);
        songAlbumArtImageView = itemView.findViewById(R.id.imageview_all_songs_item_album_art);
        itemView.setOnClickListener(v -> clickListeners.onItemClick(getBindingAdapterPosition(), v));
        itemView.setOnLongClickListener(v -> clickListeners.onItemLongClick(getBindingAdapterPosition(), v));
    }

    public void bind(Song song) {
        songTitleTextView.setText(song.getTitle());
        Album album = song.getAlbum();
        String artPath = album == null ? null : album.getArtPath();
        Glide.with(itemView.getContext())
                .load(artPath)
                .placeholder(R.drawable.ic_music)
                .into(songAlbumArtImageView);
    }

}