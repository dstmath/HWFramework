package com.android.server.devicepolicy;

import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.storage.DiskInfo;
import android.os.storage.IStorageManager;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import com.android.server.devicepolicy.plugins.SettingsMDMPlugin;
import com.huawei.utils.reflect.EasyInvokeUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class StorageUtils extends EasyInvokeUtils {
    private static final String TAG = "StorageUtils";
    private static final String USB_STORAGE = "usb";

    public static String[] getSdcardDirFromSystem(Context context) {
        if (context == null) {
            HwLog.e(TAG, "context is null");
            return null;
        }
        StorageVolume[] sv = ((StorageManager) context.getSystemService("storage")).getVolumeList();
        List<String> list = new ArrayList<>();
        for (StorageVolume storageVolume : sv) {
            if (isVolumeExternalSDcard(context, storageVolume)) {
                list.add(storageVolume.getId());
            }
        }
        if (list.isEmpty()) {
            HwLog.w(TAG, "there is no sdcard on device.");
            return null;
        }
        int size = list.size();
        String[] sdStr = new String[size];
        for (int i = 0; i < size; i++) {
            sdStr[i] = list.get(i);
        }
        return sdStr;
    }

    public static boolean hasExternalSdcard(Context context) {
        for (StorageVolume volume : ((StorageManager) context.getSystemService("storage")).getVolumeList()) {
            if (isVolumeExternalSDcard(context, volume)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isExternalSdcardMountedReadWrite(Context context) {
        return isVolumeMountedReadWrite(getExternalSdcardVolume(context));
    }

    public static boolean isExternalSdcardMountedReadOnly(Context context) {
        return isVolumeMountedReadOnly(getExternalSdcardVolume(context));
    }

    public static boolean isExternalSdcardNotStable(Context context) {
        return isVolumeNotStable(getExternalSdcardVolume(context));
    }

    public static StorageVolume getExternalSdcardVolume(Context context) {
        StorageVolume[] storageVolumes = ((StorageManager) context.getSystemService("storage")).getVolumeList();
        for (StorageVolume volume : storageVolumes) {
            if (isVolumeExternalSDcard(context, volume)) {
                return volume;
            }
        }
        return null;
    }

    public static boolean isVolumeExternalSDcard(Context context, StorageVolume storageVolume) {
        VolumeInfo volumeInfo;
        DiskInfo diskInfo;
        if (storageVolume == null || context == null || storageVolume.isPrimary() || !storageVolume.isRemovable()) {
            return false;
        }
        StorageManager sm = (StorageManager) context.getSystemService("storage");
        if (storageVolume.getUuid() == null || (volumeInfo = sm.findVolumeByUuid(storageVolume.getUuid())) == null || (diskInfo = volumeInfo.getDisk()) == null) {
            return false;
        }
        return diskInfo.isSd();
    }

    public static boolean isVolumeMountedReadWrite(StorageVolume sv) {
        if (sv != null && "mounted".equals(sv.getState())) {
            return true;
        }
        return false;
    }

    public static boolean isVolumeMountedReadOnly(StorageVolume sv) {
        if (sv != null && "mounted_ro".equals(sv.getState())) {
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
        return sv.getPath().toLowerCase(Locale.ROOT).contains(USB_STORAGE);
    }

    public static IStorageManager getMountService() {
        IBinder service = ServiceManager.getService("mount");
        if (service != null) {
            return IStorageManager.Stub.asInterface(service);
        }
        HwLog.e(TAG, "Can't get mount service");
        return null;
    }

    public static boolean doMountorUnMountSdcardPath(String[] volIds, boolean isMount) {
        if (volIds == null || volIds.length == 0) {
            return false;
        }
        IStorageManager mountService = getMountService();
        if (mountService == null) {
            HwLog.e(TAG, "unMount cannot get IMountService service.");
            return false;
        }
        long current = System.currentTimeMillis();
        for (int i = 0; i < volIds.length; i++) {
            try {
                if (isMount) {
                    mountService.mount(volIds[i]);
                } else {
                    mountService.unmount(volIds[i]);
                }
            } catch (RuntimeException e) {
                HwLog.e(TAG, "mount or unmount occur RuntimeException");
                return false;
            } catch (Exception e2) {
                HwLog.e(TAG, "mount or unmount exception");
                return false;
            }
        }
        HwLog.w(TAG, "mount and unmount cost time is " + (System.currentTimeMillis() - current));
        return true;
    }

    public static boolean isSwitchPrimaryVolumeSupported() {
        return SystemProperties.getBoolean("ro.config.switchPrimaryVolume", false);
    }

    public static boolean doMount(Context context) {
        try {
            String[] paths = getSdcardDirFromSystem(context);
            if (paths == null) {
                HwLog.w(TAG, "there is no sdcard.");
                return true;
            } else if (doMountorUnMountSdcardPath(paths, true)) {
                return true;
            } else {
                HwLog.e(TAG, "can't Mount Volume!");
                return false;
            }
        } catch (RuntimeException e) {
            HwLog.e(TAG, "doMount occur RuntimeException");
            return false;
        } catch (Exception e2) {
            HwLog.e(TAG, "mount exception");
            return false;
        }
    }

    public static boolean doUnMount(Context context) {
        try {
            String[] paths = getSdcardDirFromSystem(context);
            if (paths == null) {
                HwLog.w(TAG, "there is no sdcard.");
                return true;
            } else if (doMountorUnMountSdcardPath(paths, false)) {
                return true;
            } else {
                HwLog.e(TAG, "can't unMount Volume!");
                return false;
            }
        } catch (RuntimeException e) {
            HwLog.e(TAG, "doUnMount occur RuntimeException");
            return false;
        } catch (Exception e2) {
            HwLog.e(TAG, "unmount exception");
            return false;
        }
    }

    public static String getDiskId(Context context) {
        VolumeInfo volumnInfo;
        if (context == null) {
            HwLog.d(TAG, "context is null");
            return SettingsMDMPlugin.EMPTY_STRING;
        }
        StorageManager sm = (StorageManager) context.getSystemService("storage");
        StorageVolume[] list = sm.getVolumeList();
        for (StorageVolume volume : list) {
            if (!(volume == null || volume.isPrimary() || !volume.isRemovable() || (volumnInfo = sm.findVolumeByUuid(volume.getUuid())) == null || volumnInfo.getDisk() == null || !volumnInfo.getDisk().isSd())) {
                return volumnInfo.getDiskId();
            }
        }
        return SettingsMDMPlugin.EMPTY_STRING;
    }
}
