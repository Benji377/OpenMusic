package com.musicplayer.SocyMusic.ui.queue;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.musicplayer.SocyMusic.Song;
import com.musicplayer.SocyMusic.SongsData;
import com.woxthebox.draglistview.DragItemAdapter;

import java.util.List;

public class ItemAdapter extends DragItemAdapter<Song, ItemHolder> {
    private final SongsData songsData;
    private final Context context;
    private final int grabHandleID;
    private final boolean dragOnLongPress;
    private final int layoutID;

    private ItemHolder playingHolder;
    private onItemClickedListener clickListener;

    public ItemAdapter(Context context, List<Song> queue, @LayoutRes int layoutID, @IdRes int grabHandleID, boolean dragOnLongPress) {
        this.context = context;
        this.songsData = SongsData.getInstance(context);
        this.layoutID = layoutID;
        this.grabHandleID = grabHandleID;
        this.dragOnLongPress = dragOnLongPress;
        setItemList(queue);
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(layoutID, parent, false);
        return new ItemHolder(context, view, grabHandleID, dragOnLongPress, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Song song = songsData.getSongFromQueueAt(position);
        holder.bind(song);
        if (holder.isPlaying())
            playingHolder = holder;
    }

    public void setOnItemClickListener(onItemClickedListener clickListener) {
        this.clickListener = clickListener;
    }

    void releasePlayingVisualizer() {
        if (playingHolder != null)
            playingHolder.releaseVisualizer();
    }

    @Override
    public long getUniqueItemId(int position) {
        Song song = songsData.getSongFromQueueAt(position);
        return song.hashCode();
    }

    @Override
    public int getItemCount() {
        return songsData.getPlayingQueueCount();
    }

    interface onItemClickedListener {
        void onItemClicked(int position);
    }
}