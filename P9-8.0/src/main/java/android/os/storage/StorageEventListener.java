package android.os.storage;

public class StorageEventListener {
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
}
