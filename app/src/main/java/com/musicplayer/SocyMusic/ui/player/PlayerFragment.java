package com.musicplayer.SocyMusic.ui.player;

import android.content.Context;
import android.graphics.PorterDuff;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.musicplayer.SocyMusic.MediaPlayerUtil;
import com.musicplayer.SocyMusic.Playlist;
import com.musicplayer.SocyMusic.Song;
import com.musicplayer.SocyMusic.SongsData;
import com.musicplayer.SocyMusic.ui.queue.QueueFragment;
import com.musicplayer.musicplayer.R;


public class PlayerFragment extends Fragment {
    private Button playSongButton;
    private Button nextSongButton;
    private Button previousSongButton;
    private Button queueButton;
    private Button playlistButton;
    private CheckBox repeatCheckBox;
    private CheckBox shuffleCheckBox;
    private CheckBox favoriteCheckBox;

    private ViewPager2 songPager;
    private TextView songStartTimeTextview;
    private TextView songEndTimeTextview;

    private SeekBar songSeekBar;
    private BarVisualizer visualizer;

    private PlayerFragmentHost hostCallBack;
    private SongsData songsData;
    private Song songPlaying;
    private boolean startPlaying;
    private boolean currentlySeeking;
    private boolean scrollTriggeredByCode;

    private QueueFragment queueFragment;

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

        songPager = view.findViewById(R.id.viewpager_player_song);

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

        songPager.setAdapter(new SongPagerAdapter(requireContext(), songsData.getPlayingQueue()));
        songPager.setCurrentItem(songsData.getPlayingIndex(), false);

        songPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            private boolean newPageSelected;
            private int previousPosition = songPager.getCurrentItem();

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                newPageSelected = position != previousPosition;
                previousPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_IDLE && newPageSelected && !scrollTriggeredByCode) {
                    newPageSelected = false;
                    songsData.setPlayingIndex(songPager.getCurrentItem());
                    MediaPlayerUtil.playCurrent(getContext());
                    hostCallBack.onSongUpdate();
                }
                if (scrollTriggeredByCode && state == ViewPager2.SCROLL_STATE_IDLE) {
                    scrollTriggeredByCode = false;
                    newPageSelected = false;
                }
            }
        });


        updatePlayerUI();

        // The option to repeat the song or not
        repeatCheckBox.setChecked(songsData.isRepeat());
        // The option to shuffle the queue
        shuffleCheckBox.setChecked(songsData.isShuffle());
        // The option to set the song in the favorite playlist
        favoriteCheckBox.setChecked(songsData.isInFavorite());

        queueButton.setOnClickListener(v -> {
            // TODO: Fix this. Opens the quequefragment, but generates errors when trying to close it.
            QueueFragment queueFragment = new QueueFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            releaseVisualizer();
            View playerFragmentView = getView().findViewById(R.id.layout_player_holder);
            playerFragmentView.setVisibility(View.INVISIBLE);
            transaction.replace(R.id.layout_main_queue_container, queueFragment); // give your fragment container id in first parameter
            transaction.addToBackStack(null);  // if written, this transaction will be added to backstack
            transaction.commit();
        });

        playlistButton.setOnClickListener(v -> {
            Playlist playlist = new Playlist();
            // Testing the playlist -> FAILED
            playlist.createPlaylist("playlist_temp");
        });


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
        songSeekBar.getProgressDrawable().setColorFilter(getResources()
                .getColor(R.color.primaryAppColor), PorterDuff.Mode.MULTIPLY);
        songSeekBar.getThumb().setColorFilter(getResources().getColor(R.color.primaryAppColor), PorterDuff.Mode.SRC_IN);

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
                songStartTimeTextview.setText(createTime(MediaPlayerUtil.getPosition()));
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
                    String currentTime = createTime(MediaPlayerUtil.getPosition());
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
                songStartTimeTextview.setText(createTime(MediaPlayerUtil.getPosition()));
            }
            return true;
        });

        // Plays the previous song
        previousSongButton.setOnClickListener(v -> playPrevSong());
        previousSongButton.setOnLongClickListener(v -> {
            if (MediaPlayerUtil.isPlaying()) {
                MediaPlayerUtil.seekTo(MediaPlayerUtil.getPosition() - 10000);
                songStartTimeTextview.setText(createTime(MediaPlayerUtil.getPosition()));
            }
            return true;
        });

        repeatCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> songsData.setRepeat(isChecked));

        // Sets if the queue should be shuffled
        shuffleCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            songsData.setShuffle(isChecked);
            hostCallBack.onShuffle();
        });

        favoriteCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            songsData.setFavorited(isChecked);
            if (songsData.isInFavorite()) {
                Toast.makeText(getContext(), "Song added to Favorites", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Song removed from Favorites", Toast.LENGTH_SHORT).show();
            }
        });

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                // Notify hosting activity that load is complete
                hostCallBack.onLoadComplete();
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
            this.hostCallBack = (PlayerFragmentHost) context;
            // If implementation is missing
        } catch (final ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement PlayerFragmentHost");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        startPlaying = false;
    }

    /**
     * Gets executed if the app has been stopped before and now has been restarted
     */
    @Override
    public void onResume() {
        // Updates the app
        updatePlayerUI();
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

    public void invalidatePager() {
        SongPagerAdapter adapter = (SongPagerAdapter) songPager.getAdapter();
        if (adapter != null) {
            adapter.setQueue(songsData.getPlayingQueue());
            songPager.getAdapter().notifyDataSetChanged();
            songPager.setCurrentItem(songsData.getPlayingIndex(), false);
        }
    }


    /**
     * Creates a new instance of the fragment
     *
     * @return The fragment as a class
     */
    public static PlayerFragment newInstance() {
        PlayerFragment instance = new PlayerFragment();
        instance.startPlaying = true;
        return instance;
    }

    /**
     * Updates all the elements on the fragment. Useful to check if a change has happened and not
     * leave the user behind with old information
     */
    public void updatePlayerUI() {
        // Retrieves the song playing right now
        songPlaying = songsData.getSongPlaying();

        // Sets all properties again
        if (songPager.getCurrentItem() != songsData.getPlayingIndex()) {
            scrollTriggeredByCode = true;
            songPager.setCurrentItem(songsData.getPlayingIndex());
        }
        int position = MediaPlayerUtil.getPosition();
        int duration = MediaPlayerUtil.getDuration();
        songSeekBar.setMax(duration);
        songSeekBar.setProgress(position);

        // Sets the time of the song
        songEndTimeTextview.setText(createTime(duration));
        songStartTimeTextview.setText(createTime(position));
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
    protected void playNextSong() {
        // Plays the next song
        MediaPlayerUtil.playNext(getContext());
        hostCallBack.onSongUpdate();
    }

    /**
     * Plays the previous song in queue, if it is the first song then it will play the last one
     */
    protected void playPrevSong() {
        // Plays the previous song
        MediaPlayerUtil.playPrev(getContext());
        hostCallBack.onSongUpdate();
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

    /**
     * Converts the milliseconds in a displayable time-format like this --> min:sec
     *
     * @param duration time in milliseconds
     * @return String with the converted time in minutes and seconds
     */
    private String createTime(int duration) {
        // Placeholder
        String time = "";
        // Converts time to minutes
        int min = duration / 1000 / 60;
        // Converts time to seconds
        int sec = duration / 1000 % 60;
        // Adds to the string
        time += min + ":";
        // Adds a zero if the seconds is less than 10: 9 --> 09
        if (sec < 10) {
            time += "0";
        }
        time += sec;
        // time = min:sec
        return time;
    }

    public void initializeVisualizer() {
        int audioSessionId = MediaPlayerUtil.getAudioSessionId();
        if (audioSessionId != -1 && audioSessionId != 0)
            visualizer.setAudioSessionId(audioSessionId);
    }

    /**
     * Interface, needed for detecting specific states of the fragment
     */
    public interface PlayerFragmentHost {
        //callback methods
        void onLoadComplete();

        void onPlaybackUpdate();

        void onSongUpdate();

        void onShuffle();
    }
}
