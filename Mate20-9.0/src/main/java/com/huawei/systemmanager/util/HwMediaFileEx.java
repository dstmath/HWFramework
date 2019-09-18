package com.huawei.systemmanager.util;

import android.media.MediaFile;

public class HwMediaFileEx {

    public static class HwMediaFileTypeEx {
        public static final int AUDIO_FILE = 1;
        public static final int IMAGE_FILE = 3;
        public static final int UNKNOWN_FILE = 0;
        public static final int VIDEO_FILE = 2;
        final MediaFile.MediaFileType mInnerFileType;

        public HwMediaFileTypeEx(MediaFile.MediaFileType type) {
            this.mInnerFileType = type;
        }

        public int getFileType() {
            if (this.mInnerFileType == null) {
                return 0;
            }
            return this.mInnerFileType.fileType;
        }

        public String getMimeType() {
            if (this.mInnerFileType == null) {
                return "";
            }
            return this.mInnerFileType.mimeType;
        }
    }

    public static HwMediaFileTypeEx getFileType(String path) {
        if (MediaFile.getFileType(path) == null) {
            return null;
        }
        return new HwMediaFileTypeEx(MediaFile.getFileType(path));
    }

    public static int getFileTypeForInt(String path) {
        MediaFile.MediaFileType type = MediaFile.getFileType(path);
        if (type != null) {
            int fileType = type.fileType;
            if (MediaFile.isAudioFileType(fileType)) {
                return 1;
            }
            if (MediaFile.isVideoFileType(fileType)) {
                return 2;
            }
            if (MediaFile.isImageFileType(fileType)) {
                return 3;
            }
        }
        return 0;
    }
}
