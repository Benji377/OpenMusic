package com.musicplayer.SocyMusic.ui.albums_tab;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.musicplayer.SocyMusic.data.Album;
import com.musicplayer.musicplayer.R;

public class AlbumHolder extends RecyclerView.ViewHolder {
    private TextView albumTitleTextview;
    private TextView songCountTextview;
    private ImageView albumArtImageView;

    public AlbumHolder(@NonNull View itemView) {
        super(itemView);
        albumTitleTextview = itemView.findViewById(R.id.textview_album_card_title);
        songCountTextview = itemView.findViewById(R.id.textview_album_card_song_count);
        albumArtImageView = itemView.findViewById(R.id.imageview_album_card_art);
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
