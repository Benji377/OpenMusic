package com.musicplayer.SocyMusic.ui.all_songs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.musicplayer.SocyMusic.data.Song;
import com.musicplayer.musicplayer.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom adapter for SongsData related actions
 */
public class SongListAdapter extends RecyclerView.Adapter<SongHolder> implements Filterable {
    private final Context context;
    private List<Song> allSongs;
    private ItemClickListener clickListener;

    private ArrayList<Song> filteredData;

    public SongListAdapter(Context context, List<Song> allSongs) {
        super();
        this.allSongs = allSongs;
        this.context = context;
        filteredData = (ArrayList<Song>) allSongs;

    }

    @NonNull
    @Override
    public SongHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.list_item_all_songs, parent, false);
        return new SongHolder(itemView, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SongHolder holder, int position) {
        Song song = filteredData.get(position);
        holder.bind(song);
    }

    @Override
    public int getItemCount() {
        return allSongs.size();
    }

    public void setOnItemClickListener(ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setAllSongs(List<Song> allSongs) {
        this.allSongs = allSongs;
    }

    // This filter will select the searched sog and display it
    // TODO: Not working properly, need fixing
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults results = new FilterResults();

                //If there's nothing to filter on, return the original data for your list
                if(charSequence == null || charSequence.length() == 0) {
                    results.values = allSongs;
                    results.count = allSongs.size();
                } else {
                    ArrayList<Song> filterResultsData = new ArrayList<>();

                    for(Song data : allSongs) {
                        //In this loop, you'll filter through originalData and compare each item to charSequence.
                        //If you find a match, add it to your new ArrayList
                        //I'm not sure how you're going to do comparison, so you'll need to fill out this conditional
                        if(data.getTitle().contentEquals(charSequence)) {
                            filterResultsData.add(data);
                        }
                    }

                    results.values = filterResultsData;
                    results.count = filterResultsData.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults)
            {
                filteredData = (ArrayList<Song>)filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface ItemClickListener {
        void onItemClick(int position, View view);

        boolean onItemLongClick(int position, View view);
    }

}