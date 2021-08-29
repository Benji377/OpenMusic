package com.musicplayer.SocyMusic.ui.queue;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.musicplayer.SocyMusic.MediaPlayerUtil;
import com.musicplayer.SocyMusic.data.SongsData;
import com.musicplayer.musicplayer.R;
import com.woxthebox.draglistview.DragListView;

public class QueueFragment extends Fragment {
    private DragListView listView;
    private ItemAdapter adapter;
    private Host hostCallBack;
    private SongsData songsData;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        songsData = SongsData.getInstance(requireContext());
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_queue, container, false);

        listView = view.findViewById(R.id.listview_queue_songs);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        listView.setLayoutManager(layoutManager);
        adapter = new ItemAdapter(requireContext(), songsData.getPlayingQueue(), R.layout.list_item_queue, R.id.bar_visualizer_queue_item, false);
        adapter.setOnItemClickListener(position -> {
            songsData.setPlayingIndex(position);
            MediaPlayerUtil.playCurrent(requireContext());
            hostCallBack.onSongPlayingUpdate();
        });
        listView.setAdapter(adapter, false);
        listView.setDragListListener(new DragListView.DragListListener() {
            @Override
            public void onItemDragStarted(int position) {

            }

            @Override
            public void onItemDragging(int itemPosition, float x, float y) {

            }

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                songsData.onQueueReordered(fromPosition, toPosition);
                hostCallBack.onQueueReordered();
                adapter.releasePlayingVisualizer();
                int playingIndex = songsData.getPlayingIndex();
                if (fromPosition <= playingIndex || toPosition <= playingIndex) {
                    adapter.notifyItemRangeChanged(0, playingIndex);
                }
            }
        });
        listView.setCanDragHorizontally(false);
        layoutManager.scrollToPosition(songsData.getPlayingIndex());
        return view;
    }

    @Override
    public void onDestroyView() {
        adapter.releasePlayingVisualizer();
        super.onDestroyView();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            this.hostCallBack = (Host) context;
        } catch (final ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement QueueFragment.Host");
        }
    }

    public void updateQueue() {
        adapter.notifyDataSetChanged();
    }


    public interface Host {
        void onSongPlayingUpdate();

        void onQueueReordered();
    }
}

