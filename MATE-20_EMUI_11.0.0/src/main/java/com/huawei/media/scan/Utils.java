package com.huawei.media.scan;

import android.content.Context;
import android.media.BuildConfig;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.storage.StorageVolumeEx;
import com.huawei.android.provider.MediaStoreEx;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Utils {
    public static final int BD_MEDIA_HEIF = 916300002;
    public static final int BD_MEDIA_SCANNING = 916300001;
    public static final int FILES_BLACKLIST_ID_COLUMN_INDEX = 0;
    public static final int FILES_BLACKLIST_MEDIA_TYPE_COLUMN_INDEX = 2;
    public static final int FILES_BLACKLIST_PATH_COLUMN_INDEX = 1;
    public static final int FILES_PRESCAN_ID_COLUMN_INDEX = 0;
    public static final int FILES_PRESCAN_PATH_COLUMN_INDEX = 1;
    public static final String M_HEIF_SCAN = "Scan";
    public static final String M_SCANNING_COMPELETED_PERIOD = "CompeletedPeriod";
    private static final String TAG = "Utils";

    public static boolean contains(File[] dirs, File file) {
        for (File dir : dirs) {
            if (contains(dir, file)) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(File dir, File file) {
        if (dir == null || file == null) {
            return false;
        }
        String dirPath = null;
        String filePath = null;
        try {
            dirPath = dir.getCanonicalPath();
            filePath = file.getCanonicalPath();
        } catch (IOException e) {
            Log.e(TAG, "contains getCanonicalPath exception");
        }
        return contains(dirPath, filePath);
    }

    public static boolean contains(String dirPath, String filePath) {
        if (dirPath == null || filePath == null) {
            return false;
        }
        if (dirPath.equals(filePath)) {
            return true;
        }
        if (!dirPath.endsWith("/")) {
            dirPath = dirPath + "/";
        }
        return filePath.startsWith(dirPath);
    }

    public static String getExtSdcardVolumePath(Context context) {
        List<StorageVolume> storageVolumes;
        if (context == null || (storageVolumes = ((StorageManager) context.getSystemService("storage")).getStorageVolumes()) == null) {
            return null;
        }
        for (StorageVolume storageVolume : storageVolumes) {
            if (storageVolume.isRemovable() && !StorageVolumeEx.getPath(storageVolume).contains("usb")) {
                return StorageVolumeEx.getPath(storageVolume);
            }
        }
        return null;
    }

    private static String getPrimaryVolumePath(Context context) {
        List<StorageVolume> storageVolumes;
        if (context == null || (storageVolumes = ((StorageManager) context.getSystemService("storage")).getStorageVolumes()) == null) {
            return null;
        }
        for (StorageVolume volume : storageVolumes) {
            if (volume.isPrimary()) {
                return StorageVolumeEx.getPath(volume);
            }
        }
        return null;
    }

    public static String processLegacyPath(Context context, String path) {
        if (path == null || context == null) {
            return null;
        }
        try {
            String path2 = new File(path).getCanonicalPath();
            String externalStoragePath = getPrimaryVolumePath(context);
            if (externalStoragePath == null || path2 == null || !path2.startsWith("/sdcard")) {
                return path2;
            }
            return externalStoragePath + path2.substring("/sdcard".length());
        } catch (IOException e) {
            Log.e(TAG, "couldn't canonicalize in processLegacyPath");
            return null;
        }
    }

    private static String appendFileSeparator(String path) {
        if (path == null) {
            return null;
        }
        if (path.endsWith(File.separator)) {
            return path;
        }
        return path + File.separator;
    }

    public static boolean isValidPath(Context context, String path) {
        String path2;
        List<StorageVolume> storageVolumes;
        if (context == null || path == null || (path2 = appendFileSeparator(path)) == null) {
            return false;
        }
        StorageManager storageManager = null;
        if (context.getSystemService("storage") instanceof StorageManager) {
            storageManager = (StorageManager) context.getSystemService("storage");
        }
        if (storageManager == null || (storageVolumes = storageManager.getStorageVolumes()) == null) {
            return false;
        }
        for (StorageVolume storageVolume : storageVolumes) {
            if (path2.startsWith(appendFileSeparator(StorageVolumeEx.getPath(storageVolume)))) {
                return true;
            }
        }
        if (MediaStoreEx.IS_SUPPORT_CLONE_APP) {
            StorageVolume[] appClonedUserVolumes = MediaStoreEx.getAppClonedUserVolumes(context);
            int volumeCount = 0;
            if (appClonedUserVolumes != null) {
                volumeCount = appClonedUserVolumes.length;
            }
            for (int j = 0; j < volumeCount; j++) {
                if (path2.startsWith(appendFileSeparator(StorageVolumeEx.getPath(appClonedUserVolumes[j])))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getDisplayId() {
        String displayId = SystemPropertiesEx.get("ro.huawei.build.display.id", BuildConfig.FLAVOR);
        if (TextUtils.isEmpty(displayId)) {
            return SystemPropertiesEx.get("ro.build.display.id", "SYS");
        }
        return displayId;
    }
}
