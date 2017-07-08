package com.huawei.android.media;

import android.media.MediaScanner;

public class MediaScannerEx {
    public static void scanCustomDirectories(MediaScanner scanner, String[] directories, String volumeName, String[] whiteList, String[] blackList) {
        if (scanner != null) {
            scanner.scanCustomDirectories(directories, volumeName, whiteList, blackList);
        }
    }
}
