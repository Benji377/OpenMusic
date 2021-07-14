package com.musicplayer.SocyMusic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.musicplayer.musicplayer.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import hendrawd.storageutil.library.StorageUtil;

public class DirBrowserFragment extends Fragment {
    private RecyclerView foldersRecyclerview;

    private TextView symPathTextView;

    private String[] rootPaths;
    private File[] rootDirs;
    private String currentAbsolutePath;
    private boolean isAtRoot;
    private Set<String> savedPaths;
    private SongsData songsData;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        songsData = SongsData.getInstance(getContext());
        rootPaths = getRootPaths();
        rootDirs = getRootDirs();
        currentAbsolutePath = getUpPath(rootDirs[0].getAbsolutePath());
        isAtRoot = true;
        savedPaths = new HashSet<>(PreferenceManager.getDefaultSharedPreferences(requireContext()).getStringSet(SocyMusicApp.PREFS_KEY_LIBRARY_PATHS, SocyMusicApp.defaultPathsSet));
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dir_browser, container, false);
        symPathTextView = view.findViewById(R.id.textview_dir_browser_sym_path);
        symPathTextView.setText("/");

        foldersRecyclerview = view.findViewById(R.id.recyclerview_dir_browser);
        FolderAdapter adapter = new FolderAdapter(rootDirs);
        foldersRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        foldersRecyclerview.setAdapter(adapter);

        //TODO: foldersRecyclerview.setEmptyView(view.findViewById(R.id.textview_dir_browser_list_empty));

        return view;
    }

    protected void onBackPressed() {
        if (isAtRoot)
            requireActivity().finish();
        else {
            symPathTextView.setText(getUpPath((String) symPathTextView.getText()));
            currentAbsolutePath = getUpPath(currentAbsolutePath);
            isAtRoot = symPathTextView.getText().equals("/");
            File current = new File(currentAbsolutePath);
            File[] subDirectories;
            if (isAtRoot)
                subDirectories = rootDirs;
            else
                subDirectories = current.listFiles();
            if (subDirectories == null)
                return;
            FolderAdapter adapter = (FolderAdapter) foldersRecyclerview.getAdapter();
            adapter.setDirectories(subDirectories);
            adapter.notifyDataSetChanged();
        }
    }

    private String getUpPath(String path) {
        String[] dirs = path.substring(1).split("/");
        StringBuilder upPath = new StringBuilder("/");
        for (int i = 0; i < dirs.length - 1; i++)
            upPath.append(dirs[i]).append("/");
        return upPath.toString();
    }

    private String getDownPath(String path, String folder) {
        return path + folder + "/";
    }

    private File[] getRootDirs() {
        File[] rootDirs = new File[rootPaths.length];
        for (int i = 0; i < rootPaths.length; i++)
            rootDirs[i] = new File(rootPaths[i]);
        return rootDirs;
    }

    private String[] getRootPaths() {
        String[] rootPaths = StorageUtil.getStorageDirectories(getContext());

        //sort to put internal storage at the top
        String top = rootPaths[0];
        for (int i = 0; i < rootPaths.length; i++) {
            if (Arrays.asList(rootPaths[i].split("/")).contains("0")) {
                rootPaths[0] = rootPaths[i];
                rootPaths[i] = top;
            }
        }
        return rootPaths;
    }

    public void save() {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit()
                .putStringSet(SocyMusicApp.PREFS_KEY_LIBRARY_PATHS, savedPaths)
                .apply();
        songsData.reloadSongs(requireContext());
        requireActivity().finish();
    }


    private class FolderAdapter extends RecyclerView.Adapter<FolderHolder> {
        private File[] directories;

        public FolderAdapter(File[] dirs) {
            setDirectories(dirs);
        }

        public void setDirectories(File[] files) {
            ArrayList<File> dirsFound = new ArrayList<>();
            for (File file : files) {
                if (file.exists() && file.isDirectory())
                    dirsFound.add(file);
            }
            this.directories = new File[dirsFound.size()];
            dirsFound.toArray(this.directories);
        }

        @NonNull
        @Override
        public FolderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            return new FolderHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull FolderHolder holder, int position) {
            holder.bind(directories[position]);
        }

        @Override
        public int getItemCount() {
            return directories.length;
        }
    }

    private class FolderHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private File folder;
        private TextView dirNameTextView;
        private CheckBox selectedCheckBox;

        public FolderHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_dir_browser, parent, false));
            dirNameTextView = itemView.findViewById(R.id.textview_dir_item_name);
            selectedCheckBox = itemView.findViewById(R.id.checkbox_dir_item_selected);
            itemView.setOnClickListener(this);
            selectedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked)
                    savedPaths.add(folder.getAbsolutePath());
                else
                    savedPaths.remove(folder.getAbsolutePath());
            });
        }

        public void bind(File folder) {
            this.folder = folder;
            //TODO: check the selected checkbox if the dir is a sub-dir of a dir in savedPaths
            selectedCheckBox.setChecked(savedPaths.contains(folder.getAbsolutePath()));
            String name = folder.getName();
            if (isAtRoot) {
                if (name.equals("0"))
                    dirNameTextView.setText(R.string.dir_browser_internal_storage);
                else
                    dirNameTextView.setText(getString(R.string.dir_browser_sd_card, name));
            } else
                dirNameTextView.setText(name);
        }

        @Override
        public void onClick(View v) {
            FolderAdapter adapter = (FolderAdapter) getBindingAdapter();
            if (adapter == null)
                return;

            File[] subDirectories = folder.listFiles();
            if (!folder.canRead() || subDirectories == null) {
                Toast.makeText(getContext(), R.string.dir_browser_file_not_readable, Toast.LENGTH_SHORT).show();
                return;
            }
            adapter.setDirectories(subDirectories);
            adapter.notifyDataSetChanged();
            symPathTextView.setText(getDownPath((String) symPathTextView.getText(), folder.getName().equals("0") ? getString(R.string.dir_browser_internal_storage) : folder.getName()));
            currentAbsolutePath = folder.getAbsolutePath();
            isAtRoot = false;
        }
    }
}
