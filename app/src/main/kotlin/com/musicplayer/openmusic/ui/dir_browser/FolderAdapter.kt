package com.musicplayer.openmusic.ui.dir_browser

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.openmusic.ui.dir_browser.FolderHolder.FolderCallBack
import java.io.File

class FolderAdapter(private val context: Context, dirs: Array<File?>) :
    RecyclerView.Adapter<FolderHolder>() {
    private var directories: Array<File?>? = null
    private var listeners: AdapterListeners? = null
    private var folderCallBack: FolderCallBack? = null

    init {
        setDirectories(dirs)
    }

    fun setDirectories(files: Array<File?>) {
        val dirsFound = ArrayList<File>()
        for (file in files) {
            if (file!!.exists() && file.isDirectory && !file.isHidden) dirsFound.add(file)
        }
        directories = arrayOfNulls(dirsFound.size)
        dirsFound.toArray(directories!!)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderHolder {
        val inflater = LayoutInflater.from(context)
        return FolderHolder(inflater, parent, listeners!!, folderCallBack!!)
    }

    override fun onBindViewHolder(holder: FolderHolder, position: Int) {
        holder.bind(directories!![position]!!)
    }

    override fun getItemCount(): Int {
        return directories!!.size
    }

    fun setListeners(listeners: AdapterListeners) {
        this.listeners = listeners
    }

    fun setFolderCallBack(folderCallBack: FolderCallBack) {
        this.folderCallBack = folderCallBack
    }

    interface AdapterListeners {
        fun onCheckBoxClicked(holder: FolderHolder)
        fun onItemClicked(holder: FolderHolder)
        fun onItemLongClicked(holder: FolderHolder): Boolean
    }
}