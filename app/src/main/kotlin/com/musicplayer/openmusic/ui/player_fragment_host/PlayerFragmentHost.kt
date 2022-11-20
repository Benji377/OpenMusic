package com.musicplayer.openmusic.ui.player_fragment_host

import android.annotation.SuppressLint
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.FrameLayout
import android.window.OnBackInvokedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.musicplayer.openmusic.MediaPlayerService
import com.musicplayer.openmusic.MediaPlayerService.LocalBinder
import com.musicplayer.openmusic.MediaPlayerUtil.pause
import com.musicplayer.openmusic.MediaPlayerUtil.play
import com.musicplayer.openmusic.MediaPlayerUtil.playCurrent
import com.musicplayer.openmusic.MediaPlayerUtil.playNext
import com.musicplayer.openmusic.MediaPlayerUtil.playPrev
import com.musicplayer.openmusic.MediaPlayerUtil.startPlaying
import com.musicplayer.openmusic.MediaPlayerUtil.togglePlayPause
import com.musicplayer.openmusic.R
import com.musicplayer.openmusic.custom_views.CustomViewPager2
import com.musicplayer.openmusic.data.Song
import com.musicplayer.openmusic.data.SongsData
import com.musicplayer.openmusic.data.SongsData.Companion.getInstance
import com.musicplayer.openmusic.ui.player.PlayerFragment
import com.musicplayer.openmusic.ui.player.PlayerFragment.Companion.newInstance
import com.musicplayer.openmusic.ui.player_fragment_host.InfoPanePagerAdapter.PaneListeners
import com.musicplayer.openmusic.ui.queue.QueueFragment
import com.musicplayer.openmusic.utils.BluetoothUtil
import com.musicplayer.openmusic.utils.UiUtils.dpToPixel

abstract class PlayerFragmentHost : AppCompatActivity(), PlayerFragment.Host, QueueFragment.Host,
    ServiceConnection {
    protected var songsData: SongsData? = null
    protected var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>? = null
    private var playerFragment: PlayerFragment? = null
    private var queueFragment: QueueFragment? = null
    private var contentView1: View? = null
    private var songInfoPager: CustomViewPager2? = null
    private var startPlaying = false
    private var playerLoadComplete = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_host)
        songsData = getInstance(this)
        songInfoPager = CustomViewPager2(findViewById(R.id.viewpager_info_panes))
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet_player))
        val playerContainer = findViewById<FrameLayout>(R.id.layout_player_container)
        playerContainer.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                playerContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                bottomSheetBehavior!!.peekHeight = songInfoPager!!.get().height
            }
        })
        bottomSheetBehavior!!.isHideable = true
        bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior!!.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    invalidateOptionsMenu()
                    //actionBar.setTitle(R.string.player_title);
                    songInfoPager!!.get().isUserInputEnabled = false
                    //                    songInfoPager.findViewWithTag(songInfoPager.getCurrentItem()).setClickable(false);
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    invalidateOptionsMenu()
                    //actionBar.setTitle(R.string.all_app_name);
                    (contentView1!!.layoutParams as MarginLayoutParams).bottomMargin =
                        dpToPixel(this@PlayerFragmentHost, 50)
                    songInfoPager!!.get().isUserInputEnabled = true
                    //                    songInfoPager.findViewWithTag(songInfoPager.getCurrentItem()).setClickable(true);
                    hideQueue()
                } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    (contentView1!!.layoutParams as MarginLayoutParams).bottomMargin = 0
                }
            }

            // If user slides to the bottom on the sheet
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                songInfoPager!!.get().alpha = 1f - slideOffset
            }
        })
        songInfoPager!!.setOnPageChange(object : OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                val position = songInfoPager!!.get().currentItem
                songsData!!.playingIndex = position
                playCurrent(this@PlayerFragmentHost)
                onSongPlayingUpdate()
            }
        })
    }

    protected fun startPlayer(startPlaying: Boolean) {
        val fragmentManager = supportFragmentManager
        playerFragment = newInstance(startPlaying)
        fragmentManager.beginTransaction().add(R.id.layout_player_container, playerFragment!!)
            .commit()
        bottomSheetBehavior!!.isHideable = false
        this.startPlaying = startPlaying
    }

    override fun showQueue() {
        val fragmentManager = supportFragmentManager
        val playerFragmentView = findViewById<View>(R.id.layout_player_holder)
        queueFragment = QueueFragment()
        fragmentManager.beginTransaction().add(R.id.layout_queue_container, queueFragment!!)
            .commit()
        //actionBar.setTitle(R.string.queue_title);
        playerFragment!!.releaseVisualizer()
        playerFragmentView.visibility = View.INVISIBLE
        invalidateOptionsMenu()
    }

    fun hideQueue() {
        if (queueFragment == null) return
        val fragmentManager = supportFragmentManager
        val playerFragmentView = findViewById<View>(R.id.layout_player_holder)
        queueFragment!!.onDestroyView()
        fragmentManager.beginTransaction().remove(queueFragment!!).commit()
        playerFragmentView.visibility = View.VISIBLE
        playerFragment!!.updatePlayerUI()
        //actionBar.setTitle(R.string.player_title);
        queueFragment = null
        invalidateOptionsMenu()
    }

    override fun onPlayerLoadComplete() {
        val customAdapter = InfoPanePagerAdapter(this, songsData!!.getPlayingQueue()!!)
        customAdapter.setPaneListeners(object : PaneListeners {
            override fun onPaneClick() {
                bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
            }

            override fun onPauseButtonClick() {
                playerFragment!!.togglePlayPause()
            }
        })
        songInfoPager!!.get().adapter = customAdapter
        if (songsData!!.playingIndex != 0) songInfoPager!!.scrollByCode(
            songsData!!.playingIndex,
            false
        )
        if (startPlaying) bottomSheetBehavior!!.setState(BottomSheetBehavior.STATE_EXPANDED) else bottomSheetBehavior!!.setState(
            BottomSheetBehavior.STATE_COLLAPSED
        )
        playerLoadComplete = true
        //        songInfoPager.onIdle();
    }

    override fun onPlaybackUpdate() {
        if (mediaPlayerService != null) mediaPlayerService!!.refreshNotification()
        if (playerFragment != null) playerFragment!!.updatePlayButton()
        val currentItem = songInfoPager!!.get().currentItem
        songInfoPager!!.get().adapter!!.notifyItemChanged(
            currentItem,
            songsData!!.getSongFromQueueAt(currentItem)
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onSongPlayingUpdate() {
        if (mediaPlayerService != null) mediaPlayerService!!.refreshNotification()
        if (queueFragment != null) queueFragment!!.updateQueue() else if (playerFragment != null) playerFragment!!.updatePlayerUI()
        val pagerAdapter = songInfoPager!!.get().adapter as InfoPanePagerAdapter?
        //determine if queue changed or if simple scroll happened
        if (pagerAdapter!!.queue !== songsData!!.getPlayingQueue()) {
            pagerAdapter!!.queue = songsData!!.getPlayingQueue()!!
            pagerAdapter.notifyDataSetChanged()
            playerFragment!!.invalidatePager()
        }
        songInfoPager!!.scrollByCode(songsData!!.playingIndex, false)
        val currentItem = songInfoPager!!.get().currentItem
        pagerAdapter!!.notifyItemChanged(currentItem, songsData!!.getSongFromQueueAt(currentItem))
    }

    override fun onQueueReordered() {
        songInfoPager!!.get().adapter!!.notifyDataSetChanged()
        playerFragment!!.invalidatePager()
        songInfoPager!!.scrollByCode(songsData!!.playingIndex, false)
    }

    override fun onShuffle() {
        if (queueFragment != null) queueFragment!!.updateQueue()
        val pagerAdapter = songInfoPager!!.get().adapter as InfoPanePagerAdapter?
        pagerAdapter!!.queue = songsData!!.getPlayingQueue()!!
        pagerAdapter.notifyDataSetChanged()
        playerFragment!!.invalidatePager()
    }

    open fun onSongClick(song: Song) {
        // Opens the player fragment
        val fragmentManager = supportFragmentManager
        val fragment = fragmentManager.findFragmentById(R.id.layout_player_container)
        if (fragment == null) {
            startPlayer(true)
            val serviceIntent = Intent(this, MediaPlayerService::class.java)
            serviceIntent.putExtra(MediaPlayerService.EXTRA_SONG, song)
            startService(serviceIntent)
            bindService(serviceIntent, this, BIND_AUTO_CREATE)
        } else {
            playerFragment = fragment as PlayerFragment?
            playerFragment!!.invalidatePager()
            startPlaying(this, song)
            onSongPlayingUpdate()
            bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
            hideQueue()
        }
    }

    private val isShowingPlayer: Boolean
        get() = (playerFragment != null
                && bottomSheetBehavior!!.state != BottomSheetBehavior.STATE_HIDDEN)

    /**
     * Basically gets executed when the app gets resumed. which means when it is closed and reopened
     */
    override fun onResume() {
        super.onResume()
        registerMediaReceiver()
        if (playerLoadComplete && isShowingPlayer) onSongPlayingUpdate()
    }

    private fun registerMediaReceiver() {
        // Creates a new mediaplayer receiver if none exist
        if (mediaPlayerReceiver == null) mediaPlayerReceiver = MediaPlayerReceiver()
        // Sets all intents for actions
        val intentFilter = IntentFilter()
        intentFilter.addAction(MediaPlayerService.ACTION_PREV)
        intentFilter.addAction(MediaPlayerService.ACTION_PLAY)
        intentFilter.addAction(MediaPlayerService.ACTION_PAUSE)
        intentFilter.addAction(MediaPlayerService.ACTION_TOGGLE_PLAY_PAUSE)
        intentFilter.addAction(MediaPlayerService.ACTION_NEXT)
        intentFilter.addAction(MediaPlayerService.ACTION_CANCEL)
        registerReceiver(mediaPlayerReceiver, intentFilter)
    }

    protected fun unregisterMediaReceiver() {
        unregisterReceiver(mediaPlayerReceiver)
        mediaPlayerReceiver = null
    }

    /**
     * Sets what happens if the user presses the 'back'-key
     */

    override fun getOnBackInvokedDispatcher(): OnBackInvokedDispatcher {
        // Since the Songinfo Fragment might be present, we  need to recreate the Playerfragment first
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.layout_player_container, PlayerFragment())
        fragmentTransaction.commit()
        if (queueFragment != null)
            hideQueue()
        else if (bottomSheetBehavior!!.state == BottomSheetBehavior.STATE_EXPANDED) bottomSheetBehavior!!.state =
            BottomSheetBehavior.STATE_COLLAPSED
        return super.getOnBackInvokedDispatcher()
    }

    protected val rootView: ViewGroup
        get() = findViewById(R.id.layout_player_host_root)

    protected fun attachContentView(contentView: View) {
        this.contentView1 = contentView
        contentView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        rootView.addView(contentView, 0)
    }

    fun onQueueChanged() {
        if (!isShowingPlayer) {
            startPlayer(false)
            return
        }
        val adapter = songInfoPager!!.get().adapter as InfoPanePagerAdapter?
        adapter!!.queue = songsData!!.getPlayingQueue()!!
        onQueueReordered()
    }

    /**
     * Sets what happens if the service connects
     *
     * @param name    Name of the service
     * @param iBinder Binder for the service
     */
    override fun onServiceConnected(name: ComponentName, iBinder: IBinder) {
        mediaPlayerService = (iBinder as LocalBinder).service
    }

    /**
     * Sets what happens if the service disconnects
     *
     * @param name Name of the service
     */
    override fun onServiceDisconnected(name: ComponentName) {
        mediaPlayerService = null
    }

    /**
     * Extends the standard Broadcastreceiver to create a new receiver for the mediaplayer
     */
    private inner class MediaPlayerReceiver : BroadcastReceiver() {
        /**
         * Sets what should happen when the receiver gets a signal
         *
         * @param context Context of the app
         * @param intent  Intent to get the action from
         */
        override fun onReceive(context: Context, intent: Intent) {
            when (val action = intent.action) {
                MediaPlayerService.ACTION_PREV -> {
                    playPrev(this@PlayerFragmentHost)
                    onSongPlayingUpdate()
                }
                MediaPlayerService.ACTION_PLAY, MediaPlayerService.ACTION_PAUSE, MediaPlayerService.ACTION_TOGGLE_PLAY_PAUSE -> {
                    if (action == MediaPlayerService.ACTION_PLAY) play() else if (action == MediaPlayerService.ACTION_PAUSE) pause() else togglePlayPause()
                    mediaPlayerService!!.refreshNotification()
                    onPlaybackUpdate()
                }
                MediaPlayerService.ACTION_NEXT -> {
                    playNext(this@PlayerFragmentHost)
                    onSongPlayingUpdate()
                }
                MediaPlayerService.ACTION_CANCEL -> mediaPlayerService!!.stopSelf()
            }
        }
    }

    companion object {
        private var mediaPlayerService: MediaPlayerService? = null
        private var mediaPlayerReceiver: MediaPlayerReceiver? = null
        private val bluetoothReceiver: BluetoothUtil? = null
    }
}