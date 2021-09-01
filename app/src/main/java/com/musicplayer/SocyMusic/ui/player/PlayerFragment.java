package com.musicplayer.SocyMusic.ui.player;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.musicplayer.SocyMusic.MediaPlayerUtil;
import com.musicplayer.SocyMusic.custom_views.CustomViewPager2;
import com.musicplayer.SocyMusic.data.Playlist;
import com.musicplayer.SocyMusic.data.Song;
import com.musicplayer.SocyMusic.data.SongsData;
import com.musicplayer.SocyMusic.utils.DialogUtils;
import com.musicplayer.musicplayer.R;

@SuppressWarnings("FieldCanBeLocal")
public class PlayerFragment extends Fragment {
    private Button playSongButton;
    private Button nextSongButton;
    private Button previousSongButton;
    private Button queueButton;
    private Button playlistButton;
    private CheckBox repeatCheckBox;
    private CheckBox shuffleCheckBox;
    private CheckBox favoriteCheckBox;

    private CustomViewPager2 songPager;
    private TextView songStartTimeTextview;
    private TextView songEndTimeTextview;

    private SeekBar songSeekBar;
    private BarVisualizer visualizer;

    private Host hostCallBack;
    private SongsData songsData;
    private Song songPlaying;
    private boolean startPlaying;
    private boolean currentlySeeking;

    /**
     * Gets automatically executed when the Player gets created
     *
     * @param savedInstanceState Standard Android stuff
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        songsData = SongsData.getInstance(requireContext());
        // Gets the song which is actually playing
        // (The one the user clicked on in the list before)
        songPlaying = songsData.getSongPlaying();

    }

    /**
     * Creates the actual look of the Player with all its elements
     *
     * @param inflater           Overridden
     * @param container          Overridden
     * @param savedInstanceState Overridden
     * @return The view of the Fragment
     */
    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Creates a view by inflating its layout
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        songPager = new CustomViewPager2(view.findViewById(R.id.viewpager_player_song));

        // Adds all buttons previously declared above
        previousSongButton = view.findViewById(R.id.button_player_prev);
        nextSongButton = view.findViewById(R.id.button_player_next);
        playSongButton = view.findViewById(R.id.button_player_play_pause);
        repeatCheckBox = view.findViewById(R.id.checkbox_player_repeat);
        shuffleCheckBox = view.findViewById(R.id.checkbox_player_shuffle);
        favoriteCheckBox = view.findViewById(R.id.checkbox_player_favorite);
        queueButton = view.findViewById(R.id.button_player_queue);
        playlistButton = view.findViewById(R.id.button_player_addtoplaylist);

        // Adds all texts
        songStartTimeTextview = view.findViewById(R.id.textview_player_elapsed_time);
        songEndTimeTextview = view.findViewById(R.id.textview_player_duration);

        // Adds seekbar, visualizer and the image of the player
        songSeekBar = view.findViewById(R.id.seekbar_player);
        visualizer = view.findViewById(R.id.bar_visualizer_player);

        // After creating every element, the song starts playing
        if (startPlaying)
            MediaPlayerUtil.startPlaying(requireContext(), songPlaying);

        songPager.get().setAdapter(new SongPagerAdapter(requireContext(), songsData.getPlayingQueue()));
        songPager.get().setCurrentItem(songsData.getPlayingIndex(), false);

        songPager.setOnPageChange(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageScrollStateChanged(int state) {
                songsData.setPlayingIndex(songPager.get().getCurrentItem());
                MediaPlayerUtil.playCurrent(getContext());
                hostCallBack.onSongPlayingUpdate();

            }
        });


        updatePlayerUI();

        // The option to repeat the song or not
        repeatCheckBox.setChecked(songsData.isRepeat());
        // The option to shuffle the queue
        shuffleCheckBox.setChecked(songsData.isShuffle());

        queueButton.setOnClickListener(v -> hostCallBack.showQueue());

        playlistButton.setOnClickListener(v -> DialogUtils.showAddToPlaylistDialog(requireContext(),
                songPlaying, hostCallBack::onNewPlaylist, playlist -> {
                    if (playlist.equals(songsData.getFavoritesPlaylist()))
                        favoriteCheckBox.setChecked(songsData.isFavorited(songPlaying));
                    hostCallBack.onPlaylistUpdate(playlist);
                }));

        // Starts the seekbar thread
        //mediaPlayer.getCurrentPosition();
        Thread songSeekBarUpdaterThread = new Thread() {
            @Override
            public void run() {
                // Gets the current position on the seekbar
                int currentPosition;

                while (true) {
                    if (isResumed()) {
                        try {
                            sleep(500);
                            currentPosition = MediaPlayerUtil.getPosition();
                            //mediaPlayer.getCurrentPosition();

                            // If the seekbar gets manually adjusted, we need to get the new position
                            if (!currentlySeeking)
                                songSeekBar.setProgress(currentPosition);
                            // If the activity gets interrupted. For musicplayer app gets closed.
                            // Prevents app from crashing
                        } catch (InterruptedException | IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        // Sets properties of the seekbar
        songSeekBarUpdaterThread.start();

        // Controls the changes at the seekbar
        songSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            // User is dragging the seekbar
            public void onStartTrackingTouch(SeekBar seekBar) {
                currentlySeeking = true;
            }

            @Override
            // User has stopped dragging the seekbar
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Song needs to start at the set time
                MediaPlayerUtil.seekTo(seekBar.getProgress());
                // Time passed needs to be updated
                songStartTimeTextview.setText(MediaPlayerUtil.createTime(MediaPlayerUtil.getPosition()));
                // User is no longer dragging the seekbar
                currentlySeeking = false;
            }
        });

        // Handles time
        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // While the player is playing, it gets the actual time and sets it
                if (!MediaPlayerUtil.isStopped()) {
                    String currentTime = MediaPlayerUtil.createTime(MediaPlayerUtil.getPosition());
                    songStartTimeTextview.setText(currentTime);
                    // Has a delay of 1000ms == 1s
                    handler.postDelayed(this, delay);
                }
            }
        }, delay);

        // Sets the action to execute when the button is pressed
        playSongButton.setOnClickListener(v -> togglePlayPause());

        initializeVisualizer();

        // Plays the next song
        nextSongButton.setOnClickListener(v -> playNextSong());
        nextSongButton.setOnLongClickListener(v -> {
            if (MediaPlayerUtil.isPlaying()) {
                MediaPlayerUtil.seekTo(MediaPlayerUtil.getPosition() + 10000);
                songStartTimeTextview.setText(MediaPlayerUtil.createTime(MediaPlayerUtil.getPosition()));
            }
            return true;
        });

        // Plays the previous song
        previousSongButton.setOnClickListener(v -> playPrevSong());
        previousSongButton.setOnLongClickListener(v -> {
            if (MediaPlayerUtil.isPlaying()) {
                MediaPlayerUtil.seekTo(MediaPlayerUtil.getPosition() - 10000);
                songStartTimeTextview.setText(MediaPlayerUtil.createTime(MediaPlayerUtil.getPosition()));
            }
            return true;
        });

        repeatCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> songsData.setRepeat(isChecked));

        // Sets if the queue should be shuffled
        shuffleCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            songsData.setShuffle(isChecked);
            hostCallBack.onShuffle();
        });

        favoriteCheckBox.setOnClickListener(v -> {
            boolean isChecked = ((CheckBox) v).isChecked();
            Playlist favorites = songsData.getFavoritesPlaylist();
            if (isChecked)
                songsData.insertToPlaylist(favorites, songPlaying);
            else
                songsData.removeFromPlaylist(favorites, songPlaying, favorites.getSongList().indexOf(songPlaying), true);
            hostCallBack.onPlaylistUpdate(favorites);
        });
        favoriteCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {


        });

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                // Notify hosting activity that load is complete
                hostCallBack.onPlayerLoadComplete();
            }
        });
        return view;
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
            throw new ClassCastException(context.toString() + " must implement PlayerFragment.Host");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        startPlaying = false;
        releaseVisualizer();
    }

    /**
     * Gets executed if the app has been stopped before and now has been restarted
     */
    @Override
    public void onResume() {
        // Updates the app
        updatePlayerUI();
        initializeVisualizer();
        super.onResume();
    }

    /**
     * Gets executed if the app or the fragment has been closed
     * Releases the music visualizer bar without errors
     */
    @Override
    public void onDestroy() {
        releaseVisualizer();
        super.onDestroy();
    }

    public void releaseVisualizer() {
        if (visualizer != null) {
            visualizer.release();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void invalidatePager() {
        SongPagerAdapter adapter = (SongPagerAdapter) songPager.get().getAdapter();
        if (adapter != null) {
            adapter.setQueue(songsData.getPlayingQueue());
            songPager.get().getAdapter().notifyDataSetChanged();
            songPager.get().setCurrentItem(songsData.getPlayingIndex(), false);
        }
    }


    /**
     * Creates a new instance of the fragment
     *
     * @return The fragment as a class
     */
    public static PlayerFragment newInstance(boolean startPlaying) {
        PlayerFragment instance = new PlayerFragment();
        instance.startPlaying = startPlaying;
        return instance;
    }

    /**
     * Updates all the elements on the fragment. Useful to check if a change has happened and not
     * leave the user behind with old information
     */
    public void updatePlayerUI() {
        // Retrieves the song playing right now
        songPlaying = songsData.getSongPlaying();
        favoriteCheckBox.setChecked(songsData.isFavorited(songPlaying));

        // Sets all properties again
        if (songPager.get().getCurrentItem() != songsData.getPlayingIndex())
            songPager.scrollByCode(songsData.getPlayingIndex(), false);

        int position = MediaPlayerUtil.getPosition();
        int duration = MediaPlayerUtil.getDuration();
        songSeekBar.setMax(duration);
        songSeekBar.setProgress(position);

        // Sets the time of the song
        songEndTimeTextview.setText(MediaPlayerUtil.createTime(duration));
        songStartTimeTextview.setText(MediaPlayerUtil.createTime(position));
        initializeVisualizer();
        // If paused or playing
        updatePlayButton();
    }

    /**
     * Changes the appearance of the button depending on which state the player is currently in
     */
    public void updatePlayButton() {
        if (MediaPlayerUtil.isPlaying())
            playSongButton.setBackgroundResource(R.drawable.ic_pause);
        else
            playSongButton.setBackgroundResource(R.drawable.ic_play);

    }

    /**
     * Plays the next song in the queue, if it is the last song then it will play the first
     */
    private void playNextSong() {
        // Plays the next song
        MediaPlayerUtil.playNext(getContext());
        hostCallBack.onSongPlayingUpdate();
    }

    /**
     * Plays the previous song in queue, if it is the first song then it will play the last one
     */
    private void playPrevSong() {
        // Plays the previous song
        MediaPlayerUtil.playPrev(getContext());
        hostCallBack.onSongPlayingUpdate();
    }

    /**
     * Changes the state of the song from play to pause and vice-versa
     * Also sets the appearance of the button accordingly
     */
    public void togglePlayPause() {
        // Changes the state of the song
        MediaPlayerUtil.togglePlayPause();
        if (MediaPlayerUtil.isPlaying())
            playSongButton.setBackgroundResource(R.drawable.ic_pause);
        else
            playSongButton.setBackgroundResource(R.drawable.ic_play);
        hostCallBack.onPlaybackUpdate();
    }


    public void initializeVisualizer() {
        int audioSessionId = MediaPlayerUtil.getAudioSessionId();
        if (audioSessionId != -1 && audioSessionId != 0)
            visualizer.setAudioSessionId(audioSessionId);
    }

    /**
     * Interface, needed for detecting specific states of the fragment
     */
    public interface Host {
        //callback methods
        void onPlayerLoadComplete();

        void onPlaybackUpdate();

        void onSongPlayingUpdate();

        void onShuffle();

        void onPlaylistUpdate(Playlist playlist);

        void showQueue();

        void onNewPlaylist(Playlist newPlaylist);
    }
}
