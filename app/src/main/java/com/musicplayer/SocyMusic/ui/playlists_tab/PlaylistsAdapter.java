package com.musicplayer.SocyMusic.ui.playlists_tab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.musicplayer.SocyMusic.Playlist;
import com.musicplayer.musicplayer.R;

import java.util.List;

public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistHolder> {
    private final Context context;
    private List<Playlist> allPlaylist;
    private ItemClickListener clickListener;

    public PlaylistsAdapter(Context context, List<Playlist> allPlaylist) {
        super();
        this.allPlaylist = allPlaylist;
        this.context = context;
    }

    @NonNull
    @Override
    public PlaylistHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.list_item_playlist, parent, false);
        return new PlaylistHolder(itemView, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistHolder holder, int position) {
        Playlist playlist = allPlaylist.get(position);
        holder.bind(playlist);
    }

    @Override
    public int getItemCount() {
        return allPlaylist.size();
    }

    public void setOnItemClickListener(ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setAllPlaylist(List<Playlist> allPlaylist) {
        this.allPlaylist = allPlaylist;
    }

    public interface ItemClickListener {
        void onItemClick(int position, View view);

        boolean onItemLongClick(int position, View view);
    }

}