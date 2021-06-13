package com.example.SocyMusic;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

public class MediaPlayerUtil {
    private static MediaPlayer mediaPlayer;

    public static boolean startPlaying(Context context, Song song) {
        Uri uri = Uri.fromFile(song.getFile());
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(context, uri);
        if (mediaPlayer == null)
            return false;
        mediaPlayer.setOnCompletionListener(mp -> {
            playNext(context);
            if (context instanceof MainActivity)
                ((MainActivity) context).onSongUpdate();
        });
        mediaPlayer.start();
        return true;
    }

    public static void playNext(Context context) {
        SongsData songsData = SongsData.getInstance();
        if (songsData.isRepeat()) {
            SongsData.getInstance().setPlaying(songsData.currentSongIndex());
        } else if (songsData.lastInQueue() && !songsData.isRepeat()) {
            SongsData.getInstance().setPlaying(0);
        } else {
            SongsData.getInstance().playNext();
        }
        startPlaying(context, SongsData.getInstance().getSongPlaying());
    }

    public static void playPrev(Context context) {
        SongsData songsData = SongsData.getInstance();
        if (songsData.isRepeat()) {
            SongsData.getInstance().setPlaying(songsData.currentSongIndex());
        } else if (songsData.firstInQueue() && !songsData.isRepeat()) {
            SongsData.getInstance().setPlaying(SongsData.getInstance().songsCount() - 1);
        } else {
            SongsData.getInstance().playPrev();
        }
        startPlaying(context, SongsData.getInstance().getSongPlaying());
    }

    public static void togglePlayPause() {
        if (mediaPlayer == null)
            return;
        if (mediaPlayer.isPlaying())
            mediaPlayer.pause();
        else
            mediaPlayer.start();
    }

    public static void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
        }
    }


    public static boolean isStopped() {
        return mediaPlayer == null;
    }

    public static boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public static void seekTo(int pos) {
        if (mediaPlayer != null)
            mediaPlayer.seekTo(pos);
    }

    public static int getPosition() {
        if (mediaPlayer == null)
            return -1;
        return mediaPlayer.getCurrentPosition();
    }

    public static int getDuration() {
        if (mediaPlayer == null)
            return -1;
        return mediaPlayer.getDuration();
    }

    public static int getAudioSessionId() {
        if (mediaPlayer == null)
            return 0;
        return mediaPlayer.getAudioSessionId();
    }

}
