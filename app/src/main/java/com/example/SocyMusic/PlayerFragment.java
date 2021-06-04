package com.example.SocyMusic;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.musicplayer.R;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;


public class PlayerFragment extends Fragment {

    // All required variables get declared here
    Button playSong_button, nextSong_button, previousSong_button, fastForwardSong_button, fastRewindSong_button;
    TextView songName_textview, songStarttime_textview, songEndtime_textview;
    SeekBar song_loadingbar;
    BarVisualizer visualizer;
    ImageView song_thumbnail;

    static MediaPlayer mediaPlayer;
    Thread song_loadingbar_updater_thread;

    private OnCompleteListener mListener;
    private Song songPlaying;

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
        songStarttime_textview = view.findViewById(R.id.txtsstart);
        songEndtime_textview = view.findViewById(R.id.txtsstop);

        // Adds seekbar, visualizer and the image of the player
        song_loadingbar = view.findViewById(R.id.seekbar);
        visualizer = view.findViewById(R.id.blast);
        song_thumbnail = view.findViewById(R.id.imageview);

        Uri uri = Uri.parse(songPlaying.getFile().toString());
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

        // Stops the mediaplayer to create a new one later
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(view.getContext(), uri);
        // Starts the mediaplayer
        mediaPlayer.start();

        // Starts the seekbar thread
        song_loadingbar_updater_thread = new Thread() {
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentposition = 0;

                while (currentposition < totalDuration) {
                    try {
                        sleep(500);
                        currentposition = mediaPlayer.getCurrentPosition();
                        song_loadingbar.setProgress(currentposition);
                    } catch (InterruptedException | IllegalStateException e) {
                        e.printStackTrace();
                    }
                }

                if (currentposition == totalDuration) {
                    nextSong_button.performClick();
                }
            }
        };
        song_loadingbar.setMax(mediaPlayer.getDuration());
        song_loadingbar_updater_thread.start();
        song_loadingbar.getProgressDrawable().setColorFilter(getResources()
                .getColor(R.color.purple_500), PorterDuff.Mode.MULTIPLY);
        song_loadingbar.getThumb().setColorFilter(getResources().getColor(R.color.purple_500), PorterDuff.Mode.SRC_IN);

        // Controls the changes at the seekbar
        song_loadingbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });


        String endTime = createTime(mediaPlayer.getDuration());
        songEndtime_textview.setText(endTime);

        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                songStarttime_textview.setText(currentTime);
                handler.postDelayed(this, delay);
            }
        }, delay);


        playSong_button.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                playSong_button.setBackgroundResource(R.drawable.ic_play);
                mediaPlayer.pause();
            } else {
                playSong_button.setBackgroundResource(R.drawable.ic_pause);
                mediaPlayer.start();
            }
        });

        mediaPlayer.setOnCompletionListener(mp -> nextSong_button.performClick());

        int audiosessionId = mediaPlayer.getAudioSessionId();
        if (audiosessionId != -1) {
            visualizer.setAudioSessionId(audiosessionId);
        }

        // plays the next song
        nextSong_button.setOnClickListener(v -> {
            SongsData.getInstance().playNext();
            updateSongPlaying();

            playSong_button.setBackgroundResource(R.drawable.ic_pause);
            startAnimation(song_thumbnail);
            int audiosessionId1 = mediaPlayer.getAudioSessionId();
            if (audiosessionId1 != -1) {
                visualizer.setAudioSessionId(audiosessionId1);
            }
        });

        // plays the previous song
        previousSong_button.setOnClickListener(v -> {
            // goes back one position in the playlist
            songPlaying = SongsData.getInstance().playPrev();
            updateSongPlaying();
            playSong_button.setBackgroundResource(R.drawable.ic_pause);

//                 Starts notification
//                NotificationBar notificationBar = new NotificationBar();
//                notificationBar.createNotificationChannel();
//                notificationBar.addNotification();
            // starts the animation
            startAnimation(song_thumbnail);
            int audiosessionId12 = mediaPlayer.getAudioSessionId();
            if (audiosessionId12 != -1) {
                visualizer.setAudioSessionId(audiosessionId12);
            }
        });

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
                //notify activity load is complete
                mListener.onLoadComplete();
            }
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            this.mListener = (OnCompleteListener) context;
        } catch (final ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnCompleteListener");
        }
    }

    @Override
    // Releases the music visualizer bar without errors
    public void onDestroy() {
        if (visualizer != null) {
            visualizer.release();
        }
        super.onDestroy();
    }


    public static Fragment newInstance() {
        PlayerFragment instance = new PlayerFragment();

//        Bundle args=new Bundle();
//        **insert arguments here**
//        instance.setArguments(args);

        return instance;
    }

    // Metod to start the animation
    public void startAnimation(View view) {
        // rotates the red note image at 360 degrees
        ObjectAnimator animator = ObjectAnimator.ofFloat(song_thumbnail, "rotation", 0f, 360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }


    public void updateSongPlaying() {
        // stops the mediaplayer
        mediaPlayer.stop();
        mediaPlayer.release();
        songPlaying = SongsData.getInstance().getSongPlaying();
        Uri u = Uri.parse(songPlaying.getFile().toString());
        // creates a new mediaplayer
        mediaPlayer = MediaPlayer.create(getContext(), u);
        // sets all info of song
        songName_textview.setText(songPlaying.getTitle());
        // Updates the maximum length of the song
        String stopTime = createTime(mediaPlayer.getDuration());
        songEndtime_textview.setText(stopTime);
        song_loadingbar.setMax(mediaPlayer.getDuration());
        // strats playing of the song
        mediaPlayer.start();

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

    public interface OnCompleteListener {
        //callback method
        void onLoadComplete();
    }
}