package com.musicplayer.SocyMusic.ui.dir_browser;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.musicplayer.SocyMusic.SocyMusicApp;
import com.musicplayer.SocyMusic.custom_views.CustomRecyclerView;
import com.musicplayer.SocyMusic.data.SongsData;
import com.musicplayer.SocyMusic.utils.PathUtils;
import com.musicplayer.musicplayer.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import hendrawd.storageutil.library.StorageUtil;
import it.sephiroth.android.library.checkbox3state.CheckBox3;

public class DirBrowserFragment extends Fragment {
    private CustomRecyclerView foldersRecyclerview;

    private TextView symPathTextView;

    private File[] rootDirs;
    private String currentAbsolutePath;
    private boolean isAtRoot;
    private Set<String> savedPaths;
    private Set<String> initialSavedPaths;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootDirs = getRootDirs();
        currentAbsolutePath = PathUtils.getPathUp(rootDirs[0].getAbsolutePath());
        isAtRoot = true;
        savedPaths = new HashSet<>(PreferenceManager.getDefaultSharedPreferences(requireContext()).getStringSet(SocyMusicApp.PREFS_KEY_LIBRARY_PATHS, SocyMusicApp.defaultPathsSet));
        initialSavedPaths = new HashSet<>(savedPaths);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dir_browser, container, false);
        symPathTextView = view.findViewById(R.id.textview_dir_browser_sym_path);
        symPathTextView.setText("/");

        foldersRecyclerview = view.findViewById(R.id.recyclerview_dir_browser);
        FolderAdapter adapter = new FolderAdapter(requireContext(), rootDirs);
        adapter.setFolderCallBack(new FolderHolder.FolderCallBack() {
            @Override
            public boolean isFolderSelected(File folder) {
                //if folder itself is in the saved paths
                if (savedPaths.contains(folder.getAbsolutePath()))
                    return true;
                // or if folder's parent is in the saved paths
                for (String path : savedPaths) {
                    if (PathUtils.isSubDir(folder.getAbsolutePath(), path))
                        return true;
                }
                return false;
            }

            @Override
            public boolean isSubFolderSelected(File folder) {
                //if any of the folder's subfolders is in the saved paths
                for (String path : savedPaths) {
                    if (PathUtils.isSubDir(path, folder.getAbsolutePath()))
                        return true;
                }
                return false;
            }

            @Override
            public boolean isAtRoot() {
                return isAtRoot;
            }
        });

        adapter.setListeners(new FolderAdapter.AdapterListeners() {
            @Override
            public void onCheckBoxClicked(FolderHolder holder) {
                CheckBox3 selectedCheckBox = holder.getSelectedCheckBox();
                File folder = holder.getFolder();
                if (selectedCheckBox.isChecked())
                    savedPaths.add(folder.getAbsolutePath());
                else
                    savedPaths.remove(folder.getAbsolutePath());

                //remove sub-dirs (whether this dir was added or removed)
                ArrayList<String> pathsToRemove = new ArrayList<>();
                for (String savedPath : savedPaths) {
                    if (PathUtils.isSubDir(savedPath, folder.getAbsolutePath()))
                        pathsToRemove.add(savedPath);
                }
                savedPaths.removeAll(pathsToRemove);
                save();
            }

            @Override
            public void onItemClicked(FolderHolder holder) {
                File folder = holder.getFolder();
                File[] subDirectories = folder.listFiles();
                if (!folder.canRead() || subDirectories == null) {
                    Toast.makeText(holder.itemView.getContext(), R.string.dir_browser_folder_unreadable, Toast.LENGTH_SHORT).show();
                    return;
                }
                adapter.setDirectories(subDirectories);
                adapter.notifyDataSetChanged();
                symPathTextView.setText(PathUtils.getPathDown((String) symPathTextView.getText(), folder.getName().equals("0") ? getString(R.string.dir_browser_internal_storage) : folder.getName()));
                currentAbsolutePath = folder.getAbsolutePath();
                isAtRoot = false;
            }

            @Override
            public boolean onItemLongClicked(FolderHolder holder) {
                CheckBox3 selectedCheckBox = holder.getSelectedCheckBox();
                selectedCheckBox.toggle();
                onCheckBoxClicked(holder);
                return true;
            }
        });


        foldersRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        foldersRecyclerview.setAdapter(adapter);
        foldersRecyclerview.setEmptyView(view.findViewById(R.id.textview_dir_browser_list_empty));

        return view;
    }

    protected void onBackPressed() {
        if (isAtRoot) {
            Activity hostActivity=requireActivity();
            hostActivity.setResult(hasChangedLibrary() ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
            hostActivity.finish();
        } else {
            symPathTextView.setText(PathUtils.getPathUp((String) symPathTextView.getText()));
            currentAbsolutePath = PathUtils.getPathUp(currentAbsolutePath);
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
            assert adapter != null;
            adapter.setDirectories(subDirectories);
            adapter.notifyDataSetChanged();
        }
    }


    private File[] getRootDirs() {
        String[] rootPaths = getRootPaths();
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

    private void save() {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit()
                .putStringSet(SocyMusicApp.PREFS_KEY_LIBRARY_PATHS, savedPaths)
                .apply();
    }

    public boolean hasChangedLibrary() {
        return !savedPaths.equals(initialSavedPaths);
    }
}
