package com.musicplayer.OpenMusic.utils

import android.content.Context
import android.util.TypedValue

object UiUtils {
    @JvmStatic
    fun dpToPixel(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}