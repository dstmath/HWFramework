package android.bluetooth.le;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.IBluetoothGatt;
import android.bluetooth.IBluetoothManager;
import android.bluetooth.le.IPeriodicAdvertisingCallback.Stub;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import java.util.IdentityHashMap;
import java.util.Map;

public final class PeriodicAdvertisingManager {
    private static final int SKIP_MAX = 499;
    private static final int SKIP_MIN = 0;
    private static final int SYNC_STARTING = -1;
    private static final String TAG = "PeriodicAdvertisingManager";
    private static final int TIMEOUT_MAX = 16384;
    private static final int TIMEOUT_MIN = 10;
    Map<PeriodicAdvertisingCallback, IPeriodicAdvertisingCallback> callbackWrappers = new IdentityHashMap();
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final IBluetoothManager mBluetoothManager;

    public PeriodicAdvertisingManager(IBluetoothManager bluetoothManager) {
        this.mBluetoothManager = bluetoothManager;
    }

    public void registerSync(ScanResult scanResult, int skip, int timeout, PeriodicAdvertisingCallback callback) {
        registerSync(scanResult, skip, timeout, callback, null);
    }

    public void registerSync(ScanResult scanResult, int skip, int timeout, PeriodicAdvertisingCallback callback, Handler handler) {
        if (callback == null) {
            throw new IllegalArgumentException("callback can't be null");
        } else if (scanResult == null) {
            throw new IllegalArgumentException("scanResult can't be null");
        } else if (scanResult.getAdvertisingSid() == 255) {
            throw new IllegalArgumentException("scanResult must contain a valid sid");
        } else if (skip < 0 || skip > SKIP_MAX) {
            throw new IllegalArgumentException("timeout must be between 10 and 16384");
        } else if (timeout < 10 || timeout > 16384) {
            throw new IllegalArgumentException("timeout must be between 10 and 16384");
        } else {
            try {
                IBluetoothGatt gatt = this.mBluetoothManager.getBluetoothGatt();
                if (handler == null) {
                    handler = new Handler(Looper.getMainLooper());
                }
                IPeriodicAdvertisingCallback wrapped = wrap(callback, handler);
                this.callbackWrappers.put(callback, wrapped);
                try {
                    gatt.registerSync(scanResult, skip, timeout, wrapped);
                } catch (RemoteException e) {
                    Log.e(TAG, "Failed to register sync - ", e);
                }
            } catch (RemoteException e2) {
                Log.e(TAG, "Failed to get Bluetooth gatt - ", e2);
                callback.onSyncEstablished(0, scanResult.getDevice(), scanResult.getAdvertisingSid(), skip, timeout, 2);
            }
        }
    }

    public void unregisterSync(PeriodicAdvertisingCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback can't be null");
        }
        try {
            IBluetoothGatt gatt = this.mBluetoothManager.getBluetoothGatt();
            IPeriodicAdvertisingCallback wrapper = (IPeriodicAdvertisingCallback) this.callbackWrappers.remove(callback);
            if (wrapper == null) {
                throw new IllegalArgumentException("callback was not properly registered");
            }
            try {
                gatt.unregisterSync(wrapper);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to cancel sync creation - ", e);
            }
        } catch (RemoteException e2) {
            Log.e(TAG, "Failed to get Bluetooth gatt - ", e2);
        }
    }

    private IPeriodicAdvertisingCallback wrap(final PeriodicAdvertisingCallback callback, final Handler handler) {
        return new Stub() {
            public void onSyncEstablished(int syncHandle, BluetoothDevice device, int advertisingSid, int skip, int timeout, int status) {
                Handler handler = handler;
                final PeriodicAdvertisingCallback periodicAdvertisingCallback = callback;
                final int i = syncHandle;
                final BluetoothDevice bluetoothDevice = device;
                final int i2 = advertisingSid;
                final int i3 = skip;
                final int i4 = timeout;
                final int i5 = status;
                handler.post(new Runnable() {
                    public void run() {
                        periodicAdvertisingCallback.onSyncEstablished(i, bluetoothDevice, i2, i3, i4, i5);
                        if (i5 != 0) {
                            PeriodicAdvertisingManager.this.callbackWrappers.remove(periodicAdvertisingCallback);
                        }
                    }
                });
            }

            public void onPeriodicAdvertisingReport(final PeriodicAdvertisingReport report) {
                Handler handler = handler;
                final PeriodicAdvertisingCallback periodicAdvertisingCallback = callback;
                handler.post(new Runnable() {
                    public void run() {
                        periodicAdvertisingCallback.onPeriodicAdvertisingReport(report);
                    }
                });
            }

            public void onSyncLost(final int syncHandle) {
                Handler handler = handler;
                final PeriodicAdvertisingCallback periodicAdvertisingCallback = callback;
                handler.post(new Runnable() {
                    public void run() {
                        periodicAdvertisingCallback.onSyncLost(syncHandle);
                        PeriodicAdvertisingManager.this.callbackWrappers.remove(periodicAdvertisingCallback);
                    }
                });
            }
        };
    }
}
