package com.musicplayer.SocyMusic.ui.player_song_info;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.musicplayer.SocyMusic.MediaPlayerUtil;
import com.musicplayer.SocyMusic.SocyMusicApp;
import com.musicplayer.SocyMusic.data.Song;
import com.musicplayer.SocyMusic.data.SongsData;
import com.musicplayer.SocyMusic.ui.player.PlayerFragment;
import com.musicplayer.musicplayer.R;


public class SonginfoFragment extends Fragment {
    ImageView sthumbnail;
    TextView stitle;
    TextView slength;
    TextView sartists;
    TextView salbums;
    TextView splaylists;
    TextView stags;
    Song current_song = SongsData.data.getSongPlaying();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_songinfo, container, false);
        sthumbnail = view.findViewById(R.id.imageview_song_art);
        stitle = view.findViewById(R.id.textview_song_title);
        slength = view.findViewById(R.id.textview_song_length);
        sartists = view.findViewById(R.id.textview_song_artists);
        salbums = view.findViewById(R.id.textview_song_albums);
        splaylists = view.findViewById(R.id.textview_song_playlists);
        stags = view.findViewById(R.id.textview_song_tags);

        updateFields(null);

        return view;
    }

    /**
     * A helper method to update all layout fields. This method can also be called from the outside
     * @param song If the song is null, it will try to take the currently playing song,
     * if that is null too it gives an error.
     */
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public void updateFields(Song song) {
        if (song == null)
            song = current_song;

        if (song.extractAlbumArt() != null)
            sthumbnail.setImageBitmap(song.extractAlbumArt());

        if (song.getTitle() != null)
            stitle.setText(String.format("Title: %s", song.getTitle()));

        int duration = song.extractDuration();
        String durations = MediaPlayerUtil.createTime(duration);
        slength.setText(String.format("Duration: %s", durations));

        if (song.extractArtists() != null)
            sartists.setText(String.format("Artists: %s", song.extractArtists()));

        if (song.extractAlbumTitle() != null)
            salbums.setText(String.format("Album: %s", song.extractAlbumTitle()));

        String playlists = SongsData.getInstance(getContext()).getPlaylistsOfSong(song);
        splaylists.setText(String.format("Playlist(s): %s", playlists));
        stags.setText("Tags: Coming soon");
    }
}
