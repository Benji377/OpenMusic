package com.musicplayer.OpenMusic.custom_views;


import android.os.Handler;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import timber.log.Timber;

/**
 * A custom ViewPager2 class that can scroll without triggering ViewPager2.OnPageChangeCallBack
 * with the method scrollByCode(). This is to distinguish between when the user changes page
 * a page change happens programmatically.
 * Does not extend ViewPager2 because it's final.
 * Thanks, Google :(
 */
public class CustomViewPager2 {
    private final ViewPager2 viewPager2;
    private ViewPager2.OnPageChangeCallback onPageChange;
    private boolean isScrolledByCode;

    /**
     * CustomViewPager constructor. Defines its possible methods, actions and
     * register action listeners.
     *
     * @param viewPager2 The viewpager to wrap the custom viewpager around
     */
    public CustomViewPager2(ViewPager2 viewPager2) {
        this.viewPager2 = viewPager2;
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            private int previousPosition;
            private boolean newPageSelected;

            /**
             * Defines what should happen when the page gets selected by the user
             * @param position The position at which the user selected the view
             */
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Timber.i("ViewPager has been selected");
                // Sets the new position while keeping the old one too
                newPageSelected = position != previousPosition;
                previousPosition = position;
            }

            /**
             * Defines what should happen when the scrollState changes on the view
             * @param state New state of the view
             */
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                // The user scrolled on his own
                if (state == ViewPager.SCROLL_STATE_IDLE && newPageSelected && !isScrolledByCode) {
                    Timber.i("User scrolled on his own");
                    newPageSelected = false;
                    if (onPageChange != null)
                        onPageChange.onPageScrollStateChanged(state);
                }
                // The scrolling happened trough code
                if (isScrolledByCode && state == ViewPager.SCROLL_STATE_IDLE) {
                    Timber.i("ViewPager scrolled programmatically");
                    isScrolledByCode = false;
                    newPageSelected = false;
                }
            }
        });
    }

    /**
     * Gives us the ability to scroll to a set item (position) programmatically
     *
     * @param item         Position to scroll to
     * @param smoothScroll If scrolling should happen smoothly (visibly)
     */
    public void scrollByCode(int item, boolean smoothScroll) {
        Timber.i("ViewPager scrolled by code");
        isScrolledByCode = true;
        viewPager2.setCurrentItem(item, smoothScroll);
        (new Handler()).postDelayed(this::onIdle, 50);
    }

    /**
     * ViewPager is idle
     */
    private void onIdle() {
        this.isScrolledByCode = false;
    }

    /**
     * Gets the current viewPager
     *
     * @return This viewPager
     */
    public ViewPager2 get() {
        return viewPager2;
    }

    /**
     * Sets the pageChange actionListener when the page changes
     *
     * @param onPageChange The actionListener
     */
    public void setOnPageChange(ViewPager2.OnPageChangeCallback onPageChange) {
        this.onPageChange = onPageChange;
    }

    /**
     * Disables the ability to scroll "twice"
     */
    public void disableNestedScrolling() {
        RecyclerView innerRecyclerView = (RecyclerView) this.viewPager2.getChildAt(0);
        if (innerRecyclerView != null) {
            innerRecyclerView.setNestedScrollingEnabled(false);
            innerRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
    }
}
