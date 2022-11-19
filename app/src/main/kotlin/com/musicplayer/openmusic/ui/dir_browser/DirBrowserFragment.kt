package com.musicplayer.openmusic.ui.dir_browser

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicplayer.musicplayer.R
import com.musicplayer.openmusic.OpenMusicApp
import com.musicplayer.openmusic.custom_views.CustomRecyclerView
import com.musicplayer.openmusic.ui.dir_browser.FolderAdapter.AdapterListeners
import com.musicplayer.openmusic.ui.dir_browser.FolderHolder.FolderCallBack
import com.musicplayer.openmusic.utils.PathUtils.getPathDown
import com.musicplayer.openmusic.utils.PathUtils.getPathUp
import com.musicplayer.openmusic.utils.PathUtils.isSubDir
import hendrawd.storageutil.library.StorageUtil
import java.io.File

class DirBrowserFragment : Fragment() {
    private var foldersRecyclerview: CustomRecyclerView? = null
    private var symPathTextView: TextView? = null
    private var rootDirs: Array<File?>? = null
    private var currentAbsolutePath: String? = null
    private var isAtRoot = false
    private var savedPaths: MutableSet<String>? = null
    private var initialSavedPaths: Set<String>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootDirs = getRootDirs()
        currentAbsolutePath = getPathUp(rootDirs!![0]!!.absolutePath)
        isAtRoot = true
        savedPaths = HashSet(
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getStringSet(OpenMusicApp.PREFS_KEY_LIBRARY_PATHS, OpenMusicApp.defaultPathsSet)!!
        )
        initialSavedPaths = HashSet(savedPaths!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dir_browser, container, false)
        symPathTextView = view.findViewById(R.id.textview_dir_browser_sym_path)
        symPathTextView?.text = "/"
        foldersRecyclerview = view.findViewById(R.id.recyclerview_dir_browser)
        val adapter = FolderAdapter(requireContext(), rootDirs!!)
        adapter.setFolderCallBack(object : FolderCallBack {
            override fun isFolderSelected(folder: File): Boolean {
                //if folder itself is in the saved paths
                if (savedPaths!!.contains(folder.absolutePath)) return true
                // or if folder's parent is in the saved paths
                for (path in savedPaths!!) {
                    if (isSubDir(folder.absolutePath, path)) return true
                }
                return false
            }

            override fun isSubFolderSelected(folder: File): Boolean {
                //if any of the folder's subfolders is in the saved paths
                for (path in savedPaths!!) {
                    if (isSubDir(path, folder.absolutePath)) return true
                }
                return false
            }

            override val isAtRoot: Boolean
                // TODO: This might generate problems in the future
                get() = false

        })
        adapter.setListeners(object : AdapterListeners {
            override fun onCheckBoxClicked(holder: FolderHolder) {
                val selectedCheckBox = holder.selectedCheckBox
                val folder = holder.folder
                if (selectedCheckBox.isChecked) savedPaths!!.add(folder!!.absolutePath) else savedPaths!!.remove(
                    folder?.absolutePath
                )

                //remove sub-dirs (whether this dir was added or removed)
                val pathsToRemove = ArrayList<String>()
                for (savedPath in savedPaths!!) {
                    if (isSubDir(savedPath, folder!!.absolutePath)) pathsToRemove.add(savedPath)
                }
                savedPaths!!.removeAll(pathsToRemove.toSet())
                save()
            }

            override fun onItemClicked(holder: FolderHolder) {
                val folder = holder.folder
                val subDirectories = folder?.listFiles()
                if (!folder!!.canRead() || subDirectories == null) {
                    Toast.makeText(
                        holder.itemView.context,
                        R.string.dir_browser_folder_unreadable,
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                adapter.setDirectories(subDirectories)
                adapter.notifyDataSetChanged()
                symPathTextView?.text = getPathDown(
                    (symPathTextView?.text as String),
                    if (folder.name == "0") getString(R.string.dir_browser_internal_storage) else folder.name
                )
                currentAbsolutePath = folder.absolutePath
                isAtRoot = false
            }

            override fun onItemLongClicked(holder: FolderHolder): Boolean {
                val selectedCheckBox = holder.selectedCheckBox
                selectedCheckBox.toggle()
                onCheckBoxClicked(holder)
                return true
            }
        })
        foldersRecyclerview?.layoutManager = LinearLayoutManager(context)
        foldersRecyclerview?.adapter = adapter
        foldersRecyclerview?.setEmptyView(view.findViewById(R.id.textview_dir_browser_list_empty))
        return view
    }

    fun onBackPressed() {
        if (isAtRoot) {
            val hostActivity: Activity = requireActivity()
            hostActivity.setResult(if (hasChangedLibrary()) Activity.RESULT_OK else Activity.RESULT_CANCELED)
            hostActivity.finish()
        } else {
            symPathTextView!!.text =
                getPathUp((symPathTextView!!.text as String))
            currentAbsolutePath = getPathUp(
                currentAbsolutePath!!
            )
            isAtRoot = symPathTextView!!.text == "/"
            val current = File(currentAbsolutePath!!)
            val subDirectories: Array<File?> = (if (isAtRoot) rootDirs else current.listFiles())
                ?: return
            val adapter = (foldersRecyclerview!!.adapter as FolderAdapter?)!!
            adapter.setDirectories(subDirectories)
            adapter.notifyDataSetChanged()
        }
    }

    private fun getRootDirs(): Array<File?> {
        val rootPaths = rootPaths
        val rootDirs = arrayOfNulls<File>(rootPaths.size)
        for (i in rootPaths.indices) rootDirs[i] = File(rootPaths[i])
        return rootDirs
    }

    //sort to put internal storage at the top
    private val rootPaths: Array<String>
        get() {
            val rootPaths = StorageUtil.getStorageDirectories(context)

            //sort to put internal storage at the top
            val top = rootPaths[0]
            for (i in rootPaths.indices) {
                if (listOf(*rootPaths[i].split("/").toTypedArray()).contains("0")) {
                    rootPaths[0] = rootPaths[i]
                    rootPaths[i] = top
                }
            }
            return rootPaths
        }

    private fun save() {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .edit()
            .putStringSet(OpenMusicApp.PREFS_KEY_LIBRARY_PATHS, savedPaths)
            .apply()
    }

    private fun hasChangedLibrary(): Boolean {
        return savedPaths != initialSavedPaths
    }
}