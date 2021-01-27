package com.android.server.storage;

import android.app.usage.ExternalStorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.util.ArrayMap;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

public class FileCollector {
    private static final int AUDIO = 2;
    private static final Map<String, Integer> EXTENSION_MAP = new ArrayMap();
    private static final int IMAGES = 0;
    private static final int UNRECOGNIZED = -1;
    private static final int VIDEO = 1;

    @Retention(RetentionPolicy.SOURCE)
    private @interface FileTypes {
    }

    static {
        EXTENSION_MAP.put("aac", 2);
        EXTENSION_MAP.put("amr", 2);
        EXTENSION_MAP.put("awb", 2);
        EXTENSION_MAP.put("snd", 2);
        EXTENSION_MAP.put("flac", 2);
        EXTENSION_MAP.put("mp3", 2);
        EXTENSION_MAP.put("mpga", 2);
        EXTENSION_MAP.put("mpega", 2);
        EXTENSION_MAP.put("mp2", 2);
        EXTENSION_MAP.put("m4a", 2);
        EXTENSION_MAP.put("aif", 2);
        EXTENSION_MAP.put("aiff", 2);
        EXTENSION_MAP.put("aifc", 2);
        EXTENSION_MAP.put("gsm", 2);
        EXTENSION_MAP.put("mka", 2);
        EXTENSION_MAP.put("m3u", 2);
        EXTENSION_MAP.put("wma", 2);
        EXTENSION_MAP.put("wax", 2);
        EXTENSION_MAP.put("ra", 2);
        EXTENSION_MAP.put("rm", 2);
        EXTENSION_MAP.put("ram", 2);
        EXTENSION_MAP.put("pls", 2);
        EXTENSION_MAP.put("sd2", 2);
        EXTENSION_MAP.put("wav", 2);
        EXTENSION_MAP.put("ogg", 2);
        EXTENSION_MAP.put("oga", 2);
        EXTENSION_MAP.put("3gpp", 1);
        EXTENSION_MAP.put("3gp", 1);
        EXTENSION_MAP.put("3gpp2", 1);
        EXTENSION_MAP.put("3g2", 1);
        EXTENSION_MAP.put("avi", 1);
        EXTENSION_MAP.put("dl", 1);
        EXTENSION_MAP.put("dif", 1);
        EXTENSION_MAP.put("dv", 1);
        EXTENSION_MAP.put("fli", 1);
        EXTENSION_MAP.put("m4v", 1);
        EXTENSION_MAP.put("ts", 1);
        EXTENSION_MAP.put("mpeg", 1);
        EXTENSION_MAP.put("mpg", 1);
        EXTENSION_MAP.put("mpe", 1);
        EXTENSION_MAP.put("mp4", 1);
        EXTENSION_MAP.put("vob", 1);
        EXTENSION_MAP.put("qt", 1);
        EXTENSION_MAP.put("mov", 1);
        EXTENSION_MAP.put("mxu", 1);
        EXTENSION_MAP.put("webm", 1);
        EXTENSION_MAP.put("lsf", 1);
        EXTENSION_MAP.put("lsx", 1);
        EXTENSION_MAP.put("mkv", 1);
        EXTENSION_MAP.put("mng", 1);
        EXTENSION_MAP.put("asf", 1);
        EXTENSION_MAP.put("asx", 1);
        EXTENSION_MAP.put("wm", 1);
        EXTENSION_MAP.put("wmv", 1);
        EXTENSION_MAP.put("wmx", 1);
        EXTENSION_MAP.put("wvx", 1);
        EXTENSION_MAP.put("movie", 1);
        EXTENSION_MAP.put("wrf", 1);
        EXTENSION_MAP.put("bmp", 0);
        EXTENSION_MAP.put("gif", 0);
        EXTENSION_MAP.put("jpg", 0);
        EXTENSION_MAP.put("jpeg", 0);
        EXTENSION_MAP.put("jpe", 0);
        EXTENSION_MAP.put("pcx", 0);
        EXTENSION_MAP.put("png", 0);
        EXTENSION_MAP.put("svg", 0);
        EXTENSION_MAP.put("svgz", 0);
        EXTENSION_MAP.put("tiff", 0);
        EXTENSION_MAP.put("tif", 0);
        EXTENSION_MAP.put("wbmp", 0);
        EXTENSION_MAP.put("webp", 0);
        EXTENSION_MAP.put("dng", 0);
        EXTENSION_MAP.put("cr2", 0);
        EXTENSION_MAP.put("ras", 0);
        EXTENSION_MAP.put("art", 0);
        EXTENSION_MAP.put("jng", 0);
        EXTENSION_MAP.put("nef", 0);
        EXTENSION_MAP.put("nrw", 0);
        EXTENSION_MAP.put("orf", 0);
        EXTENSION_MAP.put("rw2", 0);
        EXTENSION_MAP.put("pef", 0);
        EXTENSION_MAP.put("psd", 0);
        EXTENSION_MAP.put("pnm", 0);
        EXTENSION_MAP.put("pbm", 0);
        EXTENSION_MAP.put("pgm", 0);
        EXTENSION_MAP.put("ppm", 0);
        EXTENSION_MAP.put("srw", 0);
        EXTENSION_MAP.put("arw", 0);
        EXTENSION_MAP.put("rgb", 0);
        EXTENSION_MAP.put("xbm", 0);
        EXTENSION_MAP.put("xpm", 0);
        EXTENSION_MAP.put("xwd", 0);
    }

    public static MeasurementResult getMeasurementResult(File path) {
        return collectFiles(StorageManager.maybeTranslateEmulatedPathToInternal(path), new MeasurementResult());
    }

    public static MeasurementResult getMeasurementResult(Context context) {
        MeasurementResult result = new MeasurementResult();
        try {
            ExternalStorageStats stats = ((StorageStatsManager) context.getSystemService("storagestats")).queryExternalStatsForUser(StorageManager.UUID_PRIVATE_INTERNAL, UserHandle.of(context.getUserId()));
            result.imagesSize = stats.getImageBytes();
            result.videosSize = stats.getVideoBytes();
            result.audioSize = stats.getAudioBytes();
            result.miscSize = ((stats.getTotalBytes() - result.imagesSize) - result.videosSize) - result.audioSize;
            return result;
        } catch (IOException e) {
            throw new IllegalStateException("Could not query storage");
        }
    }

    public static long getSystemSize(Context context) {
        File sharedPath;
        VolumeInfo primaryVolume = context.getPackageManager().getPrimaryStorageCurrentVolume();
        StorageManager sm = (StorageManager) context.getSystemService("storage");
        VolumeInfo shared = sm.findEmulatedForPrivate(primaryVolume);
        if (shared == null || (sharedPath = shared.getPath()) == null) {
            return 0;
        }
        long systemSize = sm.getPrimaryStorageSize() - sharedPath.getTotalSpace();
        if (systemSize <= 0) {
            return 0;
        }
        return systemSize;
    }

    private static MeasurementResult collectFiles(File file, MeasurementResult result) {
        File[] files = file.listFiles();
        if (files == null) {
            return result;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                try {
                    collectFiles(f, result);
                } catch (StackOverflowError e) {
                    return result;
                }
            } else {
                handleFile(result, f);
            }
        }
        return result;
    }

    private static void handleFile(MeasurementResult result, File f) {
        long fileSize = f.length();
        int fileType = EXTENSION_MAP.getOrDefault(getExtensionForFile(f), -1).intValue();
        if (fileType == 0) {
            result.imagesSize += fileSize;
        } else if (fileType == 1) {
            result.videosSize += fileSize;
        } else if (fileType != 2) {
            result.miscSize += fileSize;
        } else {
            result.audioSize += fileSize;
        }
    }

    private static String getExtensionForFile(File file) {
        String fileName = file.getName();
        int index = fileName.lastIndexOf(46);
        if (index == -1) {
            return "";
        }
        return fileName.substring(index + 1).toLowerCase();
    }

    public static class MeasurementResult {
        public long audioSize;
        public long imagesSize;
        public long miscSize;
        public long videosSize;

        public long totalAccountedSize() {
            return this.imagesSize + this.videosSize + this.miscSize + this.audioSize;
        }
    }
}
