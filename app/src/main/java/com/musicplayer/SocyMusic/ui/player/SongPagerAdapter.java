package com.musicplayer.SocyMusic.ui.player;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.musicplayer.SocyMusic.Song;
import com.musicplayer.musicplayer.R;

import java.util.List;

public class SongPagerAdapter extends RecyclerView.Adapter<SongPageHolder> {
    private final Context context;
    private List<Song> queue;

    public SongPagerAdapter(Context context, List<Song> queue) {
        this.context = context;
        this.queue = queue;
    }

    @NonNull
    @Override
    public SongPageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.pager_item_song, parent, false);
        return new SongPageHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SongPageHolder holder, int position) {
        Song song = queue.get(position);
        holder.bind(song);
    }

    @Override
    public int getItemCount() {
        return queue.size();
    }

    public void setQueue(List<Song> queue) {
        this.queue = queue;
    }
}
