package com.musicplayer.SocyMusic.ui.album;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.musicplayer.SocyMusic.data.Song;
import com.musicplayer.musicplayer.R;

import java.util.List;

public class AlbumSongAdapter extends RecyclerView.Adapter<AlbumSongHolder> {
    private Context context;
    private List<Song> albumSongs;
    private ItemClickListener clickListener;

    public AlbumSongAdapter(Context context, List<Song> albumSongs) {
        this.context = context;
        this.albumSongs = albumSongs;
    }

    @NonNull
    @Override
    public AlbumSongHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.list_item_album_song, parent, false);
        return new AlbumSongHolder(itemView, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumSongHolder holder, int position) {
        holder.bind(albumSongs.get(position));
    }

    @Override
    public int getItemCount() {
        return albumSongs.size();
    }

    public void setOnItemClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    interface ItemClickListener {

        void onItemClick(int position, View view);

        boolean onItemLongClick(int position, View view);

    }
}
