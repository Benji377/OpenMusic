package com.musicplayer.OpenMusic.ui.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.musicplayer.OpenMusic.custom_views.CustomRecyclerView;
import com.musicplayer.OpenMusic.data.Song;
import com.musicplayer.OpenMusic.data.SongsData;
import com.musicplayer.OpenMusic.ui.all_songs.SongListAdapter;
import com.musicplayer.musicplayer.R;

import java.util.List;
import java.util.stream.Collectors;

public class SearchFragment extends Fragment {
    // TODO: Fix: App crash when clicking on song and song not displaying when searching
    private SongsData songsData;
    private SearchView searchView;
    private CustomRecyclerView recyclerView;
    private List<String> list;
    private Host hostCallBack;
    private SearchViewAdapter songListAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        songsData = SongsData.getInstance(requireContext());
        // Gets the list of songs, but we only need the title,
        // so thats what we are building the list with
        list = songsData.getAllSongs().stream().map(Song::getTitle).collect(Collectors.toList());
        list = list.stream().map(String::toLowerCase).collect(Collectors.toList());
        songListAdapter = new SearchViewAdapter(getContext(), songsData.getAllSongs());
        songListAdapter.setOnItemClickListener(new SongListAdapter.ItemClickListener() {
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
                    songListAdapter.notifyDataSetChanged();
                    return;
                }
                songsData.playAllFrom(position);
                Log.e("SONG", "Song at " + position + " = " + songsData.getSongAt(position));
                hostCallBack.onSongClick(songsData.getSongAt(position));
            }

            @Override
            public boolean onItemLongClick(int position, View view) {
                return false;
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        searchView = view.findViewById(R.id.search_view);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(songListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (list.contains(query.toLowerCase())) {
                    songListAdapter.getFilter().filter(query);
                } else {
                    Toast.makeText(requireContext(), "No song found on " + songListAdapter.getItemCount(), Toast.LENGTH_SHORT).show();
                    songListAdapter.getFilter().filter("");
                }
                songListAdapter.setAllSongs(songsData.getAllSongs());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Toast.makeText(requireContext(), "Amount of songs: " + songListAdapter.getItemCount(), Toast.LENGTH_SHORT).show();
                songListAdapter.getFilter().filter(newText);
                songListAdapter.setAllSongs(songsData.getAllSongs());
                return false;
            }
        });
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            this.hostCallBack = (Host) context;
            // If implementation is missing
        } catch (final ClassCastException e) {
            throw new ClassCastException(context + " must implement SearchFragment.Host");
        }
    }

    public interface Host {
        void onSongClick(Song songClicked);
    }
}
