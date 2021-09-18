package com.musicplayer.SocyMusic.ui.player_fragment_host;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.musicplayer.SocyMusic.MediaPlayerService;
import com.musicplayer.SocyMusic.MediaPlayerUtil;
import com.musicplayer.SocyMusic.custom_views.CustomViewPager2;
import com.musicplayer.SocyMusic.data.Song;
import com.musicplayer.SocyMusic.data.SongsData;
import com.musicplayer.SocyMusic.ui.player.PlayerFragment;
import com.musicplayer.SocyMusic.ui.queue.QueueFragment;
import com.musicplayer.SocyMusic.utils.BluetoothUtil;
import com.musicplayer.SocyMusic.utils.UiUtils;
import com.musicplayer.musicplayer.R;

public abstract class PlayerFragmentHost extends AppCompatActivity implements PlayerFragment.Host, QueueFragment.Host, ServiceConnection {
    protected SongsData songsData;

    private PlayerFragment playerFragment;
    private QueueFragment queueFragment;
    private View contentView;

    private static MediaPlayerService mediaPlayerService;
    private static MediaPlayerReceiver mediaPlayerReceiver;

    private static BluetoothUtil bluetoothReceiver;

    private CustomViewPager2 songInfoPager;
    protected BottomSheetBehavior<FrameLayout> bottomSheetBehavior;

    private boolean startPlaying;
    private boolean playerLoadComplete;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_host);
        songsData = SongsData.getInstance(this);

        songInfoPager = new CustomViewPager2(findViewById(R.id.viewpager_info_panes));
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet_player));
        final FrameLayout playerContainer = findViewById(R.id.layout_player_container);
        playerContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                playerContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                bottomSheetBehavior.setPeekHeight(songInfoPager.get().getHeight());
            }
        });
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    invalidateOptionsMenu();
                    //actionBar.setTitle(R.string.player_title);
                    songInfoPager.get().setUserInputEnabled(false);
//                    songInfoPager.findViewWithTag(songInfoPager.getCurrentItem()).setClickable(false);
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    invalidateOptionsMenu();
                    //actionBar.setTitle(R.string.all_app_name);
                    ((ViewGroup.MarginLayoutParams) contentView.getLayoutParams()).bottomMargin = UiUtils.dpToPixel(PlayerFragmentHost.this, 50);
                    songInfoPager.get().setUserInputEnabled(true);
//                    songInfoPager.findViewWithTag(songInfoPager.getCurrentItem()).setClickable(true);
                    hideQueue();
                } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    ((ViewGroup.MarginLayoutParams) contentView.getLayoutParams()).bottomMargin = 0;
                }
            }

            @Override
            // If user slides to the bottom on the sheet
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                songInfoPager.get().setAlpha(1f - slideOffset);
            }
        });

        songInfoPager.setOnPageChange(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                int position = songInfoPager.get().getCurrentItem();
                songsData.setPlayingIndex(position);
                MediaPlayerUtil.playCurrent(PlayerFragmentHost.this);
                onSongPlayingUpdate();
            }
        });

    }

    protected void startPlayer(boolean startPlaying) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        playerFragment = PlayerFragment.newInstance(startPlaying);
        fragmentManager.beginTransaction().add(R.id.layout_player_container, playerFragment).commit();
        bottomSheetBehavior.setHideable(false);
        this.startPlaying = startPlaying;
    }

    public void showQueue() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        View playerFragmentView = findViewById(R.id.layout_player_holder);
        queueFragment = new QueueFragment();
        fragmentManager.beginTransaction().add(R.id.layout_queue_container, queueFragment).commit();
        //actionBar.setTitle(R.string.queue_title);
        playerFragment.releaseVisualizer();
        playerFragmentView.setVisibility(View.INVISIBLE);
        invalidateOptionsMenu();
    }

    public void hideQueue() {
        if (queueFragment == null)
            return;
        FragmentManager fragmentManager = getSupportFragmentManager();
        View playerFragmentView = findViewById(R.id.layout_player_holder);
        queueFragment.onDestroyView();
        fragmentManager.beginTransaction().remove(queueFragment).commit();

        playerFragmentView.setVisibility(View.VISIBLE);
        playerFragment.updatePlayerUI();
        //actionBar.setTitle(R.string.player_title);
        queueFragment = null;
        invalidateOptionsMenu();
    }

    @Override
    public void onPlayerLoadComplete() {
        InfoPanePagerAdapter customAdapter = new InfoPanePagerAdapter(this, songsData.getPlayingQueue());
        customAdapter.setPaneListeners(new InfoPanePagerAdapter.PaneListeners() {
            @Override
            public void onPaneClick() {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }

            @Override
            public void onPauseButtonClick() {
                playerFragment.togglePlayPause();
            }
        });
        songInfoPager.get().setAdapter(customAdapter);

        if (songsData.getPlayingIndex() != 0)
            songInfoPager.scrollByCode(songsData.getPlayingIndex(), false);

        if (startPlaying)
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        else
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        playerLoadComplete = true;
//        songInfoPager.onIdle();
    }

    @Override
    public void onPlaybackUpdate() {
        if (mediaPlayerService != null)
            mediaPlayerService.refreshNotification();
        if (playerFragment != null)
            playerFragment.updatePlayButton();
        int currentItem = songInfoPager.get().getCurrentItem();
        songInfoPager.get().getAdapter().notifyItemChanged(currentItem, songsData.getSongFromQueueAt(currentItem));
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onSongPlayingUpdate() {
        if (mediaPlayerService != null)
            mediaPlayerService.refreshNotification();
        if (queueFragment != null)
            queueFragment.updateQueue();
        else if (playerFragment != null)
            playerFragment.updatePlayerUI();

        InfoPanePagerAdapter pagerAdapter = (InfoPanePagerAdapter) songInfoPager.get().getAdapter();
        //determine if queue changed or if simple scroll happened
        if (pagerAdapter.getQueue() != songsData.getPlayingQueue()) {
            pagerAdapter.setQueue(songsData.getPlayingQueue());
            pagerAdapter.notifyDataSetChanged();
            playerFragment.invalidatePager();
        }
        songInfoPager.scrollByCode(songsData.getPlayingIndex(), false);
        int currentItem = songInfoPager.get().getCurrentItem();

        pagerAdapter.notifyItemChanged(currentItem, songsData.getSongFromQueueAt(currentItem));

    }

    @Override
    public void onQueueReordered() {
        songInfoPager.get().getAdapter().notifyDataSetChanged();
        playerFragment.invalidatePager();
        songInfoPager.scrollByCode(songsData.getPlayingIndex(), false);
    }

    @Override
    public void onShuffle() {
        if (queueFragment != null)
            queueFragment.updateQueue();
        InfoPanePagerAdapter pagerAdapter = (InfoPanePagerAdapter) songInfoPager.get().getAdapter();
        pagerAdapter.setQueue(songsData.getPlayingQueue());
        pagerAdapter.notifyDataSetChanged();
        playerFragment.invalidatePager();
    }

    public void onSongClick(Song songClicked) {
        // Opens the player fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.layout_player_container);
        if (fragment == null) {
            startPlayer(true);
            Intent serviceIntent = new Intent(this, MediaPlayerService.class);
            serviceIntent.putExtra(MediaPlayerService.EXTRA_SONG, songClicked);
            startService(serviceIntent);
            bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
        } else {
            playerFragment = (PlayerFragment) fragment;
            playerFragment.invalidatePager();
            MediaPlayerUtil.startPlaying(this, songClicked);
            onSongPlayingUpdate();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            hideQueue();
        }
    }

    public boolean isShowingPlayer() {
        return playerFragment != null
                && bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN;
    }

    /**
     * If playing is paused this method stops the mediaplayer service
     */
    @Override
    protected void onPause() {
//        if (isFinishing()) {
//            unbindService(this);
//            if (mediaPlayerService != null)
//                mediaPlayerService.stopSelf();
//            MediaPlayerUtil.stop();
//        }
        super.onPause();
    }

    /**
     * Basically gets executed when the app gets resumed. which means when it is closed and reopened
     */
    @Override
    protected void onResume() {
        super.onResume();
        registerMediaReceiver();
        if (playerLoadComplete && isShowingPlayer())
            onSongPlayingUpdate();

    }

    private void registerMediaReceiver() {
        // Creates a new mediaplayer receiver if none exist
        if (mediaPlayerReceiver == null)
            mediaPlayerReceiver = new MediaPlayerReceiver();
        // Sets all intents for actions
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MediaPlayerService.ACTION_PREV);
        intentFilter.addAction(MediaPlayerService.ACTION_PLAY);
        intentFilter.addAction(MediaPlayerService.ACTION_PAUSE);
        intentFilter.addAction(MediaPlayerService.ACTION_TOGGLE_PLAY_PAUSE);
        intentFilter.addAction(MediaPlayerService.ACTION_NEXT);
        intentFilter.addAction(MediaPlayerService.ACTION_CANCEL);
        registerReceiver(mediaPlayerReceiver, intentFilter);

    }

    protected void unregisterMediaReceiver() {
        unregisterReceiver(mediaPlayerReceiver);
        mediaPlayerReceiver = null;
    }

    /**
     * Sets what happens if the user presses the 'back'-key
     */
    @Override
    public void onBackPressed() {
        if (queueFragment != null)
            hideQueue();
        else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        else
            super.onBackPressed();

    }

    protected ViewGroup getRootView() {
        return findViewById(R.id.layout_player_host_root);
    }

    protected void attachContentView(View contentView) {
        this.contentView = contentView;
        contentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        getRootView().addView(contentView, 0);
    }

    public void onQueueChanged() {
        if (!isShowingPlayer()) {
            startPlayer(false);
            return;
        }
        InfoPanePagerAdapter adapter = (InfoPanePagerAdapter) songInfoPager.get().getAdapter();
        adapter.setQueue(songsData.getPlayingQueue());
        onQueueReordered();
    }

    /**
     * Extends the standard Broadcastreceiver to create a new receiver for the mediaplayer
     */
    private class MediaPlayerReceiver extends BroadcastReceiver {

        /**
         * Sets what should happen when the receiver gets a signal
         *
         * @param context Context of the app
         * @param intent  Intent to get the action from
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // Checks for different actions
            switch (action) {
                case MediaPlayerService.ACTION_PREV:
                    MediaPlayerUtil.playPrev(PlayerFragmentHost.this);
                    onSongPlayingUpdate();
                    break;
                case MediaPlayerService.ACTION_PLAY:
                case MediaPlayerService.ACTION_PAUSE:
                case MediaPlayerService.ACTION_TOGGLE_PLAY_PAUSE:
                    if (action.equals(MediaPlayerService.ACTION_PLAY))
                        MediaPlayerUtil.play();
                    else if (action.equals(MediaPlayerService.ACTION_PAUSE))
                        MediaPlayerUtil.pause();
                    else
                        MediaPlayerUtil.togglePlayPause();
                    mediaPlayerService.refreshNotification();
                    onPlaybackUpdate();
                    break;
                case MediaPlayerService.ACTION_NEXT:
                    MediaPlayerUtil.playNext(PlayerFragmentHost.this);
                    onSongPlayingUpdate();
                    break;
                case MediaPlayerService.ACTION_CANCEL:
                    mediaPlayerService.stopSelf();
                    break;
            }

        }

    }

    /**
     * Sets what happens if the service connects
     *
     * @param name    Name of the service
     * @param iBinder Binder for the service
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder iBinder) {
        mediaPlayerService = ((MediaPlayerService.LocalBinder) iBinder).getService();
    }

    /**
     * Sets what happens if the service disconnects
     *
     * @param name Name of the service
     */
    @Override
    public void onServiceDisconnected(ComponentName name) {
        mediaPlayerService = null;
    }

}
