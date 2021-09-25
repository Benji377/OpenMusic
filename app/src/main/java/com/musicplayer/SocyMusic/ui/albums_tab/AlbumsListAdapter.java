package com.musicplayer.SocyMusic.ui.albums_tab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.musicplayer.SocyMusic.data.Album;
import com.musicplayer.musicplayer.R;

import java.util.List;

public class AlbumsListAdapter extends RecyclerView.Adapter<AlbumHolder> {
    private final Context context;
    private List<Album> albumList;
    private ItemClickListener clickListener;

    public AlbumsListAdapter(Context context, List<Album> albumList) {
        this.context = context;
        this.albumList = albumList;
    }

    @NonNull
    @Override
    public AlbumHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.card_item_album, parent, false);
        return new AlbumHolder(itemView, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumHolder holder, int position) {
        holder.bind(albumList.get(position));
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }


    public void setAlbumList(List<Album> albumList) {
        this.albumList = albumList;
    }

    public void setOnItemClickListener(ItemClickListener clickListener) {
        this.clickListener=clickListener;
    }
    interface ItemClickListener{
        void onItemClick(int position, View view);

        boolean onItemLongClick(int position, View view);
    }
}
