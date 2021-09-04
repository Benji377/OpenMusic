package com.musicplayer.SocyMusic.ui.all_songs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.musicplayer.SocyMusic.custom_views.CustomRecyclerView;
import com.musicplayer.SocyMusic.data.Song;
import com.musicplayer.SocyMusic.data.SongsData;
import com.musicplayer.musicplayer.R;

public class AllSongsFragment extends Fragment {

    private CustomRecyclerView songsRecyclerView;
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
        View view = inflater.inflate(R.layout.fragment_all_songs, container, false);

        songsRecyclerView = view.findViewById(R.id.recyclerview_all_songs);
        SongListAdapter customAdapter = new SongListAdapter(requireContext(), songsData.getAllSongs());
        songsRecyclerView.setAdapter(customAdapter);
        songsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        TextView emptyText = view.findViewById(R.id.textview_all_songs_list_empty);
        songsRecyclerView.setEmptyView(emptyText);

        customAdapter.setOnItemClickListener(new SongListAdapter.ItemClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onItemClick(int position, View view) {
                // Error occured
                if (!songsData.songExists(position)) {
                    Toast.makeText(requireContext(), getText(R.string.main_err_file_gone), Toast.LENGTH_LONG).show();
                    try {
                        songsData.loadFromDatabase(requireContext()).join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    customAdapter.notifyDataSetChanged();
                    return;
                }
                songsData.playAllFrom(position);
                hostCallBack.onSongClick(songsData.getSongAt(position));
            }

            @Override
            public boolean onItemLongClick(int position, View view) {
                return true;
            }
        });

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void invalidateSongList() {
        SongListAdapter adapter = (SongListAdapter) songsRecyclerView.getAdapter();
        if (adapter != null) {
            adapter.setAllSongs(songsData.getAllSongs());
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * If the fragment is being attached to another activity
     *
     * @param context The context of the app
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            this.hostCallBack = (Host) context;
            // If implementation is missing
        } catch (final ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement AllSongsFragment.Host");
        }
    }

    public interface Host {
        void onSongClick(Song songClicked);
    }

}
