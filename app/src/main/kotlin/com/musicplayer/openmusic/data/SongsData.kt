package com.musicplayer.openmusic.data

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import androidx.preference.PreferenceManager
import androidx.room.Room.databaseBuilder
import com.musicplayer.openmusic.OpenMusicApp
import com.musicplayer.openmusic.data.base.AppDatabase
import com.musicplayer.openmusic.data.base.PlaylistSong
import com.musicplayer.openmusic.utils.PathUtils.getPathDown
import com.musicplayer.openmusic.utils.PathUtils.isSubDirOfAny
import com.musicplayer.musicplayer.R
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class SongsData private constructor(context: Context) {
    private val database: AppDatabase

    @Volatile
    private var allSongs: ArrayList<Song>? = null
    var allPlaylists: ArrayList<Playlist>? = null
    var allAlbums: ArrayList<Album>? = null
    private var playingQueue: ArrayList<Song>? = null
    private var originalQueue: ArrayList<Song>? = null
    private var playingQueueIndex = 0
    /**
     * Checks if the player is in repeat mode or not
     *
     * @return True if the song should be repeated, else false
     */
    /**
     * Changes the repeat state of a song, sets if the song should be repeated or not
     */
    var isRepeat = false
    /**
     * Checks if the player is in shuffle mode or not
     *
     * @return True if in shuffle mode, else false
     */// resets queue to original
    //find where the song that was playing randomly was in the original queue
// Create random queue
    /**
     * Changes the shuffle mode of the player
     */
    var isShuffle = false
        set(shuffle) {
            field = shuffle
            if (shuffle) {
                // Create random queue
                setRandomQueue()
            } else {
                // resets queue to original
                val songPlaying = songPlaying
                playingQueue = originalQueue
                //find where the song that was playing randomly was in the original queue
                for (i in playingQueue!!.indices) {
                    if (playingQueue!![i] == songPlaying) {
                        playingQueueIndex = i
                        break
                    }
                }
            }
        }
    var isDoneLoading = false

    /**
     * SongsData custom constructor.
     * When created automatically reloads all songs and creates the playingQueue
     */
    init {
        playingQueue = ArrayList()
        database =
            databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME).build()
    }

    /**
     * Method to retrieve the song that is being played
     *
     * @return Song at which the playingQueueIndex points at
     */
    val songPlaying: Song
        get() = playingQueue!![playingQueueIndex]

    /**
     * Plays the next song by grabbing the next song in the playingQueueIndex
     * WARNING: Might cause ArrayOutOfBounds Exception!
     *
     * @return the next Song that will be played
     */
    fun playNext(): Song {
        playingIndex = playingQueueIndex + 1
        return songPlaying
    }

    /**
     * Plays the previous song by grabbing the previous song in the playingQueueIndex
     * WARNING: Might cause ArrayOutOfBounds Exception if Index is 0
     *
     * @return the previous song that will be played
     */
    fun playPrev(): Song {
        playingIndex = playingQueueIndex - 1
        return songPlaying
    }

    /**
     * Clears the previous Queue and creates a new one with all songs and starts playing from
     * a specific index/position
     *
     * @param position the index from where the player should start playing
     */
    fun playAllFrom(position: Int) {
        setPlayingQueue(allSongs!!, position)
    }

    fun playPlaylistFrom(playlist: Playlist, position: Int) {
        setPlayingQueue(playlist.songList, position)
    }

    private fun setPlayingQueue(newPlayingQueue: ArrayList<Song>, position: Int) {
        playingQueue = ArrayList()
        playingQueue!!.addAll(newPlayingQueue)
        playingQueueIndex = position
        originalQueue = playingQueue
    }

    /**
     * Adds a song at the end of the Queue
     *
     * @param song the song to be added
     */
    fun addToQueue(song: Song) {
        playingQueue!!.add(song)
    }

    /**
     * Adds a song to a specific position/index to the queue
     *
     * @param position index to where the song should be placed
     */
    fun addToQueue(position: Int) {
        playingQueue!!.add(allSongs!![position])
    }

    /**
     * Loads all songs from the internal memory of the phone and overwrites/creates the allSongs list
     * This excludes SD-cards, USB, etc...
     */
    fun loadFromDatabase(context: Context): Thread {
        val loadThread = Thread({


            //get all songs from database
            allSongs = database.songDao().all
            allAlbums = database.albumDao().all

            //lazy solution to querying into songList problem
            for (album in allAlbums!!) album.setSongList(
                database.albumDao().getSongs(album.id.toString())
            )

            //get all playlists from database
            allPlaylists = database.playlistDao().all

            //lazy solution to querying into songList problem
            for (playlist in allPlaylists!!) playlist.songList =
                database.playlistDao().getSongs(playlist.id.toString())
            if (allPlaylists!!.isEmpty()) {
                val favorites = Playlist(
                    FAVORITES_PLAYLIST_ID,
                    context.getString(R.string.all_playlist_favorites_name)
                )
                allPlaylists!!.add(favorites)
                database.playlistDao().insert(favorites)
            }
        }, "loadFromDatabaseThread")
        loadThread.start()
        return loadThread
    }

    fun <T> loadFromFiles(activity: T): Thread where T : Activity, T : LoadListener {
        isDoneLoading = false
        val loadThread = Thread({
            var removedSongs: Boolean
            var addedSongs: Boolean
            var addedAlbums: Boolean
            var changedAlbums: Boolean
            var removedAlbums: Boolean
            removedAlbums = false
            changedAlbums = removedAlbums
            addedAlbums = changedAlbums
            addedSongs = addedAlbums
            removedSongs = addedSongs
            //get the saved paths from prefs
            val savedPaths = HashSet(
                PreferenceManager.getDefaultSharedPreferences(activity)
                    .getStringSet(
                        OpenMusicApp.PREFS_KEY_LIBRARY_PATHS,
                        OpenMusicApp.defaultPathsSet
                    )!!
            )
            val songPaths = HashSet<String>()
            //remove any missing songs or songs no longer in the library paths
            run {
                var i = 0
                while (i < allSongs!!.size) {
                    val song = allSongs!![i]
                    val file = song.file
                    if (!file.exists() || !file.canRead() || !isSubDirOfAny(
                            file.absolutePath,
                            savedPaths
                        )
                    ) {
                        allSongs!!.remove(song)
                        database.songDao().delete(song)
                        database.playlistSongDao().removeAllSongRefs(song.songId.toString())
                        i--
                        removedSongs = true
                    } else songPaths.add(song.path)
                    i++
                }
            }
            if (removedSongs) activity.runOnUiThread { activity.onRemovedSongs() }
            //find all songs in storage
            for (path in savedPaths) {
                val file = File(path)
                if (!file.exists() || !file.canRead()) continue
                //check if song is already in the database
                val songsLoaded = loadSongs(file)
                for (newSong in songsLoaded) {
                    if (!songPaths.contains(newSong.path)) {
                        database.songDao().insert(newSong)
                        allSongs!!.add(newSong)
                        addedSongs = true
                    }
                }
            }
            if (addedSongs) activity.runOnUiThread { activity.onAddedSongs() }
            val artFolder = File(
                getPathDown(
                    activity.filesDir.absolutePath,
                    OpenMusicApp.APP_FOLDER_ALBUMS_ART
                )
            )
            if (!artFolder.exists()) artFolder.mkdir()
            //add all albums from songs found
            for (song in allSongs!!) {
                val albumTitle = song.extractAlbumTitle()
                var album: Album? = null
                for (a in allAlbums!!) {
                    if (a.title == albumTitle) {
                        album = a
                        break
                    }
                }
                if (album == null) {
                    album = Album(UUID.randomUUID(), albumTitle, null)
                    //save album art
                    val artPath = getPathDown(
                        artFolder.absolutePath,
                        album.id.toString()
                    )
                    try {
                        FileOutputStream(artPath).use { out ->
                            val albumArt = song.extractAlbumArt()
                            albumArt.compress(Bitmap.CompressFormat.PNG, 100, out)
                            album.artPath = artPath
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    allAlbums!!.add(album)
                    database.albumDao().insert(album)
                    addedAlbums = true
                }
                database.songDao().setAlbum(song.songId.toString(), album.id.toString())
                if (!album.containsSong(song)) album.addSong(song) else song.album = album
            }
            if (addedAlbums) activity.runOnUiThread { activity.onAddedAlbums() }
            var i = 0
            while (i < allAlbums!!.size) {
                val album = allAlbums!![i]
                var j = 0
                while (j < album.getSongList()!!.size) {
                    val song = album.getSongList()!![j]
                    if (!song.file.exists() || !song.file.canRead() || !isSubDirOfAny(
                            song.path,
                            savedPaths
                        )
                    ) {
                        album.getSongList()!!.removeAt(j)
                        song.album = null
                        j--
                        changedAlbums = true
                    }
                    j++
                }
                if (album.isEmpty) {
                    database.albumDao().delete(album)
                    allAlbums!!.removeAt(i)
                    i--
                    removedAlbums = true
                }
                i++
            }
            if (changedAlbums || removedAlbums) activity.runOnUiThread { activity.onRemovedAlbums() }
            Timber.i(
                "Done loading from files - removedSongs:%s, addedSongs: %s, addedAlbums: %s, changedAlbums: %s, removedAlbums: %s",
                removedSongs, addedSongs, addedAlbums, changedAlbums, removedAlbums
            )
            activity.runOnUiThread { activity.onLoadComplete() }
        }, "loadFromDatabaseThread")
        loadThread.start()
        return loadThread
    }

    /**
     * Searches for .mp3 and .wav files in the given directory. If it finds directories on its way
     * it automatically searches in them too recursively. The songs it founds get added to an array.
     * Hidden files will be ignored.
     *
     * @param dir Directory in which the songs should be searched
     * @return Array with songs
     */
    private fun loadSongs(dir: File): ArrayList<Song> {
        // All files in given directory
        val files = dir.listFiles()
        // Array that stores all songs
        val songsFound = ArrayList<Song>()
        if (files != null) {
            // All files in the directory
            for (singlefile in files) {
                // If its a directory, the method gets called again with that directory as parameter
                if (singlefile.isDirectory && !singlefile.isHidden) {
                    songsFound.addAll(loadSongs(singlefile))
                } else {
                    // Convert file to song and add it to the array
                    // Arrays.stream(items).anyMatch(inputString::contains)
                    // singlefile.getName().endsWith(".mp3") || singlefile.getName().endsWith(".wav")
                    // Checks if the file extension is one of the supported file formats
                    if (Arrays.stream(SUPPORTED_FORMATS).parallel().anyMatch { s: String? ->
                            getFileExtension(singlefile).contains(
                                s!!
                            )
                        }) {
                        songsFound.add(Song(UUID.randomUUID(), singlefile))
                    }
                }
            }
        }
        return songsFound
    }

    /**
     * This method is needed in the loadSong method to get only the
     * File extension and therefore identify the type of songs
     * accurately
     *
     * @param file File to check
     * @return Nothing if it has no extension, else the whole extension (with dot)
     */
    private fun getFileExtension(file: File): String {
        val name = file.name
        val lastIndexOf = name.lastIndexOf(".")
        return if (lastIndexOf == -1) {
            "" // empty extension
        } else name.substring(lastIndexOf)
    }

    val playingQueueCount: Int
        get() = playingQueue!!.size

    /**
     * Checks if song at a given position/index exists
     *
     * @param position The index to check for the song's existence
     * @return True if the song exists, else false
     */
    fun songExists(position: Int): Boolean {
        return try {
            !allSongs!![position].file.exists()
        } catch (ex: IndexOutOfBoundsException) {
            true
        }
    }

    /**
     * Returns the song at a given position/index
     *
     * @param position The index to retrieve the song from
     * @return The song at the given index
     */
    fun getSongAt(position: Int): Song {
        return allSongs!![position]
    }

    fun getSongFromQueueAt(position: Int): Song {
        return playingQueue!![position]
    }

    /**
     * Returns the amount of songs that are in the Array of songs right now
     *
     * @return The size of the Array containing all the songs
     */
    fun songsCount(): Int {
        return allSongs!!.size
    }

    /**
     * Checks if the index is currently at the last position in the array
     *
     * @return True if it is the last song in the array, else false
     */
    fun lastInQueue(): Boolean {
        return playingQueueIndex == playingQueue!!.size - 1
    }

    /**
     * Checks if the index is currently at the first position in the array
     *
     * @return True if it is the first song, else false
     */
    fun firstInQueue(): Boolean {
        return playingQueueIndex == 0
    }

    fun getPlayingQueue(): ArrayList<Song>? {
        return playingQueue
    }

    fun onQueueReordered(from: Int, to: Int) {
        if (playingQueueIndex == from) playingQueueIndex = to else {
            if (from < to && playingQueueIndex > from && playingQueueIndex <= to)
                playingQueueIndex--
            else if (from > to && playingQueueIndex < from && playingQueueIndex >= to)
                playingQueueIndex++
        }
    }
    /**
     * Gets the index of the currently playing song
     *
     * @return The index of the currently playing song
     */
    /**
     * Sets the playingQueueIndex to a specific index
     */
    var playingIndex: Int
        get() = playingQueueIndex
        set(playingIndex) {
            playingQueueIndex = playingIndex
            if (playingQueueIndex < 0 || playingQueueIndex > playingQueue!!.size - 1 && isRepeat)
                playingQueueIndex = 0
        }

    /**
     * Overried the current playingQueue and creates a random queue
     */
    private fun setRandomQueue() {
        // Temporary Arraylist to store all the songs
        val temporary = ArrayList<Song>()
        val r = Random()
        // Avoids getting double random numbers
        // Example: The number 5 only gets called once
        val s: MutableSet<Int> = HashSet()
        // Adds the currently playing song, we dont want to shuffle this one too
        temporary.add(songPlaying)
        // Creates a random queue
        for (i in 0 until playingQueue!!.size - 1) {
            while (true) {
                val num = r.nextInt(playingQueue!!.size)
                // leaves out currently playing song
                if (!s.contains(num) && num != playingQueueIndex) {
                    s.add(num)
                    temporary.add(getSongFromQueueAt(num))
                    break
                }
            }
        }
        // replaces queue
        playingQueue = temporary
        playingQueueIndex = 0
    }

    fun getAllSongs(): List<Song>? {
        //return list of all songs
        return allSongs
    }

    val favoritesPlaylist: Playlist?
        get() {
            for (playlist in allPlaylists!!) if (playlist.isFavorites) return playlist
            return null
        }

    fun isFavorited(song: Song): Boolean {
        return favoritesPlaylist!!.contains(song)
    }

    fun insertToPlaylist(playlist: Playlist, song: Song) {
        playlist.addSong(song)
        Thread {
            database.playlistSongDao()
                .insert(PlaylistSong(playlist.id, song.songId, playlist.songCount - 1))
        }
            .start()
    }

    fun removeFromPlaylist(playlist: Playlist, song: Song, index: Int, all: Boolean) {
        if (!playlist.contains(song)) return
        if (!all) playlist.removeSongAt(index) else {
            while (playlist.contains(song)) playlist.removeSong(song)
        }
        Thread {
            database.playlistSongDao()
                .removeSongFromPlaylist(playlist.id.toString(), song.songId.toString(), index, all)
        }
            .start()
    }

    fun insertPlaylist(newPlaylist: Playlist) {
        allPlaylists?.add(newPlaylist)
        Thread { database.playlistDao().insert(newPlaylist) }.start()
    }

    fun deletePlaylist(index: Int) {
        if (index >= allPlaylists!!.size) return
        val playlistToDelete: Playlist = allPlaylists!!.removeAt(index)
        Thread {
            database.playlistDao().delete(playlistToDelete)
            database.playlistSongDao().deletePlaylist(playlistToDelete.id.toString())
        }.start()
    }

    fun playAlbumFrom(album: Album, position: Int) {
        setPlayingQueue(album.getSongList()!!, position)
    }

    fun getPlaylistsOfSong(song: Song): String {
        val ret = StringBuilder()
        for (i in allPlaylists!!.indices) {
            if (allPlaylists!![i].contains(song)) {
                ret.append(allPlaylists!![i].name).append(", ")
            }
        }
        return ret.toString()
    }

    interface LoadListener {
        fun onRemovedSongs()
        fun onAddedSongs()
        fun onAddedAlbums()
        fun onRemovedAlbums()
        fun onLoadComplete()
    }

    companion object {
        // Contains a list of all supported extensions.
        // Especially concerned about .ts as it is not seekable and might break the app
        val SUPPORTED_FORMATS = arrayOf(
            ".mp3",
            ".wav",
            ".ogg",
            ".3gp",
            ".mp4",
            ".m4a",
            ".aac",
            ".ts",
            ".amr",
            ".flac",
            ".mid",
            ".xmf",
            ".mxmf",
            ".rtttl",
            ".rtx",
            ".ota",
            ".imy",
            ".mkv"
        )
        val FAVORITES_PLAYLIST_ID: UUID = UUID.fromString("3a47e9a7-7cb6-4b47-8b98-7ee7d4b865f0")

        @JvmField
        var data: SongsData? = null

        /**
         * Creates an instance of the class if it doesn't already exist
         *
         * @return the new created class
         */
        @JvmStatic
        fun getInstance(context: Context): SongsData? {
            if (data == null) data = SongsData(context)
            return data
        }
    }
}