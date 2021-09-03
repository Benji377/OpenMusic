package com.musicplayer.SocyMusic.custom_views;


import android.os.Handler;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

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

    public CustomViewPager2(ViewPager2 viewPager2) {
        this.viewPager2 = viewPager2;
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            private int previousPosition;
            private boolean newPageSelected;

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                newPageSelected = position != previousPosition;
                previousPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager.SCROLL_STATE_IDLE && newPageSelected && !isScrolledByCode) {
                    newPageSelected = false;
                    if (onPageChange != null)
                        onPageChange.onPageScrollStateChanged(state);
                }
                if (isScrolledByCode && state == ViewPager.SCROLL_STATE_IDLE) {
                    isScrolledByCode = false;
                    newPageSelected = false;
                }
            }
        });
    }


    public void scrollByCode(int item, boolean smoothScroll) {
        isScrolledByCode = true;
        viewPager2.setCurrentItem(item, smoothScroll);
        (new Handler()).postDelayed(this::onIdle, 50);
    }

    private void onIdle() {
        this.isScrolledByCode = false;
    }

    public ViewPager2 get() {
        return viewPager2;
    }

    public void setOnPageChange(ViewPager2.OnPageChangeCallback onPageChange) {
        this.onPageChange = onPageChange;
    }

    public void disableNestedScrolling() {
        RecyclerView innerRecyclerView = (RecyclerView) this.viewPager2.getChildAt(0);
        if (innerRecyclerView != null) {
            innerRecyclerView.setNestedScrollingEnabled(false);
            innerRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
    }
}
