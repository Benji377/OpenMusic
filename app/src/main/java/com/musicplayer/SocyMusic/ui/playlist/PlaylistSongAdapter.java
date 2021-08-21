package com.musicplayer.SocyMusic.ui.playlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.musicplayer.SocyMusic.data.Song;
import com.musicplayer.musicplayer.R;

import java.util.List;

public class PlaylistSongAdapter extends RecyclerView.Adapter<PlaylistSongHolder> {
    private Context context;
    private List<Song> songs;

    public PlaylistSongAdapter(Context context, List<Song> songs) {
        this.context = context;
        this.songs = songs;
    }

    @NonNull
    @Override
    public PlaylistSongHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.list_item_playlist_song, parent, false);
        return new PlaylistSongHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistSongHolder holder, int position) {
        holder.bind(songs.get(position));
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }
}
