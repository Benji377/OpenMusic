package com.musicplayer.openmusic.ui.player

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.gauravk.audiovisualizer.visualizer.BarVisualizer
import com.musicplayer.musicplayer.R
import com.musicplayer.openmusic.MediaPlayerUtil
import com.musicplayer.openmusic.MediaPlayerUtil.audioSessionId
import com.musicplayer.openmusic.MediaPlayerUtil.createTime
import com.musicplayer.openmusic.MediaPlayerUtil.duration
import com.musicplayer.openmusic.MediaPlayerUtil.isPlaying
import com.musicplayer.openmusic.MediaPlayerUtil.isStopped
import com.musicplayer.openmusic.MediaPlayerUtil.playCurrent
import com.musicplayer.openmusic.MediaPlayerUtil.playNext
import com.musicplayer.openmusic.MediaPlayerUtil.playPrev
import com.musicplayer.openmusic.MediaPlayerUtil.position
import com.musicplayer.openmusic.MediaPlayerUtil.seekTo
import com.musicplayer.openmusic.MediaPlayerUtil.startPlaying
import com.musicplayer.openmusic.custom_views.CustomViewPager2
import com.musicplayer.openmusic.data.Playlist
import com.musicplayer.openmusic.data.Song
import com.musicplayer.openmusic.data.SongsData
import com.musicplayer.openmusic.data.SongsData.Companion.getInstance
import com.musicplayer.openmusic.utils.DialogUtils.OnNewPlaylistCallback
import com.musicplayer.openmusic.utils.DialogUtils.OnPlaylistUpdateCallback
import com.musicplayer.openmusic.utils.DialogUtils.showAddToPlaylistDialog

class PlayerFragment : Fragment() {
    private var actionBar: ActionBar? = null
    private var playSongButton: Button? = null
    private var nextSongButton: Button? = null
    private var previousSongButton: Button? = null
    private var queueButton: Button? = null
    private var playlistButton: Button? = null
    private var repeatCheckBox: CheckBox? = null
    private var shuffleCheckBox: CheckBox? = null
    private var favoriteCheckBox: CheckBox? = null
    private var songPager: CustomViewPager2? = null
    private var songStartTimeTextview: TextView? = null
    private var songEndTimeTextview: TextView? = null
    private var songSeekBar: SeekBar? = null
    private var visualizer: BarVisualizer? = null
    private var hostCallBack: Host? = null
    private var songsData: SongsData? = null
    private var songPlaying: Song? = null
    private var startPlaying = false
    private var currentlySeeking = false

    /**
     * Gets automatically executed when the Player gets created
     *
     * @param savedInstanceState Standard Android stuff
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        songsData = getInstance(requireContext())
        // Gets the song which is actually playing
        // (The one the user clicked on in the list before)
        songPlaying = songsData!!.songPlaying
        actionBar = (activity as AppCompatActivity?)!!.supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(false)
        actionBar!!.title = songPlaying!!.title
    }

    /**
     * Creates the actual look of the Player with all its elements
     *
     * @param inflater           Overridden
     * @param container          Overridden
     * @param savedInstanceState Overridden
     * @return The view of the Fragment
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Creates a view by inflating its layout
        val view = inflater.inflate(R.layout.fragment_player, container, false)
        songPager = CustomViewPager2(view.findViewById(R.id.viewpager_player_song))
        songPager!!.disableNestedScrolling()
        // Adds all buttons previously declared above
        previousSongButton = view.findViewById(R.id.button_player_prev)
        nextSongButton = view.findViewById(R.id.button_player_next)
        playSongButton = view.findViewById(R.id.button_player_play_pause)
        repeatCheckBox = view.findViewById(R.id.checkbox_player_repeat)
        shuffleCheckBox = view.findViewById(R.id.checkbox_player_shuffle)
        favoriteCheckBox = view.findViewById(R.id.checkbox_player_favorite)
        queueButton = view.findViewById(R.id.button_player_queue)
        playlistButton = view.findViewById(R.id.button_player_addtoplaylist)

        // Adds all texts
        songStartTimeTextview = view.findViewById(R.id.textview_player_elapsed_time)
        songEndTimeTextview = view.findViewById(R.id.textview_player_duration)

        // Adds seekbar, visualizer and the image of the player
        songSeekBar = view.findViewById(R.id.seekbar_player)
        visualizer = view.findViewById(R.id.bar_visualizer_player)

        // After creating every element, the song starts playing
        if (startPlaying) startPlaying(requireContext(), songPlaying!!)
        songPager!!.get().adapter =
            SongPagerAdapter(requireContext(), songsData?.getPlayingQueue()!!)
        songPager!!.get().setCurrentItem(songsData!!.playingIndex, false)
        songPager!!.setOnPageChange(object : OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                songsData!!.playingIndex = songPager!!.get().currentItem
                playCurrent(context!!)
                hostCallBack!!.onSongPlayingUpdate()
            }
        })
        updatePlayerUI()

        // The option to repeat the song or not
        repeatCheckBox?.isChecked = songsData!!.isRepeat
        // The option to shuffle the queue
        shuffleCheckBox?.isChecked = songsData!!.isShuffle
        queueButton?.setOnClickListener { hostCallBack!!.showQueue() }
        playlistButton?.setOnClickListener {
            showAddToPlaylistDialog(requireContext(),
                songPlaying!!,
                object : OnNewPlaylistCallback {
                    override fun onNewPlaylist(newPlaylist: Playlist) {
                        hostCallBack!!.onNewPlaylist(newPlaylist)
                    }
                },
                object : OnPlaylistUpdateCallback {
                    override fun onPlaylistUpdate(playlist: Playlist) {
                        if (playlist == songsData!!.favoritesPlaylist) favoriteCheckBox?.isChecked =
                            songsData!!.isFavorited(songPlaying!!)
                        hostCallBack!!.onPlaylistUpdate(playlist)
                    }
                })
        }

        // Starts the seekbar thread
        //mediaPlayer.getCurrentPosition();
        val songSeekBarUpdaterThread: Thread = object : Thread() {
            override fun run() {
                // Gets the current position on the seekbar
                var currentPosition: Int
                while (true) {
                    if (isResumed) {
                        try {
                            sleep(500)
                            currentPosition = position
                            //mediaPlayer.getCurrentPosition();

                            // If the seekbar gets manually adjusted, we need to get the new position
                            if (!currentlySeeking) songSeekBar?.progress = currentPosition
                            // If the activity gets interrupted. For musicplayer app gets closed.
                            // Prevents app from crashing
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        } catch (e: IllegalStateException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        // Sets properties of the seekbar
        songSeekBarUpdaterThread.start()

        // Controls the changes at the seekbar
        songSeekBar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}

            // User is dragging the seekbar
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                currentlySeeking = true
            }

            // User has stopped dragging the seekbar
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Song needs to start at the set time
                seekTo(seekBar.progress)
                // Time passed needs to be updated
                songStartTimeTextview?.text = createTime(position)
                // User is no longer dragging the seekbar
                currentlySeeking = false
            }
        })

        // Handles time
        val handler = Handler(Looper.getMainLooper())
        val delay = 1000
        handler.postDelayed(object : Runnable {
            override fun run() {
                // While the player is playing, it gets the actual time and sets it
                if (!isStopped) {
                    val currentTime = createTime(position)
                    songStartTimeTextview?.text = currentTime
                    // Has a delay of 1000ms == 1s
                    handler.postDelayed(this, delay.toLong())
                }
            }
        }, delay.toLong())

        // Sets the action to execute when the button is pressed
        playSongButton?.setOnClickListener { togglePlayPause() }
        initializeVisualizer()

        // Plays the next song
        nextSongButton?.setOnClickListener { playNextSong() }
        nextSongButton?.setOnLongClickListener {
            if (isPlaying) {
                seekTo(position + 10000)
                songStartTimeTextview?.text = createTime(position)
            }
            true
        }

        // Plays the previous song
        previousSongButton?.setOnClickListener { playPrevSong() }
        previousSongButton?.setOnLongClickListener {
            if (isPlaying) {
                seekTo(position - 10000)
                songStartTimeTextview?.text = createTime(position)
            }
            true
        }
        repeatCheckBox?.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            songsData!!.isRepeat = isChecked
        }

        // Sets if the queue should be shuffled
        shuffleCheckBox?.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            songsData!!.isShuffle = isChecked
            hostCallBack!!.onShuffle()
        }
        favoriteCheckBox?.setOnClickListener { v: View ->
            val isChecked = (v as CheckBox).isChecked
            val favorites = songsData!!.favoritesPlaylist
            if (isChecked) songsData!!.insertToPlaylist(
                favorites!!,
                songPlaying!!
            ) else songsData!!.removeFromPlaylist(
                favorites!!, songPlaying!!, favorites.songList.indexOf(songPlaying), true
            )
            hostCallBack!!.onPlaylistUpdate(favorites)
        }
        favoriteCheckBox?.setOnCheckedChangeListener { _: CompoundButton, _: Boolean -> }
        view.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                // Notify hosting activity that load is complete
                hostCallBack!!.onPlayerLoadComplete()
            }
        })
        return view
    }

    /**
     * If the fragment is being attached to another activity
     *
     * @param context The context of the app
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            hostCallBack = context as Host
            // If implementation is missing
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement PlayerFragment.Host")
        }
    }

    override fun onPause() {
        super.onPause()
        startPlaying = false
        releaseVisualizer()
    }

    /**
     * Gets executed if the app has been stopped before and now has been restarted
     */
    override fun onResume() {
        // Updates the app
        updatePlayerUI()
        initializeVisualizer()
        super.onResume()
    }

    /**
     * Gets executed if the app or the fragment has been closed
     * Releases the music visualizer bar without errors
     */
    override fun onDestroy() {
        releaseVisualizer()
        super.onDestroy()
    }

    fun releaseVisualizer() {
        if (visualizer != null) {
            visualizer!!.release()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun invalidatePager() {
        val adapter = songPager!!.get().adapter as SongPagerAdapter
        adapter.setQueue(songsData?.getPlayingQueue()!!)
        songPager!!.get().adapter!!.notifyDataSetChanged()
        songPager!!.get().setCurrentItem(songsData!!.playingIndex, false)
    }

    /**
     * Updates all the elements on the fragment. Useful to check if a change has happened and not
     * leave the user behind with old information
     */
    fun updatePlayerUI() {
        // Retrieves the song playing right now
        songPlaying = songsData!!.songPlaying
        favoriteCheckBox!!.isChecked = songsData!!.isFavorited(songPlaying!!)
        actionBar!!.title = songPlaying!!.title

        // Sets all properties again
        if (songPager!!.get().currentItem != songsData!!.playingIndex) songPager!!.scrollByCode(
            songsData!!.playingIndex, false
        )
        val position = position
        val duration = duration
        songSeekBar!!.max = duration
        songSeekBar!!.progress = position

        // Sets the time of the song
        songEndTimeTextview!!.text = createTime(duration)
        songStartTimeTextview!!.text = createTime(position)
        initializeVisualizer()
        // If paused or playing
        updatePlayButton()
    }

    /**
     * Changes the appearance of the button depending on which state the player is currently in
     */
    fun updatePlayButton() {
        if (isPlaying) playSongButton!!.setBackgroundResource(R.drawable.ic_pause) else playSongButton!!.setBackgroundResource(
            R.drawable.ic_play
        )
    }

    /**
     * Plays the next song in the queue, if it is the last song then it will play the first
     */
    private fun playNextSong() {
        // Plays the next song
        playNext(requireContext())
        hostCallBack!!.onSongPlayingUpdate()
    }

    /**
     * Plays the previous song in queue, if it is the first song then it will play the last one
     */
    private fun playPrevSong() {
        // Plays the previous song
        playPrev(requireContext())
        hostCallBack!!.onSongPlayingUpdate()
    }

    /**
     * Changes the state of the song from play to pause and vice-versa
     * Also sets the appearance of the button accordingly
     */
    fun togglePlayPause() {
        // Changes the state of the song
        MediaPlayerUtil.togglePlayPause()
        if (isPlaying) playSongButton!!.setBackgroundResource(R.drawable.ic_pause) else playSongButton!!.setBackgroundResource(
            R.drawable.ic_play
        )
        hostCallBack!!.onPlaybackUpdate()
    }

    private fun initializeVisualizer() {
        val audioSessionId = audioSessionId
        if (audioSessionId != -1 && audioSessionId != 0) visualizer!!.setAudioSessionId(
            audioSessionId
        )
    }

    /**
     * Interface, needed for detecting specific states of the fragment
     */
    interface Host {
        //callback methods
        fun onPlayerLoadComplete()
        fun onPlaybackUpdate()
        fun onSongPlayingUpdate()
        fun onShuffle()
        fun onPlaylistUpdate(playlist: Playlist)
        fun showQueue()
        fun onNewPlaylist(newPlaylist: Playlist)
    }

    companion object {
        /**
         * Creates a new instance of the fragment
         *
         * @return The fragment as a class
         */
        @JvmStatic
        fun newInstance(startPlaying: Boolean): PlayerFragment {
            val instance = PlayerFragment()
            instance.startPlaying = startPlaying
            return instance
        }
    }
}