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

    private TextView songNameTextview;
    private TextView songStartTimeTextview;
    private TextView songEndTimeTextview;

    private SeekBar songSeekBar;
    private BarVisualizer visualizer;
    private ImageView songThumbnail;

    private PlayerFragmentHost hostCallBack;
    private Song songPlaying;
    private boolean currentlySeeking;

    @Override
    // When the player gets created
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        songPlaying = SongsData.getInstance().getSongPlaying();
    }


    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        // Adds all buttons previously declared above
        previousSongButton = view.findViewById(R.id.btnprev);
        nextSongButton = view.findViewById(R.id.btnnext);
        playSongButton = view.findViewById(R.id.playbtn);
        fastForwardButton = view.findViewById(R.id.btnff);
        fastRewindButton = view.findViewById(R.id.btnfr);
        repeatCheckBox = view.findViewById(R.id.repeat_checkbox);

        // Adds all texts
        songNameTextview = view.findViewById(R.id.txtsongname);
        songStartTimeTextview = view.findViewById(R.id.txtsstart);
        songEndTimeTextview = view.findViewById(R.id.txtsstop);

        // Adds seekbar, visualizer and the image of the player
        songSeekBar = view.findViewById(R.id.seekbar);
        visualizer = view.findViewById(R.id.blast);
        songThumbnail = view.findViewById(R.id.imageview);

        MediaPlayerUtil.startPlaying(getContext(), songPlaying);
        updatePlayerUI();

        repeatCheckBox.setChecked(SongsData.getInstance().isRepeat());

        songNameTextview.setEnabled(true);
        songNameTextview.setSelected(true);
        songNameTextview.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
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
                int currentPosition;

                while (true) {
                    if (isResumed()) {
                        try {
                            sleep(500);
                            currentPosition = MediaPlayerUtil.getPosition();//mediaPlayer.getCurrentPosition();

                            if (!currentlySeeking)
                                songSeekBar.setProgress(currentPosition);
                        } catch (InterruptedException | IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
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
            public void onStartTrackingTouch(SeekBar seekBar) {
                currentlySeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MediaPlayerUtil.seekTo(seekBar.getProgress());
                songStartTimeTextview.setText(createTime(MediaPlayerUtil.getPosition()));
                currentlySeeking = false;
            }
        });

        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!MediaPlayerUtil.isStopped()) {
                    String currentTime = createTime(MediaPlayerUtil.getPosition());
                    songStartTimeTextview.setText(currentTime);
                    handler.postDelayed(this, delay);
                }
            }
        }, delay);


        playSongButton.setOnClickListener(v -> togglePlayPause());

        initializeVisualizer();

        // plays the next song
        nextSongButton.setOnClickListener(v -> playNextSong());

        // plays the previous song
        previousSongButton.setOnClickListener(v -> playPrevSong());

        // moves 10 seconds forward in the song
        fastForwardButton.setOnClickListener(v -> {
            if (MediaPlayerUtil.isPlaying()) {
                MediaPlayerUtil.seekTo(MediaPlayerUtil.getPosition() + 10000);
                songStartTimeTextview.setText(createTime(MediaPlayerUtil.getPosition()));
            }
        });

        // moves 10 seconds backwards in the song
        fastRewindButton.setOnClickListener(v -> {
            if (MediaPlayerUtil.isPlaying()) {
                MediaPlayerUtil.seekTo(MediaPlayerUtil.getPosition() - 10000);
                songStartTimeTextview.setText(createTime(MediaPlayerUtil.getPosition()));
            }
        });

        repeatCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> SongsData.getInstance().setRepeat(isChecked));

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                //notify hosting activity that load is complete
                hostCallBack.onLoadComplete();
            }
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            this.hostCallBack = (PlayerFragmentHost) context;
        } catch (final ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnCompleteListener");
        }
    }

    @Override
    public void onResume() {
        updatePlayerUI();
        super.onResume();
    }

    @Override
    // Releases the music visualizer bar without errors
    public void onDestroy() {
        releaseVisualizer();
        super.onDestroy();
    }

    public void releaseVisualizer() {
        if (visualizer != null) {
            visualizer.release();
        }
    }

    public static PlayerFragment newInstance() {
//        **insert arguments here**
        return new PlayerFragment();
    }

    // Method to start the animation
    public void animateSongThumbail(int direction) {
        // rotates the red note image at 360 degrees
        ObjectAnimator animator = ObjectAnimator.ofFloat(songThumbnail, "rotation", 0f, 360f * direction);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

    public void updatePlayerUI() {
        songPlaying = SongsData.getInstance().getSongPlaying();

        songNameTextview.setText(songPlaying.getTitle());
        int position = MediaPlayerUtil.getPosition();
        int duration = MediaPlayerUtil.getDuration();
        songSeekBar.setMax(duration);
        songSeekBar.setProgress(position);

        songEndTimeTextview.setText(createTime(duration));
        songStartTimeTextview.setText(createTime(position));
        updatePlayButton();
    }

    public void updatePlayButton() {
        if (MediaPlayerUtil.isPlaying())
            playSongButton.setBackgroundResource(R.drawable.ic_pause);
        else
            playSongButton.setBackgroundResource(R.drawable.ic_play);

    }


    protected void playNextSong() {
        MediaPlayerUtil.playNext(getContext());
        if (isResumed()) {
            animateSongThumbail(1);
            initializeVisualizer();
        }
        hostCallBack.onSongUpdate();
    }

    protected void playPrevSong() {
        MediaPlayerUtil.playPrev(getContext());
        if (isResumed()) {
            // starts the animation
            animateSongThumbail(-1);
            initializeVisualizer();
        }
        hostCallBack.onSongUpdate();
    }


    protected void togglePlayPause() {
        MediaPlayerUtil.togglePlayPause();
        if (MediaPlayerUtil.isPlaying())
            playSongButton.setBackgroundResource(R.drawable.ic_pause);
        else
            playSongButton.setBackgroundResource(R.drawable.ic_play);
        hostCallBack.onPlaybackUpdate();
    }

    // Converts integer to a displayable time String
    private String createTime(int duration) {
        String time = "";
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;

        time += min + ":";

        if (sec < 10) {
            time += "0";
        }
        time += sec;

        return time;
    }

    public void initializeVisualizer() {
        int audioSessionId = MediaPlayerUtil.getAudioSessionId();
        if (audioSessionId != -1 && audioSessionId != 0)
            visualizer.setAudioSessionId(audioSessionId);
    }

    public interface PlayerFragmentHost {
        //callback methods
        void onLoadComplete();

        void onPlaybackUpdate();

        void onSongUpdate();
    }
}
