package android.bluetooth.le;

import android.bluetooth.IBluetoothGatt;
import android.bluetooth.IBluetoothManager;
import android.os.RemoteException;
import android.util.Log;

public final class AdvertisingSet {
    private static final String TAG = "AdvertisingSet";
    private int advertiserId;
    private final IBluetoothGatt gatt;

    AdvertisingSet(int advertiserId, IBluetoothManager bluetoothManager) {
        this.advertiserId = advertiserId;
        try {
            this.gatt = bluetoothManager.getBluetoothGatt();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to get Bluetooth gatt - ", e);
            throw new IllegalStateException("Failed to get Bluetooth");
        }
    }

    void setAdvertiserId(int advertiserId) {
        this.advertiserId = advertiserId;
    }

    public void enableAdvertising(boolean enable, int duration, int maxExtendedAdvertisingEvents) {
        try {
            this.gatt.enableAdvertisingSet(this.advertiserId, enable, duration, maxExtendedAdvertisingEvents);
        } catch (RemoteException e) {
            Log.e(TAG, "remote exception - ", e);
        }
    }

    public void setAdvertisingData(AdvertiseData advertiseData) {
        try {
            this.gatt.setAdvertisingData(this.advertiserId, advertiseData);
        } catch (RemoteException e) {
            Log.e(TAG, "remote exception - ", e);
        }
    }

    public void setScanResponseData(AdvertiseData scanResponse) {
        try {
            this.gatt.setScanResponseData(this.advertiserId, scanResponse);
        } catch (RemoteException e) {
            Log.e(TAG, "remote exception - ", e);
        }
    }

    public void setAdvertisingParameters(AdvertisingSetParameters parameters) {
        try {
            this.gatt.setAdvertisingParameters(this.advertiserId, parameters);
        } catch (RemoteException e) {
            Log.e(TAG, "remote exception - ", e);
        }
    }

    public void setPeriodicAdvertisingParameters(PeriodicAdvertisingParameters parameters) {
        try {
            this.gatt.setPeriodicAdvertisingParameters(this.advertiserId, parameters);
        } catch (RemoteException e) {
            Log.e(TAG, "remote exception - ", e);
        }
    }

    public void setPeriodicAdvertisingData(AdvertiseData periodicData) {
        try {
            this.gatt.setPeriodicAdvertisingData(this.advertiserId, periodicData);
        } catch (RemoteException e) {
            Log.e(TAG, "remote exception - ", e);
        }
    }

    public void setPeriodicAdvertisingEnabled(boolean enable) {
        try {
            this.gatt.setPeriodicAdvertisingEnable(this.advertiserId, enable);
        } catch (RemoteException e) {
            Log.e(TAG, "remote exception - ", e);
        }
    }

    public void getOwnAddress() {
        try {
            this.gatt.getOwnAddress(this.advertiserId);
        } catch (RemoteException e) {
            Log.e(TAG, "remote exception - ", e);
        }
    }

    public int getAdvertiserId() {
        return this.advertiserId;
    }
}
