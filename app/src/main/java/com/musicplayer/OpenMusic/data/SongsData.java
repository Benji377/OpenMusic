package com.musicplayer.OpenMusic.data;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import com.musicplayer.OpenMusic.OpenMusicApp;
import com.musicplayer.OpenMusic.data.base.AppDatabase;
import com.musicplayer.OpenMusic.data.base.PlaylistSong;
import com.musicplayer.OpenMusic.utils.PathUtils;
import com.musicplayer.musicplayer.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import timber.log.Timber;


public class SongsData {
    // Contains a list of all supported extensions.
    // Especially concerned about .ts as it is not seekable and might break the app
    public static final String[] SUPPORTED_FORMATS = {".mp3", ".wav", ".ogg", ".3gp", ".mp4", ".m4a",
            ".aac", ".ts", ".amr", ".flac", ".mid", ".xmf", ".mxmf", ".rtttl", ".rtx", ".ota", ".imy", ".mkv"};
    public static final UUID FAVORITES_PLAYLIST_ID = UUID.fromString("3a47e9a7-7cb6-4b47-8b98-7ee7d4b865f0");
    public static SongsData data;
    private final AppDatabase database;
    private volatile ArrayList<Song> allSongs;
    private List<Playlist> allPlaylists;
    private List<Album> allAlbums;
    private ArrayList<Song> playingQueue;
    private ArrayList<Song> originalQueue;
    private int playingQueueIndex;
    private boolean repeat;
    private boolean shuffle;
    private boolean doneLoading;

    /**
     * SongsData custom constructor.
     * When created automatically reloads all songs and creates the playingQueue
     */
    private SongsData(@NonNull Context context) {
        playingQueue = new ArrayList<>();
        database = Room.databaseBuilder(context, AppDatabase.class, AppDatabase.DATABASE_NAME).build();
    }

    /**
     * Creates an instance of the class if it doesn't already exist
     *
     * @return the new created class
     */
    public static SongsData getInstance(Context context) {
        if (data == null)
            data = new SongsData(context);
        return data;
    }

    /**
     * Method to retrieve the song that is being played
     *
     * @return Song at which the playingQueueIndex points at
     */
    public Song getSongPlaying() {
        return playingQueue.get(playingQueueIndex);
    }

    /**
     * Plays the next song by grabbing the next song in the playingQueueIndex
     * WARNING: Might cause ArrayOutOfBounds Exception!
     *
     * @return the next Song that will be played
     */
    public Song playNext() {
        setPlayingIndex(playingQueueIndex + 1);
        return getSongPlaying();
    }

    /**
     * Plays the previous song by grabbing the previous song in the playingQueueIndex
     * WARNING: Might cause ArrayOutOfBounds Exception if Index is 0
     *
     * @return the previous song that will be played
     */
    public Song playPrev() {
        setPlayingIndex(playingQueueIndex - 1);
        return getSongPlaying();
    }

    /**
     * Clears the previous Queue and creates a new one with all songs and starts playing from
     * a specific index/position
     *
     * @param position the index from where the player should start playing
     */
    public void playAllFrom(int position) {
        setPlayingQueue(allSongs, position);
    }

    public void playPlaylistFrom(Playlist playlist, int position) {
        setPlayingQueue(playlist.getSongList(), position);
    }

    private void setPlayingQueue(ArrayList<Song> newPlayingQueue, int position) {
        playingQueue = new ArrayList<>();
        playingQueue.addAll(newPlayingQueue);

        playingQueueIndex = position;
        originalQueue = playingQueue;
    }

    /**
     * Adds a song at the end of the Queue
     *
     * @param song the song to be added
     */
    public void addToQueue(Song song) {
        playingQueue.add(song);
    }

    /**
     * Adds a song to a specific position/index to the queue
     *
     * @param position index to where the song should be placed
     */
    public void addToQueue(int position) {
        playingQueue.add(allSongs.get(position));
    }

    /**
     * Loads all songs from the internal memory of the phone and overwrites/creates the allSongs list
     * This excludes SD-cards, USB, etc...
     */
    public Thread loadFromDatabase(Context context) {
        Thread loadThread = new Thread(() -> {

            //get all songs from database
            allSongs = (ArrayList<Song>) database.songDao().getAll();

            allAlbums = database.albumDao().getAll();

            //lazy solution to querying into songList problem
            for (Album album : allAlbums)
                album.setSongList((ArrayList<Song>) database.albumDao().getSongs(album.getId().toString()));

            //get all playlists from database
            allPlaylists = database.playlistDao().getAll();

            //lazy solution to querying into songList problem
            for (Playlist playlist : allPlaylists)
                playlist.setSongList((ArrayList<Song>) database.playlistDao().getSongs(playlist.getId().toString()));

            if (allPlaylists.size() == 0) {
                Playlist favorites = new Playlist(FAVORITES_PLAYLIST_ID, context.getString(R.string.all_playlist_favorites_name));
                allPlaylists.add(favorites);
                database.playlistDao().insert(favorites);
            }

        }, "loadFromDatabaseThread");
        loadThread.start();
        return loadThread;

    }

    public <T extends Activity & LoadListener> Thread loadFromFiles(T activity) {
        doneLoading = false;
        Thread loadThread = new Thread(() -> {
            boolean removedSongs, addedSongs, addedAlbums, changedAlbums, removedAlbums;
            removedSongs = addedSongs = addedAlbums = changedAlbums = removedAlbums = false;
            //get the saved paths from prefs
            HashSet<String> savedPaths = new HashSet<>(
                    PreferenceManager.getDefaultSharedPreferences(activity)
                            .getStringSet(OpenMusicApp.PREFS_KEY_LIBRARY_PATHS,
                                    OpenMusicApp.defaultPathsSet));

            HashSet<String> songPaths = new HashSet<>();
            //remove any missing songs or songs no longer in the library paths
            for (int i = 0; i < allSongs.size(); i++) {
                Song song = allSongs.get(i);
                File file = song.getFile();
                if (!file.exists() || !file.canRead() || !PathUtils.isSubDirOfAny(file.getAbsolutePath(), savedPaths)) {
                    allSongs.remove(song);
                    database.songDao().delete(song);
                    database.playlistSongDao().removeAllSongRefs(song.getSongId().toString());
                    i--;
                    removedSongs = true;
                } else
                    songPaths.add(song.getPath());
            }
            if (removedSongs)
                activity.runOnUiThread(activity::onRemovedSongs);
            //find all songs in storage
            for (String path : savedPaths) {
                File file = new File(path);
                if (!file.exists() || !file.canRead())
                    continue;
                //check if song is already in the database
                ArrayList<Song> songsLoaded = loadSongs(file);
                for (Song newSong : songsLoaded) {
                    if (!songPaths.contains(newSong.getPath())) {
                        database.songDao().insert(newSong);
                        allSongs.add(newSong);
                        addedSongs = true;
                    }
                }
            }

            if (addedSongs)
                activity.runOnUiThread(activity::onAddedSongs);

            File artFolder = new File(PathUtils.getPathDown(
                    activity.getFilesDir().getAbsolutePath(),
                    OpenMusicApp.APP_FOLDER_ALBUMS_ART));
            if (!artFolder.exists())
                artFolder.mkdir();
            //add all albums from songs found
            for (Song song : allSongs) {
                String albumTitle = song.extractAlbumTitle();
                if (albumTitle == null)
                    albumTitle = song.getFolderName();
                Album album = null;
                for (Album a : allAlbums) {
                    if (a.getTitle().equals(albumTitle)) {
                        album = a;
                        break;
                    }
                }
                if (album == null) {
                    album = new Album(UUID.randomUUID(), albumTitle, null);
                    //save album art
                    String artPath = PathUtils.getPathDown(
                            artFolder.getAbsolutePath(),
                            album.getId().toString());
                    try (FileOutputStream out = new FileOutputStream(artPath)) {
                        Bitmap albumArt = song.extractAlbumArt();
                        if (albumArt != null) {
                            albumArt.compress(Bitmap.CompressFormat.PNG, 100, out);
                            album.setArtPath(artPath);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    allAlbums.add(album);
                    database.albumDao().insert(album);
                    addedAlbums = true;
                }
                database.songDao().setAlbum(song.getSongId().toString(), album.getId().toString());
                if (!album.containsSong(song))
                    album.addSong(song);
                else
                    song.setAlbum(album);
            }
            if (addedAlbums)
                activity.runOnUiThread(activity::onAddedAlbums);
            for (int i = 0; i < allAlbums.size(); i++) {
                Album album = allAlbums.get(i);
                for (int j = 0; j < album.getSongList().size(); j++) {
                    Song song = album.getSongList().get(j);
                    if (!song.getFile().exists() || !song.getFile().canRead() || !PathUtils.isSubDirOfAny(song.getPath(), savedPaths)) {
                        album.getSongList().remove(j);
                        song.setAlbum(null);
                        j--;
                        changedAlbums = true;
                    }
                }
                if (album.isEmpty()) {
                    database.albumDao().delete(album);
                    allAlbums.remove(i);
                    i--;
                    removedAlbums = true;
                }
            }
            if (changedAlbums || removedAlbums)
                activity.runOnUiThread(activity::onRemovedAlbums);
            Timber.i("Done loading from files - removedSongs:%s, addedSongs: %s, addedAlbums: %s, changedAlbums: %s, removedAlbums: %s",
                    removedSongs, addedSongs, addedAlbums, changedAlbums, removedAlbums);
            activity.runOnUiThread(activity::onLoadComplete);
        }, "loadFromDatabaseThread");
        loadThread.start();
        return loadThread;
    }

    /**
     * Searches for .mp3 and .wav files in the given directory. If it finds directories on its way
     * it automatically searches in them too recursively. The songs it founds get added to an array.
     * Hidden files will be ignored.
     *
     * @param dir Directory in which the songs should be searched
     * @return Array with songs
     */
    public ArrayList<Song> loadSongs(File dir) {
        // All files in given directory
        File[] files = dir.listFiles();
        // Array that stores all songs
        ArrayList<Song> songsFound = new ArrayList<>();
        if (files != null) {
            // All files in the directory
            for (File singlefile : files) {
                // If its a directory, the method gets called again with that directory as parameter
                if (singlefile.isDirectory() && !singlefile.isHidden()) {
                    songsFound.addAll(loadSongs(singlefile));
                } else {
                    // Convert file to song and add it to the array
                    // Arrays.stream(items).anyMatch(inputString::contains)
                    // singlefile.getName().endsWith(".mp3") || singlefile.getName().endsWith(".wav")
                    // Checks if the file extension is one of the supported file formats
                    if (Arrays.stream(SUPPORTED_FORMATS).parallel().anyMatch(getFileExtension(singlefile)::contains)) {
                        songsFound.add(new Song(UUID.randomUUID(), singlefile));
                    }
                }
            }
        }
        return songsFound;
    }

    /**
     * This method is needed in the loadSong method to get only the
     * File extension and therefore identify the type of songs
     * accurately
     *
     * @param file File to check
     * @return Nothing if it has no extension, else the whole extension (with dot)
     */
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf);
    }

    public int getPlayingQueueCount() {
        return playingQueue.size();
    }

    /**
     * Checks if song at a given position/index exists
     *
     * @param position The index to check for the song's existence
     * @return True if the song exists, else false
     */
    public boolean songExists(int position) {
        try {
            return !allSongs.get(position).getFile().exists();
        } catch (IndexOutOfBoundsException ex) {
            return true;
        }
    }

    /**
     * Returns the song at a given position/index
     *
     * @param position The index to retrieve the song from
     * @return The song at the given index
     */
    public Song getSongAt(int position) {
        return allSongs.get(position);
    }

    public Song getSongFromQueueAt(int position) {
        return playingQueue.get(position);
    }

    /**
     * Returns the amount of songs that are in the Array of songs right now
     *
     * @return The size of the Array containing all the songs
     */
    public int songsCount() {
        return allSongs.size();
    }

    /**
     * Checks if the player is in repeat mode or not
     *
     * @return True if the song should be repeated, else false
     */
    public boolean isRepeat() {
        return repeat;
    }

    /**
     * Changes the repeat state of a song, sets if the song should be repeated or not
     *
     * @param repeat True if the song should be repeated, or false if not
     */
    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    /**
     * Checks if the player is in shuffle mode or not
     *
     * @return True if in shuffle mode, else false
     */
    public boolean isShuffle() {
        return shuffle;
    }

    /**
     * Changes the shuffle mode of the player
     *
     * @param shuffle True if shuffle mode should be activated
     */
    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
        if (shuffle) {
            // Create random queue
            setRandomQueue();
        } else {
            // resets queue to original
            Song songPlaying = getSongPlaying();
            playingQueue = originalQueue;
            //find where the song that was playing randomly was in the original queue
            for (int i = 0; i < playingQueue.size(); i++) {
                if (playingQueue.get(i).equals(songPlaying)) {
                    playingQueueIndex = i;
                    break;
                }
            }

        }
    }

    /**
     * Checks if the index is currently at the last position in the array
     *
     * @return True if it is the last song in the array, else false
     */
    public boolean lastInQueue() {
        return playingQueueIndex == playingQueue.size() - 1;
    }

    /**
     * Checks if the index is currently at the first position in the array
     *
     * @return True if it is the first song, else false
     */
    public boolean firstInQueue() {
        return playingQueueIndex == 0;
    }

    public List<Song> getPlayingQueue() {
        return playingQueue;
    }

    public void onQueueReordered(int from, int to) {
        if (playingQueueIndex == from)
            playingQueueIndex = to;
        else {
            if (from < to && playingQueueIndex > from && playingQueueIndex <= to)
                playingQueueIndex--;
            else if (from > to && playingQueueIndex < from && playingQueueIndex >= to)
                playingQueueIndex++;
        }
    }

    /**
     * Gets the index of the currently playing song
     *
     * @return The index of the currently playing song
     */
    public int getPlayingIndex() {
        return playingQueueIndex;
    }

    /**
     * Sets the playingQueueIndex to a specific index
     *
     * @param playingIndex the index of the song
     */
    public void setPlayingIndex(int playingIndex) {
        playingQueueIndex = playingIndex;
        if (playingQueueIndex < 0 || playingQueueIndex > playingQueue.size() - 1 && repeat)
            playingQueueIndex = 0;
    }

    /**
     * Overried the current playingQueue and creates a random queue
     */
    public void setRandomQueue() {
        // Temporary Arraylist to store all the songs
        ArrayList<Song> temporary = new ArrayList<>();
        final Random r = new Random();
        // Avoids getting double random numbers
        // Example: The number 5 only gets called once
        final Set<Integer> s = new HashSet<>();
        // Adds the currently playing song, we dont want to shuffle this one too
        temporary.add(getSongPlaying());
        // Creates a random queue
        for (int i = 0; i < playingQueue.size() - 1; i++) {
            while (true) {
                int num = r.nextInt(playingQueue.size());
                // leaves out currently playing song
                if (!s.contains(num) && num != playingQueueIndex) {
                    s.add(num);
                    temporary.add(getSongFromQueueAt(num));
                    break;
                }
            }
        }
        // replaces queue
        playingQueue = temporary;
        playingQueueIndex = 0;
    }

    public List<Song> getAllSongs() {
        //return list of all songs
        return allSongs;
    }

    public List<Playlist> getAllPlaylists() {
        return allPlaylists;
    }

    public Playlist getFavoritesPlaylist() {
        for (Playlist playlist : allPlaylists)
            if (playlist.isFavorites())
                return playlist;
        return null;
    }

    public boolean isFavorited(Song song) {
        return getFavoritesPlaylist().contains(song);
    }

    public void insertToPlaylist(Playlist playlist, Song song) {
        playlist.addSong(song);
        new Thread(() -> database.playlistSongDao().insert(new PlaylistSong(playlist.getId(), song.getSongId(), playlist.getSongCount() - 1))).start();
    }

    public void removeFromPlaylist(Playlist playlist, Song song, int index, boolean all) {
        if (!playlist.contains(song))
            return;
        if (!all)
            playlist.removeSongAt(index);
        else {
            while (playlist.contains(song))
                playlist.removeSong(song);
        }
        new Thread(() -> database.playlistSongDao().removeSongFromPlaylist(playlist.getId().toString(), song.getSongId().toString(), index, all)).start();
    }

    public void insertPlaylist(Playlist newPlaylist) {
        allPlaylists.add(newPlaylist);
        new Thread(() -> database.playlistDao().insert(newPlaylist)).start();
    }

    public void deletePlaylist(int index) {
        if (index >= allPlaylists.size())
            return;
        Playlist playlistToDelete = allPlaylists.remove(index);
        new Thread(() -> {
            database.playlistDao().delete(playlistToDelete);
            database.playlistSongDao().deletePlaylist(playlistToDelete.getId().toString());
        }).start();
    }

    public List<Album> getAllAlbums() {
        return allAlbums;
    }

    public boolean isDoneLoading() {
        return doneLoading;
    }

    public void setDoneLoading(boolean doneLoading) {
        this.doneLoading = doneLoading;
    }

    public void playAlbumFrom(Album album, int position) {
        setPlayingQueue(album.getSongList(), position);
    }

    public String getPlaylistsOfSong(Song song) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < getAllPlaylists().size(); i++) {
            if (getAllPlaylists().get(i).contains(song)) {
                ret.append(getAllPlaylists().get(i).getName()).append(", ");
            }
        }
        return ret.toString();
    }

    public interface LoadListener {
        void onRemovedSongs();

        void onAddedSongs();

        void onAddedAlbums();

        void onRemovedAlbums();

        void onLoadComplete();
    }
}
