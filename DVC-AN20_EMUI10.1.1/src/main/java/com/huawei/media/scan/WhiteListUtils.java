package com.huawei.media.scan;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import com.huawei.android.provider.MediaStoreEx;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class WhiteListUtils {
    public static final int DIR_IS_WHITELIST = 2;
    public static final int DIR_MAYBE_WHITELIST = 1;
    public static final int DIR_NOT_WHITELIST = 0;
    private static final int OVER_TIME = 5000;
    private static final String TAG = "WhiteListUtils";
    private static final String WECHAT_FOLDER = "/tencent/MicroMsg/WeiXin/";
    private static final String WECHAT_FOLDER_NAME = "tencent";
    private static final String[] WHITE_FOLDER_NAMES = {Environment.DIRECTORY_MUSIC, Environment.DIRECTORY_PODCASTS, Environment.DIRECTORY_RINGTONES, Environment.DIRECTORY_ALARMS, Environment.DIRECTORY_NOTIFICATIONS, Environment.DIRECTORY_PICTURES, Environment.DIRECTORY_MOVIES, Environment.DIRECTORY_DOWNLOADS, Environment.DIRECTORY_DCIM, WECHAT_FOLDER_NAME};
    private Set<File> mCachedWhiteListScanPaths = new HashSet();
    private final Context mContext;
    private MediaServiceProxy mMediaService;
    private boolean mState;

    public WhiteListUtils(Context context, MediaServiceProxy proxy) {
        this.mContext = context;
        this.mMediaService = proxy;
    }

    public void scanWhiteListDirectories(String volumeName, File folderPath) {
        Log.i(TAG, "scan white list directoris begin " + volumeName);
        if (volumeName == null) {
            Log.e(TAG, "volumeName is null");
        } else if (!"internal".equals(volumeName)) {
            setState(false);
            this.mCachedWhiteListScanPaths.clear();
            for (File dir : resolveWhiteDirectories(volumeName, folderPath)) {
                MediaServiceProxy mediaServiceProxy = this.mMediaService;
                if (mediaServiceProxy != null) {
                    mediaServiceProxy.scanDirectoryEx(this.mContext, dir);
                } else {
                    Log.e(TAG, "MediaService proxy is null");
                }
            }
            setState(true);
            Log.i(TAG, "scan white list directoris end " + volumeName);
        }
    }

    private Collection<File> resolveWhiteDirectories(String volumeName, File folderPath) {
        String filelPath;
        StringBuilder sb;
        WhiteListUtils whiteListUtils = this;
        String str = volumeName;
        File file = folderPath;
        long start = System.currentTimeMillis();
        Set<File> res = whiteListUtils.mCachedWhiteListScanPaths;
        try {
            for (String exactVolume : MediaStoreEx.getExternalVolumeNames(whiteListUtils.mContext)) {
                File volumeScanPath = MediaStoreEx.getVolumePath(exactVolume);
                if (str.equals("external") || str.equals(exactVolume)) {
                    if (volumeScanPath == null) {
                        whiteListUtils = this;
                        str = volumeName;
                        file = folderPath;
                    } else if (file == null || volumeScanPath.equals(file)) {
                        File[] childFiles = volumeScanPath.listFiles(new FileFilter() {
                            /* class com.huawei.media.scan.WhiteListUtils.AnonymousClass1 */

                            public boolean accept(File file) {
                                if (file == null || !file.isDirectory() || file.isHidden()) {
                                    return false;
                                }
                                return true;
                            }
                        });
                        if (childFiles != null) {
                            for (File childFile : childFiles) {
                                if (System.currentTimeMillis() - start > 5000) {
                                    Log.w(TAG, "read white list over 5000s");
                                    return res;
                                }
                                if (childFile != null) {
                                    try {
                                        filelPath = childFile.getCanonicalPath();
                                    } catch (IOException e) {
                                        Log.e(TAG, "resolveWhiteDirectoriesb childFile.getCanonicalPath exception");
                                        filelPath = null;
                                    }
                                    String[] strArr = WHITE_FOLDER_NAMES;
                                    int i = 0;
                                    for (int length = strArr.length; i < length; length = length) {
                                        String folderString = strArr[i];
                                        if (folderString.equalsIgnoreCase(childFile.getName())) {
                                            if (folderString.equalsIgnoreCase(WECHAT_FOLDER_NAME)) {
                                                sb = new StringBuilder();
                                                sb.append(volumeScanPath);
                                                sb.append(WECHAT_FOLDER);
                                            } else {
                                                sb = new StringBuilder();
                                                sb.append(filelPath);
                                                sb.append(File.separator);
                                            }
                                            res.add(new File(sb.toString()));
                                        }
                                        i++;
                                    }
                                }
                            }
                            whiteListUtils = this;
                            str = volumeName;
                            file = folderPath;
                        }
                    }
                }
            }
        } catch (FileNotFoundException e2) {
            Log.e(TAG, "file not found");
        }
        return res;
    }

    private void setState(boolean state) {
        this.mState = state;
    }

    public int getWhiteListFlag(String path) {
        if (path == null || !this.mState) {
            return 0;
        }
        int result = 0;
        for (File file : this.mCachedWhiteListScanPaths) {
            String whiteScanPath = null;
            try {
                whiteScanPath = file.getCanonicalPath();
            } catch (IOException e) {
                Log.e(TAG, "getWhiteListFlag file.getCanonicalPath exception");
            }
            if (whiteScanPath != null) {
                if (whiteScanPath.length() > path.length()) {
                    if (whiteScanPath.substring(0, path.length()).equalsIgnoreCase(path)) {
                        result = 1;
                    }
                } else if (whiteScanPath.length() < path.length()) {
                    if (path.substring(0, whiteScanPath.length()).equalsIgnoreCase(whiteScanPath)) {
                        return 2;
                    }
                } else if (path.equalsIgnoreCase(whiteScanPath)) {
                    return 2;
                }
            }
        }
        return result;
    }

    public boolean isInWhiteScanList() {
        return !this.mState;
    }

    public boolean isWhiteListValid() {
        return this.mMediaService != null;
    }
}
