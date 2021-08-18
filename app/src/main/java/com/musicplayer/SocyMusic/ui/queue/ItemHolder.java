package com.musicplayer.SocyMusic.ui.queue;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.musicplayer.SocyMusic.MediaPlayerUtil;
import com.musicplayer.SocyMusic.Song;
import com.musicplayer.SocyMusic.SongsData;
import com.musicplayer.musicplayer.R;
import com.woxthebox.draglistview.DragItemAdapter;

public class ItemHolder extends DragItemAdapter.ViewHolder {
    private final Context context;
    private final SongsData songsData;
    private final TextView songTitleTextView;
    private final BarVisualizer visualizer;
    private Song song;

    private final ItemAdapter.onItemClickedListener onItemClickedListener;

    public ItemHolder(Context context, View itemView, @IdRes int grabHandleID, boolean dragOnLongPress, ItemAdapter.onItemClickedListener listener) {
        super(itemView, grabHandleID, dragOnLongPress);
        this.context = context;
        this.onItemClickedListener = listener;
        songsData = SongsData.getInstance(context);

        songTitleTextView = itemView.findViewById(R.id.textview_queue_item_song_title);
        visualizer = itemView.findViewById(R.id.bar_visualizer_queue_item);
    }

    public void bind(Song song) {
        this.song = song;

        // Gets the correct color from the theme -> Avoid hardcoding colors!
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.colorOnPrimary, typedValue, true);
        @ColorInt int color = typedValue.data;

        songTitleTextView.setText(song.getTitle());
        if (getBindingAdapterPosition() < songsData.getPlayingIndex())
            songTitleTextView.setTextColor(Color.GRAY);
        else
            songTitleTextView.setTextColor(color);
        visualizer.release();
        if (isPlaying()) {
            visualizer.setBackground(null);

            int audioSessionID = MediaPlayerUtil.getAudioSessionId();
            if (audioSessionID != -1 && audioSessionID != 0)
                visualizer.setAudioSessionId(audioSessionID);
            visualizer.show();
        } else {
            visualizer.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_drag_handle, context.getTheme()));
            visualizer.hide();
        }
    }

    public boolean isPlaying() {
        return song != null && song.equals(songsData.getSongPlaying());
    }

    @Override
    public void onItemClicked(View view) {
        super.onItemClicked(view);
        onItemClickedListener.onItemClicked(getBindingAdapterPosition());
        bind(songsData.getSongPlaying());

    }


    public void releaseVisualizer() {
        if (visualizer != null)
            visualizer.release();
    }
}
