package com.musicplayer.SocyMusic.ui.player_song_info;

import android.annotation.SuppressLint;
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

import com.musicplayer.SocyMusic.data.Song;
import com.musicplayer.SocyMusic.data.SongsData;
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
        if (song == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && current_song.extractThumbnail() != null)
                sthumbnail.setImageBitmap(current_song.extractThumbnail());
            if (current_song.getTitle() != null)
                stitle.setText(String.format("Title: %s", current_song.getTitle()));
            slength.setText(String.format("Duration: %d", current_song.extractDuration()));
            if (current_song.extractArtists() != null)
                sartists.setText(String.format("Artists: %s", current_song.extractArtists()));
            if (current_song.extractAlbumTitle() != null)
                salbums.setText(String.format("Album: %s", current_song.extractAlbumTitle()));
            String playlists = SongsData.getInstance(getContext()).getPlaylistsOfSong(current_song);
            splaylists.setText(String.format("Playlist(s): %s", playlists));
            stags.setText("Coming soon");
        }
    }
}
