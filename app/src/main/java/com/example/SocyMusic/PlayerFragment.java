package com.example.SocyMusic;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.musicplayer.R;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;


public class PlayerFragment extends Fragment {
    private Button playSongButton;
    private Button nextSongButton;
    private Button previousSongButton;
    private Button fastForwardButton;
    private Button fastRewindButton;
    private CheckBox repeatCheckBox;
    private CheckBox shuffleCheckBox;

    private TextView songNameTextview;
    private TextView songStartTimeTextview;
    private TextView songEndTimeTextview;

    private SeekBar songSeekBar;
    private BarVisualizer visualizer;
    private ImageView songThumbnail;

    private PlayerFragmentHost hostCallBack;
    private Song songPlaying;
    private boolean currentlySeeking;

    /**
     * Gets automatically executed when the Player gets created
     * @param savedInstanceState Standard Android stuff
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Gets the song which is actually playing
        // (The one the user clicked on in the list before)
        songPlaying = SongsData.getInstance().getSongPlaying();
    }

    /**
     * Creates the actual look of the Player with all its elements
     * @param inflater Overridden
     * @param container Overridden
     * @param savedInstanceState Overridden
     * @return The view of the Fragment
     */
    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Creates a view by inflating its layout
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        // Adds all buttons previously declared above
        previousSongButton = view.findViewById(R.id.btnprev);
        nextSongButton = view.findViewById(R.id.btnnext);
        playSongButton = view.findViewById(R.id.playbtn);
        fastForwardButton = view.findViewById(R.id.btnff);
        fastRewindButton = view.findViewById(R.id.btnfr);
        repeatCheckBox = view.findViewById(R.id.repeat_checkbox);
        shuffleCheckBox = view.findViewById(R.id.shuffle_checkbox);

        // Adds all texts
        songNameTextview = view.findViewById(R.id.txtsongname);
        songStartTimeTextview = view.findViewById(R.id.txtsstart);
        songEndTimeTextview = view.findViewById(R.id.txtsstop);

        // Adds seekbar, visualizer and the image of the player
        songSeekBar = view.findViewById(R.id.seekbar);
        visualizer = view.findViewById(R.id.blast);
        songThumbnail = view.findViewById(R.id.imageview);

        // After creating every element, the song starts playing
        MediaPlayerUtil.startPlaying(getContext(), songPlaying);
        updatePlayerUI();

        // The option to repeat the song or not
        repeatCheckBox.setChecked(SongsData.getInstance().isRepeat());

        // The option to shuffle the queue
        shuffleCheckBox.setChecked(SongsData.getInstance().isShuffle());

        // This is necessary to fix the marquee, which was lagging sometimes
        songNameTextview.setEnabled(true);
        songNameTextview.setSelected(true);
        songNameTextview.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            // Manually sets the width and height of the TextView to fix the marquee issue
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                ViewGroup.LayoutParams params = v.getLayoutParams();
                params.width = right - left;
                params.height = bottom - top;
                v.removeOnLayoutChangeListener(this);
                v.setLayoutParams(params);
            }
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
                        // If the activity gets interrupted. For example app gets closed.
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
                .getColor(R.color.purple_500), PorterDuff.Mode.MULTIPLY);
        songSeekBar.getThumb().setColorFilter(getResources().getColor(R.color.purple_500), PorterDuff.Mode.SRC_IN);

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

        // Needed for the Mediaplayer to function
        int audioSessionId = MediaPlayerUtil.getAudioSessionId();
        if (audioSessionId != -1) {
            visualizer.setAudioSessionId(audioSessionId);
        }

        // Plays the next song
        nextSongButton.setOnClickListener(v -> playNextSong());

        // Plays the previous song
        previousSongButton.setOnClickListener(v -> playPrevSong());

        // Moves 10 seconds forward in the song
        fastForwardButton.setOnClickListener(v -> {
            if (MediaPlayerUtil.isPlaying()) {
                MediaPlayerUtil.seekTo(MediaPlayerUtil.getPosition() + 10000);
                songStartTimeTextview.setText(createTime(MediaPlayerUtil.getPosition()));
            }
        });

        // Moves 10 seconds backwards in the song
        fastRewindButton.setOnClickListener(v -> {
            if (MediaPlayerUtil.isPlaying()) {
                MediaPlayerUtil.seekTo(MediaPlayerUtil.getPosition() - 10000);
                songStartTimeTextview.setText(createTime(MediaPlayerUtil.getPosition()));
            }
        });

        // Sets if the song should be repeated or not
        repeatCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SongsData.getInstance().setRepeat(isChecked);
        });

        // Sets if the queue should be shuffled
        shuffleCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SongsData.getInstance().setShuffle(isChecked);
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
     * @param context The context of the app
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            this.hostCallBack = (PlayerFragmentHost) context;
        // If implementation is missing
        } catch (final ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnCompleteListener");
        }
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
        if (visualizer != null) {
            visualizer.release();
        }
        super.onDestroy();
    }


    /**
     * Creates a new instance of the fragment
     * @return The fragment as a class
     */
    public static PlayerFragment newInstance() {
//        **insert arguments here**
        return new PlayerFragment();
    }

    /**
     * Method to animate the thumbnail of the song, gets executed when the user clicks
     * next or previous.
     * @param direction Defines in which direction it rotates (1, -1)
     */
    public void animateSongThumbail(int direction) {
        // rotates the red note image at 360 or -360 degrees
        ObjectAnimator animator = ObjectAnimator.ofFloat(songThumbnail, "rotation", 0f, 360f * direction);
        // 1000ms = 1s
        animator.setDuration(1000);
        // External animation class --> Github
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

    /**
     * Updates all the elements on the fragment. Useful to check if a change has happened and not
     * leave the user behind with old information
     */
    public void updatePlayerUI() {
        // Retrieves the song playing right now
        songPlaying = SongsData.getInstance().getSongPlaying();

        // Sets all properties again
        songNameTextview.setText(songPlaying.getTitle());
        int position = MediaPlayerUtil.getPosition();
        int duration = MediaPlayerUtil.getDuration();
        songSeekBar.setMax(duration);
        songSeekBar.setProgress(position);

        // Sets the time of the song
        songEndTimeTextview.setText(createTime(duration));
        songStartTimeTextview.setText(createTime(position));
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
        if (isResumed()) {
            // Rotates the thumbnail
            animateSongThumbail(1);
            int audioSessionId1 = MediaPlayerUtil.getAudioSessionId();
            if (audioSessionId1 != -1)
                visualizer.setAudioSessionId(audioSessionId1);
        }
        hostCallBack.onSongUpdate();
    }

    /**
     * Plays the previous song in queue, if it is the first song then it will play the last one
     */
    protected void playPrevSong() {
        // Plays the previous song
        MediaPlayerUtil.playPrev(getContext());
        if (isResumed()) {
            // Rotates the thumbnail in the negative direction
            animateSongThumbail(-1);
            int audioSessionId12 = MediaPlayerUtil.getAudioSessionId();
            if (audioSessionId12 != -1)
                visualizer.setAudioSessionId(audioSessionId12);
        }
        hostCallBack.onSongUpdate();
    }

    /**
     * Changes the state of the song from play to pause and vice-versa
     * Also sets the appearance of the button accordingly
     */
    protected void togglePlayPause() {
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

    /**
     * Interface, needed for detecting specific states of the fragment
     */
    public interface PlayerFragmentHost {
        //callback methods
        void onLoadComplete();
        void onPlaybackUpdate();
        void onSongUpdate();
    }
}
