package com.musicplayer.SocyMusic;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.musicplayer.musicplayer.R;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.woxthebox.draglistview.DragItemAdapter;
import com.woxthebox.draglistview.DragListView;

public class QueueFragment extends Fragment {
    private DragListView listView;
    private ItemAdapter adapter;
    private QueueFragmentHost hostCallBack;
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
        listView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ItemAdapter(R.layout.list_item_queue, R.id.bar_visualizer_queue_item, false);
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
                adapter.releasePlayingVisualizer();
            }
        });
        listView.setCanDragHorizontally(false);
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
            this.hostCallBack = (QueueFragment.QueueFragmentHost) context;
        } catch (final ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement QueueFragmentHost");
        }
    }

    public void updateQueue() {
        adapter.notifyDataSetChanged();
    }

    class ItemAdapter extends DragItemAdapter<Song, ItemAdapter.ViewHolder> {
        private ViewHolder playingHolder;
        private final int grabHandleID;
        private final boolean dragOnLongPress;
        private final int layoutID;

        public ItemAdapter(int layoutID, int grabHandleID, boolean dragOnLongPress) {
            super();
            this.layoutID = layoutID;
            this.grabHandleID = grabHandleID;
            this.dragOnLongPress = dragOnLongPress;
            setItemList(songsData.getPlayingQueue());
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(layoutID, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            holder.position = position;
            Song song = songsData.getSongFromQueueAt(position);
            holder.setSong(song);
            if (holder.isPlaying())
                playingHolder = holder;
            holder.updateViews();
        }

        void releasePlayingVisualizer() {
            if (playingHolder != null)
                playingHolder.visualizer.release();
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


        class ViewHolder extends DragItemAdapter.ViewHolder {
            private final TextView songTitleTextView;
            private final BarVisualizer visualizer;
            private int position;
            private Song song;

            public ViewHolder(View itemView) {
                super(itemView, grabHandleID, dragOnLongPress);
                songTitleTextView = itemView.findViewById(R.id.textview_queue_item_song_title);
                visualizer = itemView.findViewById(R.id.bar_visualizer_queue_item);
            }

            public void updateViews() {
                songTitleTextView.setText(song.getTitle());
                visualizer.release();
                if (isPlaying()) {
                    visualizer.setBackground(null);

                    int audioSessionID = MediaPlayerUtil.getAudioSessionId();
                    if (audioSessionID != -1 && audioSessionID != 0)
                        visualizer.setAudioSessionId(audioSessionID);
                    visualizer.show();
                } else {
                    visualizer.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_drag_handle, null));
                    visualizer.hide();
                }
            }

            public void setSong(Song song) {
                this.song = song;
            }

            public boolean isPlaying() {
                return song.equals(songsData.getSongPlaying());
            }

            @Override
            public void onItemClicked(View view) {
                super.onItemClicked(view);
                songsData.setPlayingIndex(position);
                MediaPlayerUtil.startPlaying(requireContext(), songsData.getSongPlaying());
                updateViews();
                hostCallBack.onSongUpdate();
            }


        }

    }

    public interface QueueFragmentHost {
        void onSongUpdate();
    }
}

