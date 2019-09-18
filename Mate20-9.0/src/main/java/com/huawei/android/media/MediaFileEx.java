package com.huawei.android.media;

import android.media.MediaFile;

public class MediaFileEx {
    public static String getMimeTypeForFile(String path) {
        return MediaFile.getMimeTypeForFile(path);
    }

    public static int getFileTypeForMimeType(String mimeType) {
        return MediaFile.getFileTypeForMimeType(mimeType);
    }
}
