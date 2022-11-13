package com.musicplayer.OpenMusic.ui.albums_tab;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.musicplayer.OpenMusic.data.Album;
import com.musicplayer.musicplayer.R;

public class AlbumHolder extends RecyclerView.ViewHolder {
    private final TextView albumTitleTextview;
    private final TextView songCountTextview;
    private final ImageView albumArtImageView;

    public AlbumHolder(@NonNull View itemView, AlbumsListAdapter.ItemClickListener clickListener) {
        super(itemView);
        albumTitleTextview = itemView.findViewById(R.id.textview_album_card_title);
        songCountTextview = itemView.findViewById(R.id.textview_album_card_song_count);
        albumArtImageView = itemView.findViewById(R.id.imageview_album_card_art);

        itemView.setOnClickListener(view -> clickListener.onItemClick(getBindingAdapterPosition(), view));
        itemView.setOnLongClickListener(view -> clickListener.onItemLongClick(getBindingAdapterPosition(), view));
        this.setIsRecyclable(false);
    }

    public void bind(Album album) {
        albumTitleTextview.setText(album.getTitle());
        songCountTextview.setText(itemView.getContext().getString(R.string.all_song_count, album.getSongCount()));
        Glide.with(itemView.getContext())
                .load(album.getArtPath())
                .placeholder(R.drawable.music_combined)
                .into(albumArtImageView);
    }
}
