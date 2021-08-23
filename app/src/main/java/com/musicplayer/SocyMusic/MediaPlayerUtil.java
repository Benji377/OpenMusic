package com.musicplayer.SocyMusic;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.musicplayer.SocyMusic.data.Song;
import com.musicplayer.SocyMusic.data.SongsData;
import com.musicplayer.SocyMusic.ui.main.MainActivity;

public class MediaPlayerUtil {
    private static MediaPlayer mediaPlayer;

    /**
     * Starts the mediaplayer and returns its state
     *
     * @param context Context of the app
     * @param song    Song to be played
     * @return true if successful, else false
     */
    public static boolean startPlaying(@NonNull Context context, Song song) {
        // Gets the file from the Song
        Uri uri = Uri.fromFile(song.getFile());
        // If the mediaplayer already exists or is playing
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        // Creates a new mediaplayer
        mediaPlayer = MediaPlayer.create(context, uri);
        // Something went wrong
        if (mediaPlayer == null)
            return false;
        // Mediaplayer is ready
        mediaPlayer.setOnCompletionListener(mp -> {
            playNext(context);
            if (context instanceof MainActivity)
                ((MainActivity) context).onSongUpdate();
        });
        // Start playing
        mediaPlayer.start();
        return true;
    }

    public static void playCurrent(Context context) {
        SongsData songsData = SongsData.getInstance(context);
        startPlaying(context, songsData.getSongPlaying());
    }

    /**
     * Plays the next song.
     * If repeat is set to true then it will just keep playing the same song
     * If the song is the last in queue it will play the first song in queue again
     *
     * @param context Context of the app
     */
    public static void playNext(Context context) {
        // Fixed instance
        SongsData songsData = SongsData.getInstance(context);
        // Repeats the song
        if (songsData.isRepeat()) {
            SongsData.getInstance(context).setPlayingIndex(songsData.getPlayingIndex());
        } else if (songsData.lastInQueue() && !songsData.isRepeat()) {
            songsData.setPlayingIndex(0);
            // Plays the next song
        } else {
            songsData.playNext();
        }
        // Starts playing the selected song
        startPlaying(context, songsData.getSongPlaying());
    }

    /**
     * Plays the previous song in queue
     * If the actual song is the first song it just plays the last song in queue
     * If  repeat is set to true, it just plays the same song again
     *
     * @param context Context of the app
     */
    public static void playPrev(Context context) {
        // Fixed instance
        SongsData songsData = SongsData.getInstance(context);
        if (songsData.isRepeat()) {
            SongsData.getInstance(context).setPlayingIndex(songsData.getPlayingIndex());
            // Plays the last song in queue
        } else if (songsData.firstInQueue() && !songsData.isRepeat()) {
            songsData.setPlayingIndex(songsData.songsCount() - 1);
            // Plays the previous song
        } else {
            songsData.playPrev();
        }
        // Starts playing the selected song
        startPlaying(context, songsData.getSongPlaying());
    }

    /**
     * Toggles between two modes: paused and playing
     * If the mediaplayer is paused, it starts playing and vice versa
     */
    public static void togglePlayPause() {
        // Something went wrong
        if (mediaPlayer == null)
            return;
        // Toggles mode
        if (mediaPlayer.isPlaying())
            mediaPlayer.pause();
        else
            mediaPlayer.start();
    }

    public static void play() {
        if (mediaPlayer == null || mediaPlayer.isPlaying())
            return;
        mediaPlayer.start();
    }


    public static void pause() {
        if (mediaPlayer == null || !mediaPlayer.isPlaying())
            return;
        mediaPlayer.pause();
    }

    /**
     * Stops and releases the mediaplayer to avoid errors
     */
    public static void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     * Checks if the mediaplayer is stopped
     *
     * @return true if stopped, else false
     */
    public static boolean isStopped() {
        return mediaPlayer == null;
    }

    /**
     * Checks if mediaplayer is playing
     *
     * @return true if playing, else false
     */
    public static boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    /**
     * Sets the mediaplayer to play at a defined position in the song
     *
     * @param pos Position to be played at
     */
    public static void seekTo(int pos) {
        if (mediaPlayer != null)
            mediaPlayer.seekTo(pos);
    }

    /**
     * Gets the position the mediaplayer is currently at
     *
     * @return The position the player is at right now or -1 if an error occurs
     */
    public static int getPosition() {
        if (mediaPlayer == null)
            return -1;
        return mediaPlayer.getCurrentPosition();
    }

    /**
     * Gets the duration of the song that is being played right now
     *
     * @return The duration of the song or -1 if an error occurs
     */
    public static int getDuration() {
        if (mediaPlayer == null)
            return -1;
        return mediaPlayer.getDuration();
    }

    /**
     * Gets the AudiosessionID of the mediaplayer
     *
     * @return the ID or 0 if an error occurs
     */
    public static int getAudioSessionId() {
        if (mediaPlayer == null)
            return 0;
        return mediaPlayer.getAudioSessionId();
    }

    /**
     * Converts the milliseconds in a displayable time-format like this --> min:sec
     *
     * @param duration time in milliseconds
     * @return String with the converted time in minutes and seconds
     */
    public static String createTime(int duration) {
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
}
