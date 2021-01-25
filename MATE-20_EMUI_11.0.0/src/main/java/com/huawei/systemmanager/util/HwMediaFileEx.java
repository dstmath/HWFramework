package com.huawei.systemmanager.util;

import android.media.MediaFile;

public class HwMediaFileEx {
    @Deprecated
    public static HwMediaFileTypeEx getFileType(String path) {
        return null;
    }

    public static int getFileTypeForInt(String path) {
        String mimeType = MediaFile.getMimeTypeForFile(path);
        if (mimeType == null) {
            return 0;
        }
        if (MediaFile.isAudioMimeType(mimeType)) {
            return 1;
        }
        if (MediaFile.isVideoMimeType(mimeType)) {
            return 2;
        }
        if (MediaFile.isImageMimeType(mimeType)) {
            return 3;
        }
        return 0;
    }

    public static class HwMediaFileTypeEx {
        public static final int AUDIO_FILE = 1;
        public static final int IMAGE_FILE = 3;
        public static final int UNKNOWN_FILE = 0;
        public static final int VIDEO_FILE = 2;
        final MediaFile.MediaFileType mInnerFileType;

        @Deprecated
        public HwMediaFileTypeEx(MediaFile.MediaFileType type) {
            this.mInnerFileType = type;
        }

        @Deprecated
        public int getFileType() {
            MediaFile.MediaFileType mediaFileType = this.mInnerFileType;
            if (mediaFileType == null) {
                return 0;
            }
            return mediaFileType.fileType;
        }

        @Deprecated
        public String getMimeType() {
            MediaFile.MediaFileType mediaFileType = this.mInnerFileType;
            if (mediaFileType == null) {
                return "";
            }
            return mediaFileType.mimeType;
        }
    }
}
