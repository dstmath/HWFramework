package com.huawei.media.scan;

import android.content.Context;
import android.media.BuildConfig;
import android.os.storage.StorageVolume;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.storage.StorageVolumeEx;
import com.huawei.android.provider.MediaStoreEx;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HwMediaStoreImpl extends DefaultHwMediaStore {
    private static final String CUSTOM_DIR = "/custom";
    private static final String DATA_PRELOADS_MEDIA_DIR = "/data/preloads/media";
    private static final String EXTERNAL_STORAGE_PREFIX = "/storage/emulated";
    private static final String FACTORY_VERSION_P_MEDIA_DIR = "/product/etc/factory/Pictures";
    private static final String FACTORY_VERSION_Q_MEDIA_DIR = "/hw_product/etc/factory/Pictures";
    private static final Object LOCK_MEDIA_STORE = new Object();
    private static final boolean LOGD = Log.isLoggable(TAG, 3);
    private static final String MULTI_PRELOADED_DIR = "Multi-Preloaded";
    private static final String ODM_INTERNAL_DIR = "/odm";
    private static final String RUN_MODE_FACTORY = "factory";
    private static final String RUN_MODE_PROPERTY = "ro.runmode";
    private static final String TAG = "HwMediaStoreImpl";
    private static final String VENDOR_INTERNAL_DIR = "/vendor";
    private static String sCustomMediaDirectory = null;
    private static HwMediaStoreImpl sInstance = null;
    private static String sPreloadMediaDirectory = null;
    private static ArrayList<String> sPreloadMediaDirs = new ArrayList<>();

    static {
        sPreloadMediaDirs.add("/media/Pre-loaded");
        sPreloadMediaDirs.add(DATA_PRELOADS_MEDIA_DIR);
        sPreloadMediaDirs.add("/media/custom");
        sPreloadMediaDirs.add("/etc/factory/Pictures");
        sPreloadMediaDirs.add("/media/Multi-Preloaded");
    }

    private HwMediaStoreImpl() {
    }

    public static HwMediaStoreImpl getDefault() {
        HwMediaStoreImpl hwMediaStoreImpl;
        synchronized (LOCK_MEDIA_STORE) {
            if (sInstance == null) {
                sInstance = new HwMediaStoreImpl();
            }
            hwMediaStoreImpl = sInstance;
        }
        return hwMediaStoreImpl;
    }

    public String getVolumeNameEx(File path) {
        if (path == null) {
            Log.e(TAG, "getVolumeNameEx path is null");
            return null;
        }
        String filePath = null;
        try {
            filePath = path.getCanonicalPath();
        } catch (IOException e) {
            Log.e(TAG, "getVolumeNameEx path.getCanonicalPath exception");
        }
        try {
            if (path.getCanonicalPath().startsWith(EXTERNAL_STORAGE_PREFIX)) {
                return "external_primary";
            }
            if (isInternalPreloadRes(filePath)) {
                return "external_primary";
            }
            return null;
        } catch (IOException e2) {
            Log.e(TAG, "getVolumeNameEx getCanonicalPath exception");
        }
    }

    private static boolean isInternalPreloadRes(String path) {
        if (path == null) {
            Log.e(TAG, "isInternalPreloadRes path is null");
            return false;
        }
        int size = sPreloadMediaDirs.size();
        for (int i = 0; i < size; i++) {
            String preloadDirItem = sPreloadMediaDirs.get(i);
            if (!TextUtils.isEmpty(preloadDirItem) && path.contains(preloadDirItem)) {
                return true;
            }
        }
        return false;
    }

    public void getHwVolumeScanPaths(@NonNull Context context, @NonNull List<File> list, @NonNull String volumeName) throws FileNotFoundException {
        if (context == null || list == null || volumeName == null) {
            Log.e(TAG, "getHwVolumeScanPaths parameter is null");
            return;
        }
        if ("internal".equals(volumeName)) {
            addHwScanPathsForInternal(list);
        } else if ("external_primary".equals(volumeName)) {
            addHwScanPathsForExternal(context, list);
        } else {
            Log.i(TAG, "getHwVolumeScanPaths volumeName = " + volumeName);
        }
        printVolumeScanPaths(list, volumeName);
    }

    private void addHwScanPathsForInternal(List<File> list) {
        Iterator<String> it = getCfgPolicyMediaDirs().iterator();
        while (it.hasNext()) {
            addCanonicalFile(list, new File(it.next()));
        }
    }

    private String getDataPreloadsMediaDir() {
        return DATA_PRELOADS_MEDIA_DIR;
    }

    public static String getPreloadMediaDirectory() {
        return sPreloadMediaDirectory;
    }

    public static String getCustomMediaDirectory() {
        return sCustomMediaDirectory;
    }

    private void addHwScanPathsForExternal(Context context, List<File> list) {
        if (HwMediaScannerImpl.isFirstTimeScan()) {
            try {
                Iterator it = MediaStoreEx.getVolumeScanPaths("internal").iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    File dir = (File) it.next();
                    File file = new File(dir, "Pre-loaded");
                    if (file.exists() && file.isDirectory()) {
                        sPreloadMediaDirectory = dir + "/Pre-loaded";
                        addCanonicalFile(list, file);
                        break;
                    }
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "addHwScanPathsForExternal FileNotFoundException");
            }
        }
        File file2 = new File(getDataPreloadsMediaDir());
        if (file2.exists() && file2.isDirectory()) {
            addCanonicalFile(list, file2);
        }
        if (isEcotaVersion() && HwMediaScannerImpl.isFirstTimeScanForEcota()) {
            addEcotaPathForExternal(list);
        }
        if (MediaStoreEx.IS_SUPPORT_CLONE_APP) {
            addClonedUserVolumeForExternal(context, list);
        }
        if (RUN_MODE_FACTORY.equalsIgnoreCase(SystemPropertiesEx.get(RUN_MODE_PROPERTY, BuildConfig.FLAVOR))) {
            addCanonicalFile(list, new File(getFactoryVersionPMediaDir()));
            addCanonicalFile(list, new File(getFactoryVersionQMediaDir()));
        }
        addMultiPreloadedPathForExternal(list);
    }

    private void addEcotaPathForExternal(List<File> list) {
        ArrayList<String> allDirectories = getCfgPolicyMediaDirs();
        for (int i = allDirectories.size() - 1; i >= 0; i--) {
            String dir = allDirectories.get(i);
            File customFile = new File(dir + CUSTOM_DIR);
            if (customFile.exists() && customFile.isDirectory()) {
                sCustomMediaDirectory = dir + CUSTOM_DIR;
                addCanonicalFile(list, new File(dir + CUSTOM_DIR));
                return;
            }
        }
    }

    private void addClonedUserVolumeForExternal(Context context, List<File> list) {
        StorageVolume[] storageVolumes = MediaStoreEx.getAppClonedUserVolumes(context);
        String sdcardPath = Utils.getExtSdcardVolumePath(context);
        int volumeCount = 0;
        if (storageVolumes != null) {
            volumeCount = storageVolumes.length;
        }
        for (int j = 0; j < volumeCount; j++) {
            String volumePath = StorageVolumeEx.getPath(storageVolumes[j]);
            Log.i(TAG, "storageVolumes path = " + volumePath);
            if (volumePath.equals(sdcardPath)) {
                Log.i(TAG, "skip sdcard when cloned user volumes");
            } else {
                addCanonicalFile(list, new File(volumePath));
            }
        }
    }

    private void addMultiPreloadedPathForExternal(List<File> list) {
        Iterator<String> it = getCfgPolicyMediaDirs().iterator();
        while (it.hasNext()) {
            File preloadedFile = new File(it.next(), MULTI_PRELOADED_DIR);
            if (preloadedFile.exists() && preloadedFile.isDirectory()) {
                addCanonicalFile(list, preloadedFile);
            }
        }
    }

    private String getFactoryVersionPMediaDir() {
        return FACTORY_VERSION_P_MEDIA_DIR;
    }

    private String getFactoryVersionQMediaDir() {
        return FACTORY_VERSION_Q_MEDIA_DIR;
    }

    public static boolean isEcotaVersion() {
        return !TextUtils.isEmpty(SystemPropertiesEx.get("ro.product.EcotaVersion", BuildConfig.FLAVOR));
    }

    private static void addCanonicalFile(List<File> list, File file) {
        try {
            list.add(file.getCanonicalFile());
        } catch (IOException e) {
            Log.w(TAG, "Failed to resolve for IOException");
            list.add(file);
        }
    }

    private void printVolumeScanPaths(List<File> list, String volumeName) {
        if (LOGD) {
            Log.d(TAG, "printVolumeScanPaths " + volumeName);
            for (File f : list) {
                String filePath = null;
                try {
                    filePath = f.getCanonicalPath();
                } catch (IOException e) {
                    Log.e(TAG, "printVolumeScanPaths f.getCanonicalPath exception");
                }
                Log.d(TAG, "path: " + filePath);
            }
        }
    }

    public static ArrayList<String> getCfgPolicyMediaDirs() {
        ArrayList<String> allDirectories = new ArrayList<>();
        String[] hwCfgMediaTypeDirs = HwCfgFilePolicy.getCfgPolicyDir(1);
        if (hwCfgMediaTypeDirs == null) {
            Log.e(TAG, "getCfgPolicyDir is null");
            return allDirectories;
        }
        for (int i = 0; i < hwCfgMediaTypeDirs.length; i++) {
            String absPath = BuildConfig.FLAVOR;
            try {
                absPath = new File(hwCfgMediaTypeDirs[i]).getCanonicalPath();
            } catch (IOException e) {
                Log.e(TAG, "IOException occur");
            }
            if (absPath != null && !absPath.startsWith(VENDOR_INTERNAL_DIR) && !absPath.startsWith(ODM_INTERNAL_DIR)) {
                allDirectories.add(absPath + "/media");
            }
            allDirectories.add("/data/hw_init" + absPath + "/media");
        }
        return allDirectories;
    }
}
