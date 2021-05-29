package com.example.SocyMusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;


public class PlayerActivity extends AppCompatActivity {

    // All required variables get declared here
    Button playSong_button, nextSong_button, previousSong_button, fastForwardSong_button, fastRewindSong_button;
    TextView songName_textview, songStarttime_textview, songEndtime_textview;
    SeekBar song_loadingbar;
    BarVisualizer visualizer;
    ImageView song_thumbnail;
    String songName;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int song_position;
    ArrayList<File> list_of_songs;
    Thread song_loadingbar_updater_thread;



    @Override
    // Shows an arrow back to the home menu
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    // Releases the music visualizer bar without errors
    protected void onDestroy() {
        if (visualizer != null) {
            visualizer.release();
        }
        super.onDestroy();
    }

    @Override
    // When the player gets created
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Sets the bar at the top
        getSupportActionBar().setTitle("Now Playing");
        // Enables the ability to get back to the home screen
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Adds all buttons previously declared above
        previousSong_button = findViewById(R.id.btnprev);
        nextSong_button = findViewById(R.id.btnnext);
        playSong_button = findViewById(R.id.playbtn);
        fastForwardSong_button = findViewById(R.id.btnff);
        fastRewindSong_button = findViewById(R.id.btnfr);

        // Adds all texts
        songName_textview = findViewById(R.id.txtsongname);
        songStarttime_textview = findViewById(R.id.txtsstart);
        songEndtime_textview = findViewById(R.id.txtsstop);

        // Adds seekbar, visualizer and the image of the player
        song_loadingbar = findViewById(R.id.seekbar);
        visualizer = findViewById(R.id.blast);
        song_thumbnail = findViewById(R.id.imageview);

        // Stops the mediaplayer to create a new one later
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        // Gets all the info about the song
        list_of_songs = (ArrayList) bundle.getParcelableArrayList("songs");
        String songName = i.getStringExtra("songname");
        song_position = bundle.getInt("pos", 0);
        songName_textview.setSelected(true);
        Uri uri = Uri.parse(list_of_songs.get(song_position).toString());
        this.songName = list_of_songs.get(song_position).getName();
        songName_textview.setText(this.songName);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        // Starts the mediaplayer
        mediaPlayer.start();

        // Starts the seekbar thread
        song_loadingbar_updater_thread = new Thread() {
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentposition = 0;

                while(currentposition < totalDuration) {
                    try {
                        sleep(500);
                        currentposition = mediaPlayer.getCurrentPosition();
                        song_loadingbar.setProgress(currentposition);
                    } catch (InterruptedException | IllegalStateException e){
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


        playSong_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    playSong_button.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                } else {
                    playSong_button.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                nextSong_button.performClick();
            }
        });

        int audiosessionId = mediaPlayer.getAudioSessionId();
        if (audiosessionId != -1) {
            visualizer.setAudioSessionId(audiosessionId);
        }

        nextSong_button.setOnClickListener(new View.OnClickListener() {
            @Override
            // plays the next song
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                song_position = ((song_position +1)% list_of_songs.size());
                Uri u = Uri.parse(list_of_songs.get(song_position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                PlayerActivity.this.songName = list_of_songs.get(song_position).getName();
                songName_textview.setText(PlayerActivity.this.songName);
                // Updates the duration of the song
                String stopTime = createTime(mediaPlayer.getDuration());
                songEndtime_textview.setText(stopTime);
                song_loadingbar.setMax(mediaPlayer.getDuration());
                // starts the mediaplayer
                mediaPlayer.start();
                playSong_button.setBackgroundResource(R.drawable.ic_pause);
                startAnimation(song_thumbnail);
                int audiosessionId = mediaPlayer.getAudioSessionId();
                if (audiosessionId != -1) {
                    visualizer.setAudioSessionId(audiosessionId);
                }
            }
        });

        previousSong_button.setOnClickListener(new View.OnClickListener() {
            @Override
            // plays the previous song
            public void onClick(View v) {
                // stops the mediaplayer
                mediaPlayer.stop();
                mediaPlayer.release();
                // goes back one position in the playlist
                song_position = ((song_position -1)<0)?(list_of_songs.size()-1):(song_position -1);
                Uri u = Uri.parse(list_of_songs.get(song_position).toString());
                // creates a new mediaplayer
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                // sets all info of song
                PlayerActivity.this.songName = list_of_songs.get(song_position).getName();
                songName_textview.setText(PlayerActivity.this.songName);
                // Updates the maximum length of the song
                String stopTime = createTime(mediaPlayer.getDuration());
                songEndtime_textview.setText(stopTime);
                song_loadingbar.setMax(mediaPlayer.getDuration());
                // strats playing of the song
                mediaPlayer.start();
                playSong_button.setBackgroundResource(R.drawable.ic_pause);
                // starts the animation
                startAnimation(song_thumbnail);
                int audiosessionId = mediaPlayer.getAudioSessionId();
                if (audiosessionId != -1) {
                    visualizer.setAudioSessionId(audiosessionId);
                }
            }
        });

        fastForwardSong_button.setOnClickListener(new View.OnClickListener() {
            @Override
            // moves 10 seconds forward in the song
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });

        fastRewindSong_button.setOnClickListener(new View.OnClickListener() {
            @Override
            // moves 10 seconds backwards in the song
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }
            }
        });


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


    // Converts integer to a displayable time String
    public String createTime(int duration) {
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;

        time+=min+":";

        if (sec < 10) {
            time+="0";
        }
        time+=sec;

        return time;
    }
}