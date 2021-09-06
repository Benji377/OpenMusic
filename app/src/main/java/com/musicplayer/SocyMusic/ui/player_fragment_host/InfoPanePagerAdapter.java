package com.musicplayer.SocyMusic.ui.player_fragment_host;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.musicplayer.SocyMusic.data.Song;
import com.musicplayer.musicplayer.R;

import java.util.List;

public class InfoPanePagerAdapter extends RecyclerView.Adapter<InfoPanePageHolder> {
    private final Context context;
    private List<Song> queue;
    private PaneListeners listeners;

    public InfoPanePagerAdapter(Context context, List<Song> queue) {
        this.context = context;
        this.queue = queue;
    }

    @NonNull
    @Override
    public InfoPanePageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.pager_item_song_pane, parent, false);
        return new InfoPanePageHolder(itemView, listeners);
    }

    @Override
    public void onBindViewHolder(@NonNull InfoPanePageHolder holder, int position) {
        holder.itemView.setTag(position);
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

    public List<Song> getQueue() {
        return queue;
    }

    public void setPaneListeners(PaneListeners listeners) {
        this.listeners = listeners;
    }

    public interface PaneListeners {
        void onPaneClick();

        void onPauseButtonClick();
    }
}

