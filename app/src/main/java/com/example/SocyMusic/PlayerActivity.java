package com.example.SocyMusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
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
    private static final String CHANNEL_ID = "";
    Button btnplay, btnnext, btnprev, btnff, btnfr;
    TextView txtsname, txtsstart, txtsstop;
    SeekBar seekmusic;
    BarVisualizer visualizer;
    ImageView imageView;
    String sname;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;
    Thread updateseekbar;


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
        btnprev = findViewById(R.id.btnprev);
        btnnext = findViewById(R.id.btnnext);
        btnplay = findViewById(R.id.playbtn);
        btnff = findViewById(R.id.btnff);
        btnfr = findViewById(R.id.btnfr);

        // Adds all texts
        txtsname = findViewById(R.id.txtsongname);
        txtsstart = findViewById(R.id.txtsstart);
        txtsstop = findViewById(R.id.txtsstop);

        // Adds seekbar, visualizer and the image of the player
        seekmusic = findViewById(R.id.seekbar);
        visualizer = findViewById(R.id.blast);
        imageView = findViewById(R.id.imageview);

        // Stops the mediaplayer to create a new one later
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        // Gets all the info about the song
        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String songName = i.getStringExtra("songname");
        position = bundle.getInt("pos", 0);
        txtsname.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(position).toString());
        sname = mySongs.get(position).getName();
        txtsname.setText(sname);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        // Starts the mediaplayer
        mediaPlayer.start();

        // Starts the seekbar thread
        updateseekbar = new Thread() {
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentposition = 0;

                while(currentposition < totalDuration) {
                    try {
                        sleep(500);
                        currentposition = mediaPlayer.getCurrentPosition();
                        seekmusic.setProgress(currentposition);
                    } catch (InterruptedException | IllegalStateException e){
                        e.printStackTrace();
                    }
                }

                if (currentposition == totalDuration) {
                    btnnext.performClick();
                }
            }
        };
        seekmusic.setMax(mediaPlayer.getDuration());
        updateseekbar.start();
        seekmusic.getProgressDrawable().setColorFilter(getResources()
                .getColor(R.color.purple_500), PorterDuff.Mode.MULTIPLY);
        seekmusic.getThumb().setColorFilter(getResources().getColor(R.color.purple_500), PorterDuff.Mode.SRC_IN);

        // Controls the changes at the seekbar
        seekmusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
        txtsstop.setText(endTime);

        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                txtsstart.setText(currentTime);
                handler.postDelayed(this, delay);
            }
        }, delay);


        btnplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    btnplay.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                } else {
                    btnplay.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnnext.performClick();
            }
        });

        int audiosessionId = mediaPlayer.getAudioSessionId();
        if (audiosessionId != -1) {
            visualizer.setAudioSessionId(audiosessionId);
        }

        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            // plays the next song
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position+1)%mySongs.size());
                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                sname = mySongs.get(position).getName();
                txtsname.setText(sname);
                // Updates the duration of the song
                String stopTime = createTime(mediaPlayer.getDuration());
                txtsstop.setText(stopTime);
                seekmusic.setMax(mediaPlayer.getDuration());
                // starts the mediaplayer
                mediaPlayer.start();
                btnplay.setBackgroundResource(R.drawable.ic_pause);
                startAnimation(imageView);
                int audiosessionId = mediaPlayer.getAudioSessionId();
                if (audiosessionId != -1) {
                    visualizer.setAudioSessionId(audiosessionId);
                }
            }
        });

        btnprev.setOnClickListener(new View.OnClickListener() {
            @Override
            // plays the previous song
            public void onClick(View v) {
                // stops the mediaplayer
                mediaPlayer.stop();
                mediaPlayer.release();
                // goes back one position in the playlist
                position = ((position-1)<0)?(mySongs.size()-1):(position-1);
                Uri u = Uri.parse(mySongs.get(position).toString());
                // creates a new mediaplayer
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                // sets all info of song
                sname = mySongs.get(position).getName();
                txtsname.setText(sname);
                // Updates the maximum length of the song
                String stopTime = createTime(mediaPlayer.getDuration());
                txtsstop.setText(stopTime);
                seekmusic.setMax(mediaPlayer.getDuration());
                // strats playing of the song
                mediaPlayer.start();
                btnplay.setBackgroundResource(R.drawable.ic_pause);
                // starts the animation
                startAnimation(imageView);
                int audiosessionId = mediaPlayer.getAudioSessionId();
                if (audiosessionId != -1) {
                    visualizer.setAudioSessionId(audiosessionId);
                }
            }
        });

        btnff.setOnClickListener(new View.OnClickListener() {
            @Override
            // moves 10 seconds forward in the song
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });

        btnfr.setOnClickListener(new View.OnClickListener() {
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
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f);
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