package com.musicplayer.openmusic.utils

object PathUtils {
    @JvmStatic
    fun getPathUp(path: String): String {
        val dirs = path.substring(1).split("/").toTypedArray()
        val upPath = StringBuilder("/")
        for (i in 0 until dirs.size - 1) upPath.append(dirs[i]).append("/")
        return upPath.toString()
    }

    @JvmStatic
    fun getPathDown(vararg paths: String): String {
        val builder = StringBuilder()
        builder.append(paths[0])
        for (i in 1 until paths.size) builder.append(paths[i]).append('/')
        return builder.toString()
    }

    /**
     * Tests if the path1 is a valid a subdirectory of path2, that is on any level below path2.
     *
     * @param path1 path of the subdirectory (to be tested)
     * @param path2 path of the parent directory (to be tested)
     * @return true if path1 is a valid a subdirectory of path2, false otherwise
     */
    @JvmStatic
    fun isSubDir(path1: String, path2: String): Boolean {
        val dirs1 = path1.split("/").toTypedArray()
        val dirs2 = path2.split("/").toTypedArray()
        if (dirs1.size <= dirs2.size) return false
        for (i in dirs2.indices) {
            if (dirs1[i] != dirs2[i]) return false
        }
        return true
    }

    @JvmStatic
    fun isSubDirOfAny(path: String, pathsSet: Set<String>): Boolean {
        for (setPath in pathsSet) if (isSubDir(path, setPath)) return true
        return false
    }
}