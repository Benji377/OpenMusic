package com.musicplayer.OpenMusic.ui.album;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.musicplayer.OpenMusic.custom_views.CustomRecyclerView;
import com.musicplayer.OpenMusic.data.Album;
import com.musicplayer.OpenMusic.data.Song;
import com.musicplayer.OpenMusic.data.SongsData;
import com.musicplayer.musicplayer.R;

public class AlbumFragment extends Fragment {
    private static final String KEY_ALBUM = "com.musicplayer.OpenMusic.ui.album.AlbumFragment.album";
    private Album album;
    private CustomRecyclerView songsRecyclerView;
    private SongsData songsData;
    private Host hostCallback;
    private TextView albumTitleTextview;
    private ImageView albumArtImageView;
    private TextView songCountTextview;

    public static AlbumFragment newInstance(Album album) {
        Bundle args = new Bundle();
        args.putSerializable(KEY_ALBUM, album);
        AlbumFragment fragment = new AlbumFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            album = (Album) getArguments().getSerializable(KEY_ALBUM);
        songsData = SongsData.getInstance(requireContext());
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album, container, false);

        CollapsingToolbarLayout toolbarLayout = view.findViewById(R.id.layout_album_collapsing_toolbar);
        toolbarLayout.setTitle(album.getTitle());

        AppBarLayout appBarLayout = (AppBarLayout) view.findViewById(R.id.layout_album_appbar);
        View toolbarContent = view.findViewById(R.id.layout_album_toolbar_content);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1)
                    scrollRange = appBarLayout.getTotalScrollRange();
                float offsetFactor = (float) (-verticalOffset) / (float) scrollRange;
                toolbarContent.setAlpha(1 - offsetFactor);
                if (scrollRange + verticalOffset == 0)
                    toolbarLayout.setTitle(album.getTitle());
                else
                    toolbarLayout.setTitle("");
            }
        });

        albumTitleTextview = view.findViewById(R.id.textview_album_name);
        albumTitleTextview.setText(album.getTitle());
        albumArtImageView = view.findViewById(R.id.imageview_album_art);
        Glide.with(requireContext())
                .load(album.getArtPath())
                .placeholder(R.drawable.music_combined)
                .into(albumArtImageView);

        songCountTextview = view.findViewById(R.id.textview_album_song_count);
        songCountTextview.setText(requireContext().getString(R.string.all_song_count, album.getSongCount()));

        AlbumSongAdapter adapter = new AlbumSongAdapter(requireContext(), album.getSongList());
        adapter.setOnItemClickListener(new AlbumSongAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                songsData.playAlbumFrom(album, position);
                hostCallback.onSongClick(album.getSongAt(position));
            }

            @Override
            public boolean onItemLongClick(int position, View view) {
                return false;
            }
        });

        songsRecyclerView = view.findViewById(R.id.recyclerview_album_songs);
        songsRecyclerView.setAdapter(adapter);
        songsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        //TODO: get this empty textview to work with the collapsing toolbar layout
//        TextView listEmptyTextview = view.findViewById(R.id.textview_playlist_empty);
//        songsRecyclerView.setEmptyView(listEmptyTextview);
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            this.hostCallback = (Host) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement AlbumFragment.Host");
        }
    }

    interface Host {
        void onSongClick(Song song);
    }
}
