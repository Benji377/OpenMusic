package com.musicplayer.OpenMusic.custom_views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber

/**
 * Custom RecyclerView that has an empty view to show when empty and hide when not, automatically,
 * just like ListView's setEmptyView() method.
 * This is used to only show the player when an actual song is selected, else the player shouldn't be visible
 * Code taken from: https://stackoverflow.com/a/27801394/14200676
 */
class CustomRecyclerView : RecyclerView {
    private var emptyView: View? = null

    /**
     * Sets an AdapterObserver to observe incoming requests and states
     */
    private val observer: AdapterDataObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            Timber.i("CustomRecyclerView has changed")
            checkIfEmpty()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            Timber.i("CustomRecyclerView has inserted a new Item")
            checkIfEmpty()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            Timber.i("CustomRecyclerView has removed an Item")
            checkIfEmpty()
        }
    }

    /**
     * Constructor for the RecyclerView
     *
     * @param context Context of the app
     */
    constructor(context: Context) : super(context)

    /**
     * Constructor for the RecyclerView
     *
     * @param context Context of the app
     * @param attrs   Custom AttributeSet
     */
    constructor(context: Context, attrs: AttributeSet) : super(
        context, attrs
    )

    /**
     * Constructor for the RecyclerView
     *
     * @param context  Context of the app
     * @param attrs    Custom AttributeSet
     * @param defStyle Defined Styling
     */
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context, attrs, defStyle
    )

    /**
     * Checks if the current view is empty, if so then the view will go invisible.
     * Else the view will become visible
     */
    fun checkIfEmpty() {
        if (emptyView != null && adapter != null) {
            // Counts the items inside the view, if its 0 it means the view is empty
            val emptyViewVisible = adapter!!.itemCount == 0
            emptyView!!.visibility = if (emptyViewVisible) VISIBLE else GONE
            visibility = if (emptyViewVisible) GONE else VISIBLE
        }
    }

    /**
     * Sets the Adapter for the Observer on the view
     *
     * @param adapter Adapter to set on the View
     */
    override fun setAdapter(adapter: Adapter<*>?) {
        Timber.i("Setting the Adapter to the CustomRecyclerView")
        val oldAdapter = getAdapter()
        oldAdapter?.unregisterAdapterDataObserver(observer)
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(observer)
        checkIfEmpty()
    }

    /**
     * Manually sets this view as empty
     *
     * @param emptyView The view to empty
     */
    fun setEmptyView(emptyView: View?) {
        Timber.i("Setting view to empty on CustomRecyclerView")
        this.emptyView = emptyView
        checkIfEmpty()
    }
}