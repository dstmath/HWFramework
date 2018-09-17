package com.huawei.android.os.storage;

import android.os.storage.DiskInfo;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.os.storage.VolumeRecord;
import java.io.File;

public class StorageManagerEx {

    private static class InnerStorageEventListener extends StorageEventListener {
        private StorageEventListenerEx outerListner;

        public InnerStorageEventListener(StorageEventListenerEx listner) {
            this.outerListner = listner;
        }

        public void onUsbMassStorageConnectionChanged(boolean connected) {
            this.outerListner.onUsbMassStorageConnectionChanged(connected);
        }

        public void onStorageStateChanged(String path, String oldState, String newState) {
            this.outerListner.onStorageStateChanged(path, oldState, newState);
        }

        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            this.outerListner.onVolumeStateChanged(vol, oldState, newState);
        }

        public void onVolumeRecordChanged(VolumeRecord rec) {
            this.outerListner.onVolumeRecordChanged(rec);
        }

        public void onVolumeForgotten(String fsUuid) {
            this.outerListner.onVolumeForgotten(fsUuid);
        }

        public void onDiskScanned(DiskInfo disk, int volumeCount) {
            this.outerListner.onDiskScanned(disk, volumeCount);
        }

        public void onDiskDestroyed(DiskInfo disk) {
            this.outerListner.onDiskDestroyed(disk);
        }
    }

    public static class StorageEventListenerEx {
        private StorageEventListener innerListener = new InnerStorageEventListener(this);

        public void onUsbMassStorageConnectionChanged(boolean connected) {
        }

        public void onStorageStateChanged(String path, String oldState, String newState) {
        }

        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
        }

        public void onVolumeRecordChanged(VolumeRecord rec) {
        }

        public void onVolumeForgotten(String fsUuid) {
        }

        public void onDiskScanned(DiskInfo disk, int volumeCount) {
        }

        public void onDiskDestroyed(DiskInfo disk) {
        }

        StorageEventListener getInnerListener() {
            return this.innerListener;
        }
    }

    public static final StorageVolume[] getVolumeList(StorageManager storageManager) {
        return storageManager.getVolumeList();
    }

    public static String getVolumeState(StorageManager storageManager, String mountPoint) {
        return storageManager.getVolumeState(mountPoint);
    }

    public static StorageVolume[] getVolumeList(int userId, int flags) {
        return StorageManager.getVolumeList(userId, flags);
    }

    public static final int getFlagRealState() {
        return 512;
    }

    public static long getStorageLowBytes(StorageManager storageManager, File path) {
        return storageManager.getStorageLowBytes(path);
    }

    public static VolumeInfoEx findVolumeByUuid(StorageManager storageManager, String fsUuid) {
        return new VolumeInfoEx(storageManager.findVolumeByUuid(fsUuid));
    }

    public static void registerStorageEventListener(StorageManager storageManager, StorageEventListenerEx listener) {
        storageManager.registerListener(listener.getInnerListener());
    }

    public static void unregisterStorageEventListener(StorageManager storageManager, StorageEventListenerEx listener) {
        storageManager.unregisterListener(listener.getInnerListener());
    }
}
