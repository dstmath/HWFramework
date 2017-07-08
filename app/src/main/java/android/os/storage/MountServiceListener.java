package android.os.storage;

public abstract class MountServiceListener {
    void onUsbMassStorageConnectionChanged(boolean connected) {
    }

    void onStorageStateChange(String path, String oldState, String newState) {
    }
}
