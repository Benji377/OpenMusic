package com.musicplayer.OpenMusic;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import timber.log.Timber;

import androidx.annotation.NonNull;

import com.musicplayer.OpenMusic.data.Song;
import com.musicplayer.OpenMusic.data.SongsData;
import com.musicplayer.OpenMusic.ui.main.MainActivity;

public class MediaPlayerUtil {
    // mediaPlayer is a Android component used to play various media like songs or videos
    private static MediaPlayer mediaPlayer;

    /**
     * Uses the mediaPlayer to start playing a song. It then also outputs if this
     * action was successfully or not.
     * @param context Context of the app
     * @param song    Song to be played
     * @return true if successful, else false
     */
    public static boolean startPlaying(@NonNull Context context, Song song) {
        Timber.i("Creating a new mediaPlayer to start playing");
        // Gets the file from the Song
        Uri uri = Uri.fromFile(song.getFile());
        // If the mediaPlayer already exists or is playing it should be stopped and released
        if (mediaPlayer != null) {
            Timber.i("mediaPlayer already exists, releasing...");
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        // Creates a new mediaPlayer from the app context and the file extracted above
        mediaPlayer = MediaPlayer.create(context, uri);
        // Something went wrong and the mediaPlayer was not created
        if (mediaPlayer == null) {
            Timber.e("MediaPlayer has not been created, aborting");
            return false;
        }
        /*
         * MediaPlayer is ready and we are notifying a completionListener here.
         * The Listener will handle what happens when the current song (mediaPlayer) stops playing
         * In our case we want it to continue playing the next song, and that's what we are given
         * in the first parameter. In the second parameter we give him a method that should be
         * executed when the next song starts playing
         */
        mediaPlayer.setOnCompletionListener(mp -> {
            playNext(context);
            if (context instanceof MainActivity)
                ((MainActivity) context).onSongPlayingUpdate();
        });
        // Finally we start the mediaPlayer, and if everything went well until here we can return
        // a positive state, as the method got executed successfully.
        mediaPlayer.start();
        Timber.i("MediaPlayer has been created successfully");
        return true;
    }

    /**
     * This methods is mainly used when the user stops the current song and then wants to keep
     * on playing it again. We therefore just look which song is currently assigned to the mediaPlayer
     * and execute the startPlaying method to resume the song
     * @param context Context of the app
     */
    public static void playCurrent(Context context) {
        SongsData songsData = SongsData.getInstance(context);
        startPlaying(context, songsData.getSongPlaying());
    }

    /**
     * Plays the next song.
     * If repeat is set to true then it will just keep playing the same song
     * If the song is the last in queue it will play the first song in queue again
     * @param context Context of the app
     */
    public static void playNext(Context context) {
        Timber.i("Retrieving next song in queue to play from songsData");
        // Gets an instance of SongsData. This follows the Singleton design pattern
        // SongsData contains all songs and instances to it.
        SongsData songsData = SongsData.getInstance(context);
        // If the song is set on repeat, it will just set the same song as playing again
        if (songsData.isRepeat()) {
            SongsData.getInstance(context).setPlayingIndex(songsData.getPlayingIndex());
        // else, if the song should not get repeated, it will check if the song is th last in queue
        } else if (songsData.lastInQueue() && !songsData.isRepeat()) {
            // If the song is the last in queue, the first one will be played again
            songsData.setPlayingIndex(0);
        // if the song should not be repeated and its not the last in queue, just play the song
        // that comes after it
        } else {
            // WARNING! This doesn't directly play the next song, but just gets the instance of it
            songsData.playNext();
        }
        // Starts playing the selected song
        Timber.i("Starting to play next song...");
        startPlaying(context, songsData.getSongPlaying());
    }

    /**
     * Plays the previous song in queue
     * If the actual song is the first song it just plays the last song in queue
     * If  repeat is set to true, it just plays the same song again
     * @param context Context of the app
     */
    public static void playPrev(Context context) {
        Timber.i("Retrieving the previous song in queue to be played");
        // Gets the instance of songsData: like a big array containing all songs
        SongsData songsData = SongsData.getInstance(context);
        // If the song is set on repeat, just set the same song as playing again
        if (songsData.isRepeat()) {
            SongsData.getInstance(context).setPlayingIndex(songsData.getPlayingIndex());
        // If the song is the first in queue, we just pick the last song in queue
        } else if (songsData.firstInQueue() && !songsData.isRepeat()) {
            songsData.setPlayingIndex(songsData.songsCount() - 1);
        // Else, set the next song as playable
        } else {
            // WARNING! Doesn't actually play the song, but gets the instance of it
            songsData.playPrev();
        }
        // Starts playing the selected song
        Timber.i("Starting to play the selected song");
        startPlaying(context, songsData.getSongPlaying());
    }

    /**
     * Toggles between two modes: paused and playing
     * This is mainly used when the user pauses the player and then wants to resume the song
     * If the mediaPlayer is paused, it starts playing and vice versa
     */
    public static void togglePlayPause() {
        // Something went wrong
        if (mediaPlayer == null) {
            Timber.e("Can't toggle mediaPlayer, mediaPlayer is null");
            return;
        }
        // Toggles mode
        if (mediaPlayer.isPlaying()) {
            Timber.i("Toggling mediaPlayer from playing to pause");
            mediaPlayer.pause();
        } else {
            Timber.i("Toggling mediaPlayer from pause to playing");
            mediaPlayer.start();
        }
    }

    /**
     * Simply starts the mediaPlayer, but only if the player is set to something and isn't
     * already playing
     */
    public static void play() {
        Timber.i("Starting mediaPlayer...");
        if (mediaPlayer == null || mediaPlayer.isPlaying()) {
            Timber.e("Aborting, mediaPlayer is already playing or null");
            return;
        }
        mediaPlayer.start();
    }

    /**
     * Pauses the mediaPlayer if it currently playing. Returns void if the mediaPlayer isn't set
     * or not currently playing something
     */
    public static void pause() {
        Timber.i("Pausing mediaPlayer...");
        if (mediaPlayer == null || !mediaPlayer.isPlaying()) {
            Timber.e("Aborting, mediaPlayer is not playing or null");
            return;
        }
        mediaPlayer.pause();
    }

    /**
     * Stops and releases the mediaPlayer to avoid errors.
     * This assures a smooth release of memory without leaking or bugs
     */
    public static void stop() {
        Timber.i("Stopping mediaPlayer");
        if (mediaPlayer != null) {
            // First stops the mediaPlayer, then releases it from memory and finally sets it null for good measure
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        } else {
            Timber.i("mediaPlayer already stopped");
        }
    }

    /**
     * Checks if the mediaPlayer is stopped
     * @return true if stopped, else false
     */
    public static boolean isStopped() {
        return mediaPlayer == null;
    }

    /**
     * Checks if the mediaPlayer is playing
     * @return true if playing, else false
     */
    public static boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    /**
     * Sets the mediaPlayer to play at a defined position in the song
     * @param pos Position to be played at (in seconds)
     */
    public static void seekTo(int pos) {
        if (mediaPlayer != null)
            mediaPlayer.seekTo(pos);
    }

    /**
     * Gets the position the mediaPlayer is currently at (in seconds)
     * @return The position the player is at right now or -1 if an error occurs
     */
    public static int getPosition() {
        if (mediaPlayer == null) {
            Timber.e("Couldn't get mediaPlayer position");
            return -1;
        }
        return mediaPlayer.getCurrentPosition();
    }

    /**
     * Gets the duration of the song that is being played right now ( in seconds)
     * @return The duration of the song or -1 if an error occurs
     */
    public static int getDuration() {
        if (mediaPlayer == null) {
            Timber.e("Couldn't get duration of current song from mediaPlayer");
            return -1;
        }
        return mediaPlayer.getDuration();
    }

    /**
     * Gets the AudioSessionID of the mediaPlayer
     * @return the ID or 0 if an error occurs
     */
    public static int getAudioSessionId() {
        if (mediaPlayer == null) {
            Timber.e("Couldn't get current mediaPlayer AudioSessionId");
            return 0;
        }
        return mediaPlayer.getAudioSessionId();
    }

    /**
     * Converts the milliseconds in a displayable time-format like this --> min:sec
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
