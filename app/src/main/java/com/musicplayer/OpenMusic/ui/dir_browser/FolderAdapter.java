package com.musicplayer.OpenMusic.ui.dir_browser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class FolderAdapter extends RecyclerView.Adapter<FolderHolder> {
    private final Context context;
    private File[] directories;
    private AdapterListeners listeners;
    private FolderHolder.FolderCallBack folderCallBack;


    public FolderAdapter(Context context, File[] dirs) {
        this.context = context;
        setDirectories(dirs);
    }

    public void setDirectories(File[] files) {
        ArrayList<File> dirsFound = new ArrayList<>();
        for (File file : files) {
            if (file.exists() && file.isDirectory() && !file.isHidden())
                dirsFound.add(file);
        }
        this.directories = new File[dirsFound.size()];
        dirsFound.toArray(this.directories);
    }

    @NonNull
    @Override
    public FolderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return new FolderHolder(inflater, parent, this.listeners, this.folderCallBack);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderHolder holder, int position) {
        holder.bind(directories[position]);
    }

    @Override
    public int getItemCount() {
        return directories.length;
    }

    public void setListeners(AdapterListeners listeners) {
        this.listeners = listeners;
    }

    public void setFolderCallBack(FolderHolder.FolderCallBack folderCallBack) {
        this.folderCallBack = folderCallBack;
    }

    interface AdapterListeners {
        void onCheckBoxClicked(FolderHolder holder);

        void onItemClicked(FolderHolder holder);

        boolean onItemLongClicked(FolderHolder holder);
    }
}

