package com.musicplayer.SocyMusic.ui.all_songs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.musicplayer.SocyMusic.Song;
import com.musicplayer.musicplayer.R;

import java.util.List;

/**
 * Custom adapter for SongsData related actions
 */
public class SongListAdapter extends RecyclerView.Adapter<SongHolder> {
    private final Context context;
    private List<Song> allSongs;
    private ItemClickListener clickListener;

    public SongListAdapter(Context context, List<Song> allSongs) {
        super();
        this.allSongs = allSongs;
        this.context = context;
    }

    @NonNull
    @Override
    public SongHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.list_item_main, parent, false);
        return new SongHolder(itemView, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SongHolder holder, int position) {
        Song song = allSongs.get(position);
        holder.bind(song);
    }

    @Override
    public int getItemCount() {
        return allSongs.size();
    }

    public void setOnItemClickListener(ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setAllSongs(List<Song> allSongs) {
        this.allSongs = allSongs;
    }

    public interface ItemClickListener {
        void onItemClick(int position, View view);

        boolean onItemLongClick(int position, View view);
    }

}