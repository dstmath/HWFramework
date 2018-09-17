package com.android.server.storage;

import android.annotation.IntDef;
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

    @IntDef({-1, 0, 1, 2})
    @Retention(RetentionPolicy.SOURCE)
    private @interface FileTypes {
    }

    public static class MeasurementResult {
        public long audioSize;
        public long imagesSize;
        public long miscSize;
        public long videosSize;

        public long totalAccountedSize() {
            return ((this.imagesSize + this.videosSize) + this.miscSize) + this.audioSize;
        }
    }

    static {
        EXTENSION_MAP.put("aac", Integer.valueOf(2));
        EXTENSION_MAP.put("amr", Integer.valueOf(2));
        EXTENSION_MAP.put("awb", Integer.valueOf(2));
        EXTENSION_MAP.put("snd", Integer.valueOf(2));
        EXTENSION_MAP.put("flac", Integer.valueOf(2));
        EXTENSION_MAP.put("mp3", Integer.valueOf(2));
        EXTENSION_MAP.put("mpga", Integer.valueOf(2));
        EXTENSION_MAP.put("mpega", Integer.valueOf(2));
        EXTENSION_MAP.put("mp2", Integer.valueOf(2));
        EXTENSION_MAP.put("m4a", Integer.valueOf(2));
        EXTENSION_MAP.put("aif", Integer.valueOf(2));
        EXTENSION_MAP.put("aiff", Integer.valueOf(2));
        EXTENSION_MAP.put("aifc", Integer.valueOf(2));
        EXTENSION_MAP.put("gsm", Integer.valueOf(2));
        EXTENSION_MAP.put("mka", Integer.valueOf(2));
        EXTENSION_MAP.put("m3u", Integer.valueOf(2));
        EXTENSION_MAP.put("wma", Integer.valueOf(2));
        EXTENSION_MAP.put("wax", Integer.valueOf(2));
        EXTENSION_MAP.put("ra", Integer.valueOf(2));
        EXTENSION_MAP.put("rm", Integer.valueOf(2));
        EXTENSION_MAP.put("ram", Integer.valueOf(2));
        EXTENSION_MAP.put("pls", Integer.valueOf(2));
        EXTENSION_MAP.put("sd2", Integer.valueOf(2));
        EXTENSION_MAP.put("wav", Integer.valueOf(2));
        EXTENSION_MAP.put("ogg", Integer.valueOf(2));
        EXTENSION_MAP.put("oga", Integer.valueOf(2));
        EXTENSION_MAP.put("3gpp", Integer.valueOf(1));
        EXTENSION_MAP.put("3gp", Integer.valueOf(1));
        EXTENSION_MAP.put("3gpp2", Integer.valueOf(1));
        EXTENSION_MAP.put("3g2", Integer.valueOf(1));
        EXTENSION_MAP.put("avi", Integer.valueOf(1));
        EXTENSION_MAP.put("dl", Integer.valueOf(1));
        EXTENSION_MAP.put("dif", Integer.valueOf(1));
        EXTENSION_MAP.put("dv", Integer.valueOf(1));
        EXTENSION_MAP.put("fli", Integer.valueOf(1));
        EXTENSION_MAP.put("m4v", Integer.valueOf(1));
        EXTENSION_MAP.put("ts", Integer.valueOf(1));
        EXTENSION_MAP.put("mpeg", Integer.valueOf(1));
        EXTENSION_MAP.put("mpg", Integer.valueOf(1));
        EXTENSION_MAP.put("mpe", Integer.valueOf(1));
        EXTENSION_MAP.put("mp4", Integer.valueOf(1));
        EXTENSION_MAP.put("vob", Integer.valueOf(1));
        EXTENSION_MAP.put("qt", Integer.valueOf(1));
        EXTENSION_MAP.put("mov", Integer.valueOf(1));
        EXTENSION_MAP.put("mxu", Integer.valueOf(1));
        EXTENSION_MAP.put("webm", Integer.valueOf(1));
        EXTENSION_MAP.put("lsf", Integer.valueOf(1));
        EXTENSION_MAP.put("lsx", Integer.valueOf(1));
        EXTENSION_MAP.put("mkv", Integer.valueOf(1));
        EXTENSION_MAP.put("mng", Integer.valueOf(1));
        EXTENSION_MAP.put("asf", Integer.valueOf(1));
        EXTENSION_MAP.put("asx", Integer.valueOf(1));
        EXTENSION_MAP.put("wm", Integer.valueOf(1));
        EXTENSION_MAP.put("wmv", Integer.valueOf(1));
        EXTENSION_MAP.put("wmx", Integer.valueOf(1));
        EXTENSION_MAP.put("wvx", Integer.valueOf(1));
        EXTENSION_MAP.put("movie", Integer.valueOf(1));
        EXTENSION_MAP.put("wrf", Integer.valueOf(1));
        EXTENSION_MAP.put("bmp", Integer.valueOf(0));
        EXTENSION_MAP.put("gif", Integer.valueOf(0));
        EXTENSION_MAP.put("jpg", Integer.valueOf(0));
        EXTENSION_MAP.put("jpeg", Integer.valueOf(0));
        EXTENSION_MAP.put("jpe", Integer.valueOf(0));
        EXTENSION_MAP.put("pcx", Integer.valueOf(0));
        EXTENSION_MAP.put("png", Integer.valueOf(0));
        EXTENSION_MAP.put("svg", Integer.valueOf(0));
        EXTENSION_MAP.put("svgz", Integer.valueOf(0));
        EXTENSION_MAP.put("tiff", Integer.valueOf(0));
        EXTENSION_MAP.put("tif", Integer.valueOf(0));
        EXTENSION_MAP.put("wbmp", Integer.valueOf(0));
        EXTENSION_MAP.put("webp", Integer.valueOf(0));
        EXTENSION_MAP.put("dng", Integer.valueOf(0));
        EXTENSION_MAP.put("cr2", Integer.valueOf(0));
        EXTENSION_MAP.put("ras", Integer.valueOf(0));
        EXTENSION_MAP.put("art", Integer.valueOf(0));
        EXTENSION_MAP.put("jng", Integer.valueOf(0));
        EXTENSION_MAP.put("nef", Integer.valueOf(0));
        EXTENSION_MAP.put("nrw", Integer.valueOf(0));
        EXTENSION_MAP.put("orf", Integer.valueOf(0));
        EXTENSION_MAP.put("rw2", Integer.valueOf(0));
        EXTENSION_MAP.put("pef", Integer.valueOf(0));
        EXTENSION_MAP.put("psd", Integer.valueOf(0));
        EXTENSION_MAP.put("pnm", Integer.valueOf(0));
        EXTENSION_MAP.put("pbm", Integer.valueOf(0));
        EXTENSION_MAP.put("pgm", Integer.valueOf(0));
        EXTENSION_MAP.put("ppm", Integer.valueOf(0));
        EXTENSION_MAP.put("srw", Integer.valueOf(0));
        EXTENSION_MAP.put("arw", Integer.valueOf(0));
        EXTENSION_MAP.put("rgb", Integer.valueOf(0));
        EXTENSION_MAP.put("xbm", Integer.valueOf(0));
        EXTENSION_MAP.put("xpm", Integer.valueOf(0));
        EXTENSION_MAP.put("xwd", Integer.valueOf(0));
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
        StorageManager sm = (StorageManager) context.getSystemService("storage");
        VolumeInfo shared = sm.findEmulatedForPrivate(context.getPackageManager().getPrimaryStorageCurrentVolume());
        if (shared == null) {
            return 0;
        }
        File sharedPath = shared.getPath();
        if (sharedPath == null) {
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
            }
            handleFile(result, f);
        }
        return result;
    }

    private static void handleFile(MeasurementResult result, File f) {
        long fileSize = f.length();
        switch (((Integer) EXTENSION_MAP.getOrDefault(getExtensionForFile(f), Integer.valueOf(-1))).intValue()) {
            case 0:
                result.imagesSize += fileSize;
                return;
            case 1:
                result.videosSize += fileSize;
                return;
            case 2:
                result.audioSize += fileSize;
                return;
            default:
                result.miscSize += fileSize;
                return;
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
}
