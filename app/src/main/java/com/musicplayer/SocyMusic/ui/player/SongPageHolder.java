package com.musicplayer.SocyMusic.ui.player;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.musicplayer.SocyMusic.data.Album;
import com.musicplayer.SocyMusic.data.Song;
import com.musicplayer.musicplayer.R;

public class SongPageHolder extends RecyclerView.ViewHolder {
    private final TextView songTitleTextView;
    private final ImageView songAlbumArtImageView;

    public SongPageHolder(@NonNull View itemView) {
        super(itemView);
        songTitleTextView = itemView.findViewById(R.id.textview_player_song_title);
        songAlbumArtImageView = itemView.findViewById(R.id.imageview_player_album_art);
        // This is necessary to fix the marquee, which was lagging sometimes
        songTitleTextView.setEnabled(true);
        songTitleTextView.setSelected(true);
        songTitleTextView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            // Manually sets the width and height of the TextView to fix the marquee issue
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                ViewGroup.LayoutParams params = v.getLayoutParams();
                params.width = right - left;
                params.height = bottom - top;
                v.removeOnLayoutChangeListener(this);
                v.setLayoutParams(params);
            }
        });

    }

    public void bind(Song song) {
        songTitleTextView.setText(song.getTitle());
        Album album = song.getAlbum();
        String artPath = album == null ? null : album.getArtPath();
        Glide.with(itemView.getContext())
                .load(artPath)
                .placeholder(R.drawable.music_combined)
                .into(songAlbumArtImageView);
    }
}
