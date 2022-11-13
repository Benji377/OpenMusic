package com.musicplayer.OpenMusic.utils;

import java.util.Set;

public class PathUtils {
    public static String getPathUp(String path) {
        String[] dirs = path.substring(1).split("/");
        StringBuilder upPath = new StringBuilder("/");
        for (int i = 0; i < dirs.length - 1; i++)
            upPath.append(dirs[i]).append("/");
        return upPath.toString();
    }

    public static String getPathDown(String... paths) {
        StringBuilder builder = new StringBuilder();
        builder.append(paths[0]);
        for (int i = 1; i < paths.length; i++)
            builder.append(paths[i]).append('/');
        return builder.toString();
    }

    /**
     * Tests if the path1 is a valid a subdirectory of path2, that is on any level below path2.
     *
     * @param path1 path of the subdirectory (to be tested)
     * @param path2 path of the parent directory (to be tested)
     * @return true if path1 is a valid a subdirectory of path2, false otherwise
     */
    public static boolean isSubDir(String path1, String path2) {
        String[] dirs1 = path1.split("/");
        String[] dirs2 = path2.split("/");
        if (dirs1.length <= dirs2.length)
            return false;
        for (int i = 0; i < dirs2.length; i++) {
            if (!dirs1[i].equals(dirs2[i]))
                return false;
        }
        return true;
    }


    public static boolean isSubDirOfAny(String path, Set<String> pathsSet) {
        for (String setPath : pathsSet)
            if (isSubDir(path, setPath))
                return true;
        return false;
    }


}
