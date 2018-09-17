package com.android.server.devicepolicy;

import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.storage.DiskInfo;
import android.os.storage.IStorageManager;
import android.os.storage.IStorageManager.Stub;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.util.Log;
import com.huawei.utils.reflect.EasyInvokeUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class StorageUtils extends EasyInvokeUtils {
    public static final String SDCARD_ROMOUNTED_STATE = "true";
    public static final String SDCARD_RO_STATE = "vold.sdcard.mdm_set_ro";
    public static final String SDCARD_RWMOUNTED_STATE = "false";
    private static final String TAG = "StorageUtils";
    private static final String USB_STORAGE = "usb";

    public static String[] getSdcardDirFromSystem(Context context) {
        if (context == null) {
            Log.e(TAG, "context is null");
            return null;
        }
        StorageVolume[] sv = ((StorageManager) context.getSystemService("storage")).getVolumeList();
        List<String> list = new ArrayList();
        for (StorageVolume storageVolume : sv) {
            if (isVolumeExternalSDcard(context, storageVolume)) {
                list.add(storageVolume.getPath());
            }
        }
        if (list.size() == 0) {
            Log.w(TAG, "there is no sdcard on device.");
            return null;
        }
        String[] mStr = new String[list.size()];
        int n = list.size();
        for (int m = 0; m < n; m++) {
            mStr[m] = (String) list.get(m);
        }
        return mStr;
    }

    public static boolean hasExternalSdcard(Context context) {
        for (StorageVolume volume : ((StorageManager) context.getSystemService("storage")).getVolumeList()) {
            if (isVolumeExternalSDcard(context, volume)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isExternalSdcardMounted(Context context) {
        return isVolumeMounted(getExternalSdcardVolume(context));
    }

    public static boolean isExternalSdcardNotStable(Context context) {
        return isVolumeNotStable(getExternalSdcardVolume(context));
    }

    public static StorageVolume getExternalSdcardVolume(Context context) {
        for (StorageVolume volume : ((StorageManager) context.getSystemService("storage")).getVolumeList()) {
            if (isVolumeExternalSDcard(context, volume)) {
                return volume;
            }
        }
        return null;
    }

    /* JADX WARNING: Missing block: B:3:0x0005, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isVolumeExternalSDcard(Context context, StorageVolume storageVolume) {
        if (storageVolume == null || context == null || storageVolume.isPrimary() || !storageVolume.isRemovable()) {
            return false;
        }
        StorageManager sm = (StorageManager) context.getSystemService("storage");
        if (storageVolume.getUuid() == null) {
            return false;
        }
        VolumeInfo volumeInfo = sm.findVolumeByUuid(storageVolume.getUuid());
        if (volumeInfo == null) {
            return false;
        }
        DiskInfo diskInfo = volumeInfo.getDisk();
        if (diskInfo != null) {
            return diskInfo.isSd();
        }
        return false;
    }

    public static boolean isVolumeMounted(StorageVolume sv) {
        if (sv == null) {
            return false;
        }
        String state = sv.getState();
        if ("mounted".equals(state) || "mounted_ro".equals(state)) {
            return true;
        }
        return false;
    }

    public static boolean isVolumeNotStable(StorageVolume sv) {
        if (sv == null) {
            return false;
        }
        String state = sv.getState();
        if ("checking".equals(state) || "ejecting".equals(state)) {
            return true;
        }
        return false;
    }

    public static boolean isVolumeUsb(StorageVolume sv) {
        if (sv == null || sv.getPath() == null) {
            return false;
        }
        return sv.getPath().toLowerCase(Locale.US).contains(USB_STORAGE);
    }

    public static IStorageManager getMountService() {
        IBinder service = ServiceManager.getService("mount");
        if (service != null) {
            return Stub.asInterface(service);
        }
        Log.e(TAG, "Can't get mount service");
        return null;
    }

    public static boolean doMountorUnMountSdcardPath(String[] path, boolean isMount) {
        if (path == null || path.length == 0) {
            return false;
        }
        IStorageManager mountService = getMountService();
        if (mountService == null) {
            Log.e(TAG, "unMount cannot get IMountService service.");
            return false;
        }
        long current = System.currentTimeMillis();
        int i = 0;
        while (i < path.length) {
            try {
                if (isMount) {
                    mountService.mountVolume(path[i]);
                } else {
                    mountService.unmountVolume(path[i], true, false);
                }
                i++;
            } catch (Exception e) {
                Log.e(TAG, "mount or unmount exception is  " + e + " and ");
                return false;
            }
        }
        Log.w(TAG, "mount and unmount cost time is " + (System.currentTimeMillis() - current));
        return true;
    }

    public static boolean isSwitchPrimaryVolumeSupported() {
        return SystemProperties.getBoolean("ro.config.switchPrimaryVolume", false);
    }

    public static boolean isSDCardWritingDisabled() {
        return SDCARD_ROMOUNTED_STATE.equals(SystemProperties.get(SDCARD_RO_STATE, SDCARD_RWMOUNTED_STATE));
    }

    public static boolean doMount(Context context) {
        try {
            String[] paths = getSdcardDirFromSystem(context);
            if (paths == null) {
                Log.w(TAG, "there is no sdcard.");
                return true;
            } else if (doMountorUnMountSdcardPath(paths, true)) {
                return true;
            } else {
                Log.e(TAG, "can't Mount Volume!");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "mount exception is " + e);
            return false;
        }
    }

    public static boolean doUnMount(Context context) {
        try {
            String[] paths = getSdcardDirFromSystem(context);
            if (paths == null) {
                Log.w(TAG, "there is no sdcard.");
                return true;
            } else if (doMountorUnMountSdcardPath(paths, false)) {
                return true;
            } else {
                Log.e(TAG, "can't unMount Volume!");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "unmount exception is " + e);
            return false;
        }
    }
}
