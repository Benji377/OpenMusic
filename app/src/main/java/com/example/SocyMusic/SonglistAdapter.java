package com.example.SocyMusic;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.SocyMusic.SocyMusic.SocyMusic;
import com.example.musicplayer.R;

public class SonglistAdapter extends BaseAdapter {
    private SocyMusic music;
    private Context context;
    private LayoutInflater inflater;

    public SonglistAdapter(SocyMusic music, Context context, LayoutInflater inflater){
        this.music = music;
        this.context = context;
        this.inflater = inflater;
    }

    @Override
    public int getCount() {
        return music.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View myView =  inflater.inflate(R.layout.list_item, null);
        myView.setBackgroundColor(Color.TRANSPARENT);
        TextView textsong = myView.findViewById(R.id.textsongname);
        textsong.setSelected(true);
        textsong.setText(music.at(position).getName());

        return myView;
    }
}
