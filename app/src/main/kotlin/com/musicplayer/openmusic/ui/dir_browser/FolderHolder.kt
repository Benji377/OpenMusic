package com.musicplayer.openmusic.ui.dir_browser

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.openmusic.R
import com.musicplayer.openmusic.ui.dir_browser.FolderAdapter.AdapterListeners
import it.sephiroth.android.library.checkbox3state.CheckBox3
import java.io.File

class FolderHolder(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    private val adapterListeners: AdapterListeners,
    private val folderCallBack: FolderCallBack
) : RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item_dir_browser, parent, false)) {
    private val dirNameTextView: TextView = itemView.findViewById(R.id.textview_dir_item_name)
    val selectedCheckBox: CheckBox3 = itemView.findViewById(R.id.checkbox_dir_item_selected)
    var folder: File? = null

    init {
        itemView.setOnClickListener { adapterListeners.onItemClicked(this) }
        itemView.setOnLongClickListener { adapterListeners.onItemLongClicked(this) }
        selectedCheckBox.setOnClickListener { adapterListeners.onCheckBoxClicked(this) }
        (selectedCheckBox.parent as View).setOnClickListener {
            adapterListeners.onItemLongClicked(
                this
            )
        }
    }

    fun bind(folder: File) {
        this.folder = folder
        if (!folder.canRead() || folder.list() == null) {
            dirNameTextView.setTextColor(Color.DKGRAY)
            selectedCheckBox.isEnabled = false
            return
        }

        //dirNameTextView.setTextColor(itemView.getContext().getColor(R.color.white));
        selectedCheckBox.isEnabled = true
        setSelectedCheckBoxState()
        val name = folder.name
        if (folderCallBack.isAtRoot) {
            if (name == "0") dirNameTextView.setText(R.string.dir_browser_internal_storage) else dirNameTextView.text =
                itemView.context.getString(R.string.dir_browser_sd_card, name)
        } else dirNameTextView.text = name
    }

    private fun setSelectedCheckBoxState() {
        if (folderCallBack.isFolderSelected(folder!!)) selectedCheckBox.setChecked(
            true,
            false
        ) //checked
        else if (folderCallBack.isSubFolderSelected(folder!!)) selectedCheckBox.setChecked(
            !selectedCheckBox.isChecked,
            true
        ) //indeterminate
        else selectedCheckBox.setChecked(false, false) //unchecked
    }

    interface FolderCallBack {
        fun isFolderSelected(folder: File): Boolean
        fun isSubFolderSelected(folder: File): Boolean
        val isAtRoot: Boolean
    }
}