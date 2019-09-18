package android.bluetooth.le;

import android.bluetooth.IBluetoothGatt;
import android.bluetooth.IBluetoothManager;
import android.os.RemoteException;
import android.util.Log;

public final class AdvertisingSet {
    private static final String TAG = "AdvertisingSet";
    private int mAdvertiserId;
    private final IBluetoothGatt mGatt;

    AdvertisingSet(int advertiserId, IBluetoothManager bluetoothManager) {
        this.mAdvertiserId = advertiserId;
        try {
            this.mGatt = bluetoothManager.getBluetoothGatt();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to get Bluetooth gatt - ", e);
            throw new IllegalStateException("Failed to get Bluetooth");
        }
    }

    /* access modifiers changed from: package-private */
    public void setAdvertiserId(int advertiserId) {
        this.mAdvertiserId = advertiserId;
    }

    public void enableAdvertising(boolean enable, int duration, int maxExtendedAdvertisingEvents) {
        try {
            this.mGatt.enableAdvertisingSet(this.mAdvertiserId, enable, duration, maxExtendedAdvertisingEvents);
        } catch (RemoteException e) {
            Log.e(TAG, "remote exception - ", e);
        }
    }

    public void setAdvertisingData(AdvertiseData advertiseData) {
        try {
            this.mGatt.setAdvertisingData(this.mAdvertiserId, advertiseData);
        } catch (RemoteException e) {
            Log.e(TAG, "remote exception - ", e);
        }
    }

    public void setScanResponseData(AdvertiseData scanResponse) {
        try {
            this.mGatt.setScanResponseData(this.mAdvertiserId, scanResponse);
        } catch (RemoteException e) {
            Log.e(TAG, "remote exception - ", e);
        }
    }

    public void setAdvertisingParameters(AdvertisingSetParameters parameters) {
        try {
            this.mGatt.setAdvertisingParameters(this.mAdvertiserId, parameters);
        } catch (RemoteException e) {
            Log.e(TAG, "remote exception - ", e);
        }
    }

    public void setPeriodicAdvertisingParameters(PeriodicAdvertisingParameters parameters) {
        try {
            this.mGatt.setPeriodicAdvertisingParameters(this.mAdvertiserId, parameters);
        } catch (RemoteException e) {
            Log.e(TAG, "remote exception - ", e);
        }
    }

    public void setPeriodicAdvertisingData(AdvertiseData periodicData) {
        try {
            this.mGatt.setPeriodicAdvertisingData(this.mAdvertiserId, periodicData);
        } catch (RemoteException e) {
            Log.e(TAG, "remote exception - ", e);
        }
    }

    public void setPeriodicAdvertisingEnabled(boolean enable) {
        try {
            this.mGatt.setPeriodicAdvertisingEnable(this.mAdvertiserId, enable);
        } catch (RemoteException e) {
            Log.e(TAG, "remote exception - ", e);
        }
    }

    public void getOwnAddress() {
        try {
            this.mGatt.getOwnAddress(this.mAdvertiserId);
        } catch (RemoteException e) {
            Log.e(TAG, "remote exception - ", e);
        }
    }

    public int getAdvertiserId() {
        return this.mAdvertiserId;
    }
}
