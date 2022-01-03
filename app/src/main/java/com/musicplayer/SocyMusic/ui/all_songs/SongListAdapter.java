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
import java.util.List;

/**
 * Custom adapter for SongsData related actions
 */
public class SongListAdapter extends RecyclerView.Adapter<SongHolder> implements Filterable {
    private final Context context;
    private List<Song> allSongs;
    private ItemClickListener clickListener;
    private List<Song> filteredList;

    public SongListAdapter(Context context, List<Song> allSongs) {
        super();
        this.allSongs = allSongs;
        this.context = context;
        filteredList = allSongs;

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
        Song song = filteredList.get(position);
        holder.bind(song);
    }

    @Override
    public int getItemCount() {
        return filteredList == null ? 0 : filteredList.size();
    }

    public void setOnItemClickListener(ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setAllSongs(List<Song> allSongs) {
        this.allSongs = allSongs;
    }

    // This filter will select the searched song and display it
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();

                if (charString.isEmpty()) {
                    filteredList = allSongs;
                } else {
                    filteredList.clear();
                    for (Song data : allSongs) {
                        //In this loop, you'll filter through originalData and compare each item to charSequence.
                        //If you find a match, add it to your new ArrayList
                        if (data.getTitle().toLowerCase().contains(charSequence.toString().toLowerCase())) {
                            filteredList.add(data);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredList = (List<Song>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface ItemClickListener {
        void onItemClick(int position, View view);

        boolean onItemLongClick(int position, View view);
    }

}