package com.musicplayer.OpenMusic.custom_views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import timber.log.Timber;


/**
 * Custom RecyclerView that has an empty view to show when empty and hide when not, automatically,
 * just like ListView's setEmptyView() method.
 * This is used to only show the player when an actual song is selected, else the player shouldn't be visible
 * Code taken from: https://stackoverflow.com/a/27801394/14200676
 */
public class CustomRecyclerView extends RecyclerView {
    private View emptyView;
    /**
     * Sets an AdapterObserver to observe incoming requests and states
     */
    final private AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            Timber.i("CustomRecyclerView has changed");
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            Timber.i("CustomRecyclerView has inserted a new Item");
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            Timber.i("CustomRecyclerView has removed an Item");
            checkIfEmpty();
        }
    };

    /**
     * Constructor for the RecyclerView
     * @param context Context of the app
     */
    public CustomRecyclerView(Context context) {
        super(context);
    }

    /**
     * Constructor for the RecyclerView
     * @param context Context of the app
     * @param attrs Custom AttributeSet
     */
    public CustomRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Constructor for the RecyclerView
     * @param context Context of the app
     * @param attrs Custom AttributeSet
     * @param defStyle Defined Styling
     */
    public CustomRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Checks if the current view is empty, if so then the view will go invisible.
     * Else the view will become visible
     */
    void checkIfEmpty() {
        if (emptyView != null && getAdapter() != null) {
            // Counts the items inside the view, if its 0 it means the view is empty
            final boolean emptyViewVisible = getAdapter().getItemCount() == 0;
            emptyView.setVisibility(emptyViewVisible ? VISIBLE : GONE);
            setVisibility(emptyViewVisible ? GONE : VISIBLE);
        }
    }

    /**
     * Sets the Adapter for the Observer on the view
     * @param adapter Adapter to set on the View
     */
    @Override
    public void setAdapter(Adapter adapter) {
        Timber.i("Setting the Adapter to the CustomRecyclerView");
        final Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
        }

        checkIfEmpty();
    }

    /**
     * Manually sets this view as empty
     * @param emptyView The view to empty
     */
    public void setEmptyView(View emptyView) {
        Timber.i("Setting view to empty on CustomRecyclerView");
        this.emptyView = emptyView;
        checkIfEmpty();
    }
}