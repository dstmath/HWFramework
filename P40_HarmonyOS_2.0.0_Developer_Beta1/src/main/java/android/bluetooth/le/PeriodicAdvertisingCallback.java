package android.bluetooth.le;

import android.bluetooth.BluetoothDevice;

public abstract class PeriodicAdvertisingCallback {
    public static final int SYNC_NO_RESOURCES = 2;
    public static final int SYNC_NO_RESPONSE = 1;
    public static final int SYNC_SUCCESS = 0;

    public void onSyncEstablished(int syncHandle, BluetoothDevice device, int advertisingSid, int skip, int timeout, int status) {
    }

    public void onPeriodicAdvertisingReport(PeriodicAdvertisingReport report) {
    }

    public void onSyncLost(int syncHandle) {
    }
}
