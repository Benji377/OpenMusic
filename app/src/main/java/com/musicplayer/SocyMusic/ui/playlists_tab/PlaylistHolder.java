package com.musicplayer.SocyMusic.ui.playlists_tab;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.musicplayer.SocyMusic.Playlist;
import com.musicplayer.musicplayer.R;

public class PlaylistHolder extends RecyclerView.ViewHolder {
    private final TextView playlistTitleTextView;
    private final PlaylistsAdapter.ItemClickListener clickListener;

    public PlaylistHolder(@NonNull View itemView, PlaylistsAdapter.ItemClickListener clickListeners) {
        super(itemView);
        playlistTitleTextView = itemView.findViewById(R.id.textview_main_item_song_title);
        this.clickListener = clickListeners;
        itemView.setOnClickListener(v -> clickListeners.onItemClick(getBindingAdapterPosition(), v));
        itemView.setOnLongClickListener(v -> clickListeners.onItemLongClick(getBindingAdapterPosition(), v));
    }

    public void bind(Playlist playlist) {
        playlistTitleTextView.setText(playlist.getName());
    }

}