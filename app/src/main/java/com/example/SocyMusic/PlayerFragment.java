package com.example.SocyMusic;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.musicplayer.R;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;


public class PlayerFragment extends Fragment {

    // All required variables get declared here
    Button playSong_button, nextSong_button, previousSong_button, fastForwardSong_button, fastRewindSong_button;
    TextView songName_textview, songStartTime_textview, songEndTime_textview;
    SeekBar song_loadingBar;
    BarVisualizer visualizer;
    ImageView song_thumbnail;

    public static MediaPlayer mediaPlayer;
    Thread song_loadingBar_updater_thread;

    private PlayerFragmentHost hostCallBack;
    private Song songPlaying;
    private boolean currentlySeeking;
    public static boolean stopped;

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
        previousSong_button = view.findViewById(R.id.btnprev);
        nextSong_button = view.findViewById(R.id.btnnext);
        playSong_button = view.findViewById(R.id.playbtn);
        fastForwardSong_button = view.findViewById(R.id.btnff);
        fastRewindSong_button = view.findViewById(R.id.btnfr);

        // Adds all texts
        songName_textview = view.findViewById(R.id.txtsongname);
        songStartTime_textview = view.findViewById(R.id.txtsstart);
        songEndTime_textview = view.findViewById(R.id.txtsstop);

        // Adds seekbar, visualizer and the image of the player
        song_loadingBar = view.findViewById(R.id.seekbar);
        visualizer = view.findViewById(R.id.blast);
        song_thumbnail = view.findViewById(R.id.imageview);

        songName_textview.setText(songPlaying.getTitle());
        songName_textview.setEnabled(true);
        songName_textview.setSelected(true);
        songName_textview.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                ViewGroup.LayoutParams params = v.getLayoutParams();
                params.width = right - left;
                params.height = bottom - top;
                v.removeOnLayoutChangeListener(this);
                v.setLayoutParams(params);
            }
        });

        updateSongPlaying();


        // Starts the seekbar thread
        song_loadingBar_updater_thread = new Thread() {
            @Override
            public void run() {
                int currentPosition;

                while (true) {
                    if (isResumed()) {
                        try {
                            sleep(500);
                            currentPosition = mediaPlayer.getCurrentPosition();

                            if (!currentlySeeking)
                                song_loadingBar.setProgress(currentPosition);
                        } catch (InterruptedException | IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        song_loadingBar.setMax(mediaPlayer.getDuration());
        song_loadingBar_updater_thread.start();
        song_loadingBar.getProgressDrawable().setColorFilter(getResources()
                .getColor(R.color.purple_500), PorterDuff.Mode.MULTIPLY);
        song_loadingBar.getThumb().setColorFilter(getResources().getColor(R.color.purple_500), PorterDuff.Mode.SRC_IN);

        // Controls the changes at the seekbar
        song_loadingBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                currentlySeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
                songStartTime_textview.setText(createTime(mediaPlayer.getCurrentPosition()));
                currentlySeeking = false;
            }
        });

        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!stopped) {
                    String currentTime = createTime(mediaPlayer.getCurrentPosition());
                    songStartTime_textview.setText(currentTime);
                    handler.postDelayed(this, delay);
                }
            }
        }, delay);


        playSong_button.setOnClickListener(v -> togglePlayPause());

        int audioSessionId = mediaPlayer.getAudioSessionId();
        if (audioSessionId != -1) {
            visualizer.setAudioSessionId(audioSessionId);
        }

        // plays the next song
        nextSong_button.setOnClickListener(v -> playNextSong());

        // plays the previous song
        previousSong_button.setOnClickListener(v -> playPrevSong());

        // moves 10 seconds forward in the song
        fastForwardSong_button.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 10000);
            }
        });

        // moves 10 seconds backwards in the song
        fastRewindSong_button.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 10000);
            }
        });

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
        song_loadingBar.setProgress(mediaPlayer.getCurrentPosition());
        super.onResume();
    }

    @Override
    // Releases the music visualizer bar without errors
    public void onDestroy() {
        if (visualizer != null) {
            visualizer.release();
        }
        super.onDestroy();
    }


    public static PlayerFragment newInstance() {
        PlayerFragment instance = new PlayerFragment();

//        Bundle args=new Bundle();
//        **insert arguments here**
//        instance.setArguments(args);

        return instance;
    }

    // Method to start the animation
    public void startAnimation() {
        // rotates the red note image at 360 degrees
        ObjectAnimator animator = ObjectAnimator.ofFloat(song_thumbnail, "rotation", 0f, 360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

    public void updateSongPlaying() {
        // stops the media player
        if (mediaPlayer != null && !stopped) {
            mediaPlayer.stop();
            mediaPlayer.release();
            stopped = true;
        }
        songPlaying = SongsData.getInstance().getSongPlaying();
        Uri u = Uri.parse(songPlaying.getFile().toString());
        // creates a new media player

        mediaPlayer = MediaPlayer.create(getContext(), u);
        int skipCount = 0;
        Toast errorToast = null;
        while (mediaPlayer == null) {
            //TODO: handle case where all files are unreadable, for now: infinite loop
            skipCount++;
            errorToast = Toast.makeText(getContext(), String.format(getString(R.string.error_file_corrupted_msg), skipCount), Toast.LENGTH_LONG);
            songPlaying = SongsData.getInstance().playNext();
            u = Uri.parse(songPlaying.getFile().toString());
            mediaPlayer = MediaPlayer.create(getContext(), u);
        }
        if (errorToast != null)
            errorToast.show();
        stopped = false;
        mediaPlayer.setOnCompletionListener(mp -> nextSong_button.performClick());
        // sets all info of song
        songName_textview.setText(songPlaying.getTitle());
        // Updates the maximum length of the song
        String stopTime = createTime(mediaPlayer.getDuration());
        songEndTime_textview.setText(stopTime);
        songStartTime_textview.setText(createTime(0));
        song_loadingBar.setMax(mediaPlayer.getDuration());
        song_loadingBar.setProgress(0);
        // starts playing the song
        mediaPlayer.start();
        playSong_button.setBackgroundResource(R.drawable.ic_pause);

    }


    protected void playNextSong() {
        // Checks if song is the last song in the list
        if (SongsData.getInstance().getSongPlaying() == SongsData.getInstance().getSongAt(SongsData.getInstance().songsCount()-1)) {
            // Starts from the beginning of the list again
            SongsData.getInstance().setPlaying(0);
        } else {
            // Plays next song
            SongsData.getInstance().playNext();
        }
        updateSongPlaying();
        hostCallBack.onSwitchTrack(songPlaying);

        if (isResumed()) {
            startAnimation();
            int audioSessionId1 = mediaPlayer.getAudioSessionId();
            if (audioSessionId1 != -1)
                visualizer.setAudioSessionId(audioSessionId1);
        }
    }

    protected void playPrevSong() {
        // Checks if song is the first song in the list
        if (SongsData.getInstance().getSongPlaying() == SongsData.getInstance().getSongAt(0)) {
            // Starts from the end of the list again
            SongsData.getInstance().setPlaying(SongsData.getInstance().songsCount()-1);
        } else {
            // Plays next song
            SongsData.getInstance().playNext();
        }
        updateSongPlaying();
        hostCallBack.onSwitchTrack(songPlaying);

        if (isResumed()) {
            // starts the animation
            startAnimation();
            int audiosessionId12 = mediaPlayer.getAudioSessionId();
            if (audiosessionId12 != -1)
                visualizer.setAudioSessionId(audiosessionId12);
        }
    }


    protected void togglePlayPause() {
        if (mediaPlayer.isPlaying()) {
            playSong_button.setBackgroundResource(R.drawable.ic_play);
            mediaPlayer.pause();
        } else {
            playSong_button.setBackgroundResource(R.drawable.ic_pause);
            mediaPlayer.start();
        }
        hostCallBack.onTogglePlayPause();
    }

    // Converts integer to a displayable time String
    public String createTime(int duration) {
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

    public interface PlayerFragmentHost {
        //callback methods
        void onLoadComplete();

        void onTogglePlayPause();

        void onSwitchTrack(Song song);

    }
}
