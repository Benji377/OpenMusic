package com.musicplayer.openmusic.custom_views

import android.os.Handler
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import timber.log.Timber

/**
 * A custom ViewPager2 class that can scroll without triggering ViewPager2.OnPageChangeCallBack
 * with the method scrollByCode(). This is to distinguish between when the user changes page
 * a page change happens programmatically.
 * Does not extend ViewPager2 because it's final.
 * Thanks, Google :(
 */
class CustomViewPager2(private val viewPager2: ViewPager2) {
    private var onPageChange: OnPageChangeCallback? = null
    private var isScrolledByCode = false

    /**
     * CustomViewPager constructor. Defines its possible methods, actions and
     * register action listeners.
     */
    init {
        viewPager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            private var previousPosition = 0
            private var newPageSelected = false

            /**
             * Defines what should happen when the page gets selected by the user
             * @param position The position at which the user selected the view
             */
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Timber.i("ViewPager has been selected")
                // Sets the new position while keeping the old one too
                newPageSelected = position != previousPosition
                previousPosition = position
            }

            /**
             * Defines what should happen when the scrollState changes on the view
             * @param state New state of the view
             */
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                // The user scrolled on his own
                if (state == ViewPager.SCROLL_STATE_IDLE && newPageSelected && !isScrolledByCode) {
                    Timber.i("User scrolled on his own")
                    newPageSelected = false
                    if (onPageChange != null) onPageChange!!.onPageScrollStateChanged(state)
                }
                // The scrolling happened trough code
                if (isScrolledByCode && state == ViewPager.SCROLL_STATE_IDLE) {
                    Timber.i("ViewPager scrolled programmatically")
                    isScrolledByCode = false
                    newPageSelected = false
                }
            }
        })
    }

    /**
     * Gives us the ability to scroll to a set item (position) programmatically
     *
     * @param item         Position to scroll to
     * @param smoothScroll If scrolling should happen smoothly (visibly)
     */
    fun scrollByCode(item: Int, smoothScroll: Boolean) {
        Timber.i("ViewPager scrolled by code")
        isScrolledByCode = true
        viewPager2.setCurrentItem(item, smoothScroll)
        Handler().postDelayed({ onIdle() }, 50)
    }

    /**
     * ViewPager is idle
     */
    private fun onIdle() {
        isScrolledByCode = false
    }

    /**
     * Gets the current viewPager
     *
     * @return This viewPager
     */
    fun get(): ViewPager2 {
        return viewPager2
    }

    /**
     * Sets the pageChange actionListener when the page changes
     *
     * @param onPageChange The actionListener
     */
    fun setOnPageChange(onPageChange: OnPageChangeCallback) {
        this.onPageChange = onPageChange
    }

    /**
     * Disables the ability to scroll "twice"
     */
    fun disableNestedScrolling() {
        val innerRecyclerView = viewPager2.getChildAt(0) as RecyclerView
        innerRecyclerView.isNestedScrollingEnabled = false
        innerRecyclerView.overScrollMode = View.OVER_SCROLL_NEVER
    }
}