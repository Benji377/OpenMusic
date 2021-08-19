package com.musicplayer.SocyMusic.ui.dir_browser;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.musicplayer.musicplayer.R;

import java.io.File;

import it.sephiroth.android.library.checkbox3state.CheckBox3;

public class FolderHolder extends RecyclerView.ViewHolder {
    private final FolderCallBack folderCallBack;
    private File folder;
    private final TextView dirNameTextView;
    private final CheckBox3 selectedCheckBox;
    private final FolderAdapter.AdapterListeners adapterListeners;

    public FolderHolder(LayoutInflater inflater, ViewGroup parent, FolderAdapter.AdapterListeners listeners, FolderCallBack folderCallBack) {
        super(inflater.inflate(R.layout.list_item_dir_browser, parent, false));
        this.adapterListeners = listeners;
        this.folderCallBack = folderCallBack;
        dirNameTextView = itemView.findViewById(R.id.textview_dir_item_name);
        selectedCheckBox = itemView.findViewById(R.id.checkbox_dir_item_selected);
        itemView.setOnClickListener(v -> adapterListeners.onItemClicked(this));
        itemView.setOnLongClickListener(v -> adapterListeners.onItemLongClicked(this));
        selectedCheckBox.setOnClickListener(v -> adapterListeners.onCheckBoxClicked(this));
        ((View) selectedCheckBox.getParent()).setOnClickListener(v -> adapterListeners.onItemLongClicked(this));

    }

    public void bind(File folder) {
        this.folder = folder;
        if (!folder.canRead() || folder.list() == null) {
            dirNameTextView.setTextColor(Color.DKGRAY);
            selectedCheckBox.setEnabled(false);
            return;
        }

        //dirNameTextView.setTextColor(itemView.getContext().getColor(R.color.white));
        selectedCheckBox.setEnabled(true);

        setSelectedCheckBoxState();
        String name = folder.getName();
        if (folderCallBack.isAtRoot()) {
            if (name.equals("0"))
                dirNameTextView.setText(R.string.dir_browser_internal_storage);
            else
                dirNameTextView.setText(itemView.getContext().getString(R.string.dir_browser_sd_card, name));
        } else
            dirNameTextView.setText(name);
    }

    private void setSelectedCheckBoxState() {
        if (folderCallBack.folderSelected(folder))
            selectedCheckBox.setChecked(true, false);   //checked
        else if (folderCallBack.subFolderSelected(folder))
            selectedCheckBox.setChecked(!selectedCheckBox.isChecked(), true);    //indeterminate
        else
            selectedCheckBox.setChecked(false, false);  //unchecked
    }

    public File getFolder() {
        return folder;
    }

    public CheckBox3 getSelectedCheckBox() {
        return selectedCheckBox;
    }

    interface FolderCallBack {
        boolean folderSelected(File folder);

        boolean subFolderSelected(File folder);

        boolean isAtRoot();
    }
}