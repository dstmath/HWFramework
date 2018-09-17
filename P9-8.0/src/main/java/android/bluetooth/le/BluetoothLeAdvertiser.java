package android.bluetooth.le;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothUuid;
import android.bluetooth.IBluetoothGatt;
import android.bluetooth.IBluetoothManager;
import android.bluetooth.le.AdvertisingSetParameters.Builder;
import android.bluetooth.le.IAdvertisingSetCallback.Stub;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.util.Log;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class BluetoothLeAdvertiser {
    private static final int FLAGS_FIELD_BYTES = 3;
    private static final int MANUFACTURER_SPECIFIC_DATA_LENGTH = 2;
    private static final int MAX_ADVERTISING_DATA_BYTES = 1650;
    private static final int MAX_LEGACY_ADVERTISING_DATA_BYTES = 31;
    private static final int OVERHEAD_BYTES_PER_FIELD = 2;
    private static final String TAG = "BluetoothLeAdvertiser";
    private final Map<Integer, AdvertisingSet> mAdvertisingSets = Collections.synchronizedMap(new HashMap());
    private BluetoothAdapter mBluetoothAdapter;
    private final IBluetoothManager mBluetoothManager;
    private final Map<AdvertisingSetCallback, IAdvertisingSetCallback> mCallbackWrappers = Collections.synchronizedMap(new HashMap());
    private final Handler mHandler;
    private final Map<AdvertiseCallback, AdvertisingSetCallback> mLegacyAdvertisers = new HashMap();

    public BluetoothLeAdvertiser(IBluetoothManager bluetoothManager) {
        this.mBluetoothManager = bluetoothManager;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mHandler = new Handler(Looper.getMainLooper());
    }

    public void startAdvertising(AdvertiseSettings settings, AdvertiseData advertiseData, AdvertiseCallback callback) {
        startAdvertising(settings, advertiseData, null, callback);
    }

    public void startAdvertising(AdvertiseSettings settings, AdvertiseData advertiseData, AdvertiseData scanResponse, AdvertiseCallback callback) {
        Log.i(TAG, "startAdvertising is called");
        synchronized (this.mLegacyAdvertisers) {
            BluetoothLeUtils.checkAdapterStateOn(this.mBluetoothAdapter);
            if (callback == null) {
                throw new IllegalArgumentException("callback cannot be null");
            }
            boolean isConnectable = settings.isConnectable();
            if (totalBytes(advertiseData, isConnectable) > 31 || totalBytes(scanResponse, false) > 31) {
                postStartFailure(callback, 1);
            } else if (this.mLegacyAdvertisers.containsKey(callback)) {
                postStartFailure(callback, 3);
            } else {
                Builder parameters = new Builder();
                parameters.setLegacyMode(true);
                parameters.setConnectable(isConnectable);
                parameters.setScannable(true);
                if (settings.getMode() == 0) {
                    parameters.setInterval(AdvertisingSetParameters.INTERVAL_HIGH);
                } else if (settings.getMode() == 1) {
                    parameters.setInterval(400);
                } else if (settings.getMode() == 2) {
                    parameters.setInterval(160);
                }
                if (settings.getTxPowerLevel() == 0) {
                    parameters.setTxPowerLevel(-21);
                } else if (settings.getTxPowerLevel() == 1) {
                    parameters.setTxPowerLevel(-15);
                } else if (settings.getTxPowerLevel() == 2) {
                    parameters.setTxPowerLevel(-7);
                } else if (settings.getTxPowerLevel() == 3) {
                    parameters.setTxPowerLevel(1);
                }
                int duration = 0;
                int timeoutMillis = settings.getTimeout();
                if (timeoutMillis > 0) {
                    duration = timeoutMillis < 10 ? 1 : timeoutMillis / 10;
                }
                AdvertisingSetCallback wrapped = wrapOldCallback(callback, settings);
                this.mLegacyAdvertisers.put(callback, wrapped);
                startAdvertisingSet(parameters.build(), advertiseData, scanResponse, null, null, duration, 0, wrapped);
            }
        }
    }

    AdvertisingSetCallback wrapOldCallback(final AdvertiseCallback callback, final AdvertiseSettings settings) {
        return new AdvertisingSetCallback() {
            public void onAdvertisingSetStarted(AdvertisingSet advertisingSet, int txPower, int status) {
                if (status != 0) {
                    BluetoothLeAdvertiser.this.postStartFailure(callback, status);
                } else {
                    BluetoothLeAdvertiser.this.postStartSuccess(callback, settings);
                }
            }

            public void onAdvertisingEnabled(AdvertisingSet advertisingSet, boolean enabled, int status) {
                if (enabled) {
                    Log.e(BluetoothLeAdvertiser.TAG, "Legacy advertiser should be only disabled on timeout, but was enabled!");
                } else {
                    BluetoothLeAdvertiser.this.stopAdvertising(callback);
                }
            }
        };
    }

    public void stopAdvertising(AdvertiseCallback callback) {
        Log.i(TAG, "stopAdvertising is called");
        synchronized (this.mLegacyAdvertisers) {
            if (callback == null) {
                throw new IllegalArgumentException("callback cannot be null");
            }
            AdvertisingSetCallback wrapper = (AdvertisingSetCallback) this.mLegacyAdvertisers.remove(callback);
            if (wrapper == null) {
                return;
            }
            stopAdvertisingSet(wrapper);
            this.mLegacyAdvertisers.remove(callback);
        }
    }

    public void startAdvertisingSet(AdvertisingSetParameters parameters, AdvertiseData advertiseData, AdvertiseData scanResponse, PeriodicAdvertisingParameters periodicParameters, AdvertiseData periodicData, AdvertisingSetCallback callback) {
        startAdvertisingSet(parameters, advertiseData, scanResponse, periodicParameters, periodicData, 0, 0, callback, new Handler(Looper.getMainLooper()));
    }

    public void startAdvertisingSet(AdvertisingSetParameters parameters, AdvertiseData advertiseData, AdvertiseData scanResponse, PeriodicAdvertisingParameters periodicParameters, AdvertiseData periodicData, AdvertisingSetCallback callback, Handler handler) {
        startAdvertisingSet(parameters, advertiseData, scanResponse, periodicParameters, periodicData, 0, 0, callback, handler);
    }

    public void startAdvertisingSet(AdvertisingSetParameters parameters, AdvertiseData advertiseData, AdvertiseData scanResponse, PeriodicAdvertisingParameters periodicParameters, AdvertiseData periodicData, int duration, int maxExtendedAdvertisingEvents, AdvertisingSetCallback callback) {
        startAdvertisingSet(parameters, advertiseData, scanResponse, periodicParameters, periodicData, duration, maxExtendedAdvertisingEvents, callback, new Handler(Looper.getMainLooper()));
    }

    public void startAdvertisingSet(AdvertisingSetParameters parameters, AdvertiseData advertiseData, AdvertiseData scanResponse, PeriodicAdvertisingParameters periodicParameters, AdvertiseData periodicData, int duration, int maxExtendedAdvertisingEvents, AdvertisingSetCallback callback, Handler handler) {
        BluetoothLeUtils.checkAdapterStateOn(this.mBluetoothAdapter);
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        }
        boolean isConnectable = parameters.isConnectable();
        if (!parameters.isLegacy()) {
            boolean supportCodedPhy = this.mBluetoothAdapter.isLeCodedPhySupported();
            boolean support2MPhy = this.mBluetoothAdapter.isLe2MPhySupported();
            int pphy = parameters.getPrimaryPhy();
            int sphy = parameters.getSecondaryPhy();
            if (pphy == 3 && (supportCodedPhy ^ 1) != 0) {
                throw new IllegalArgumentException("Unsupported primary PHY selected");
            } else if ((sphy != 3 || (supportCodedPhy ^ 1) == 0) && (sphy != 2 || (support2MPhy ^ 1) == 0)) {
                int maxData = this.mBluetoothAdapter.getLeMaximumAdvertisingDataLength();
                if (totalBytes(advertiseData, isConnectable) > maxData) {
                    throw new IllegalArgumentException("Advertising data too big");
                } else if (totalBytes(scanResponse, false) > maxData) {
                    throw new IllegalArgumentException("Scan response data too big");
                } else if (totalBytes(periodicData, false) > maxData) {
                    throw new IllegalArgumentException("Periodic advertising data too big");
                } else {
                    boolean supportPeriodic = this.mBluetoothAdapter.isLePeriodicAdvertisingSupported();
                    if (!(periodicParameters == null || (supportPeriodic ^ 1) == 0)) {
                        throw new IllegalArgumentException("Controller does not support LE Periodic Advertising");
                    }
                }
            } else {
                throw new IllegalArgumentException("Unsupported secondary PHY selected");
            }
        } else if (totalBytes(advertiseData, isConnectable) > 31) {
            throw new IllegalArgumentException("Legacy advertising data too big");
        } else if (totalBytes(scanResponse, false) > 31) {
            throw new IllegalArgumentException("Legacy scan response data too big");
        }
        if (maxExtendedAdvertisingEvents < 0 || maxExtendedAdvertisingEvents > 255) {
            throw new IllegalArgumentException("maxExtendedAdvertisingEvents out of range: " + maxExtendedAdvertisingEvents);
        } else if (maxExtendedAdvertisingEvents != 0 && (this.mBluetoothAdapter.isLePeriodicAdvertisingSupported() ^ 1) != 0) {
            throw new IllegalArgumentException("Can't use maxExtendedAdvertisingEvents with controller that don't support LE Extended Advertising");
        } else if (duration < 0 || duration > 65535) {
            throw new IllegalArgumentException("duration out of range: " + duration);
        } else {
            try {
                IBluetoothGatt gatt = this.mBluetoothManager.getBluetoothGatt();
                IAdvertisingSetCallback wrapped = wrap(callback, handler);
                if (this.mCallbackWrappers.putIfAbsent(callback, wrapped) != null) {
                    throw new IllegalArgumentException("callback instance already associated with advertising");
                }
                try {
                    gatt.startAdvertisingSet(parameters, advertiseData, scanResponse, periodicParameters, periodicData, duration, maxExtendedAdvertisingEvents, wrapped);
                } catch (RemoteException e) {
                    Log.e(TAG, "Failed to start advertising set - ", e);
                    throw new IllegalStateException("Failed to start advertising set");
                }
            } catch (RemoteException e2) {
                Log.e(TAG, "Failed to get Bluetooth gatt - ", e2);
                throw new IllegalStateException("Failed to get Bluetooth");
            }
        }
    }

    public void stopAdvertisingSet(AdvertisingSetCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        }
        IAdvertisingSetCallback wrapped = (IAdvertisingSetCallback) this.mCallbackWrappers.remove(callback);
        if (wrapped != null) {
            try {
                this.mBluetoothManager.getBluetoothGatt().stopAdvertisingSet(wrapped);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to stop advertising - ", e);
                throw new IllegalStateException("Failed to stop advertising");
            }
        }
    }

    public void updateAdvertiseInterval(AdvertiseCallback callback, int interval) {
        Log.d(TAG, "ruby in updateAdvertiseInterval");
    }

    public void updateAdvertiseData(AdvertiseCallback callback, AdvertiseData data, boolean isScanResponse) {
        Log.d(TAG, "ruby in updateAdvertiseData isScanResponse:" + isScanResponse);
    }

    public void cleanup() {
        this.mLegacyAdvertisers.clear();
        this.mCallbackWrappers.clear();
        this.mAdvertisingSets.clear();
    }

    private int totalBytes(AdvertiseData data, boolean isFlagsIncluded) {
        if (data == null) {
            return 0;
        }
        int size = isFlagsIncluded ? 3 : 0;
        if (data.getServiceUuids() != null) {
            int num16BitUuids = 0;
            int num32BitUuids = 0;
            int num128BitUuids = 0;
            for (ParcelUuid uuid : data.getServiceUuids()) {
                if (BluetoothUuid.is16BitUuid(uuid)) {
                    num16BitUuids++;
                } else if (BluetoothUuid.is32BitUuid(uuid)) {
                    num32BitUuids++;
                } else {
                    num128BitUuids++;
                }
            }
            if (num16BitUuids != 0) {
                size += (num16BitUuids * 2) + 2;
            }
            if (num32BitUuids != 0) {
                size += (num32BitUuids * 4) + 2;
            }
            if (num128BitUuids != 0) {
                size += (num128BitUuids * 16) + 2;
            }
        }
        for (ParcelUuid uuid2 : data.getServiceData().keySet()) {
            size += byteLength((byte[]) data.getServiceData().get(uuid2)) + (BluetoothUuid.uuidToBytes(uuid2).length + 2);
        }
        for (int i = 0; i < data.getManufacturerSpecificData().size(); i++) {
            size += byteLength((byte[]) data.getManufacturerSpecificData().valueAt(i)) + 4;
        }
        if (data.getIncludeTxPowerLevel()) {
            size += 3;
        }
        if (data.getIncludeDeviceName() && this.mBluetoothAdapter.getName() != null) {
            size += this.mBluetoothAdapter.getName().length() + 2;
        }
        return size;
    }

    private int byteLength(byte[] array) {
        return array == null ? 0 : array.length;
    }

    IAdvertisingSetCallback wrap(final AdvertisingSetCallback callback, final Handler handler) {
        return new Stub() {
            public void onAdvertisingSetStarted(int advertiserId, int txPower, int status) {
                Handler handler = handler;
                final AdvertisingSetCallback advertisingSetCallback = callback;
                final int i = status;
                final int i2 = advertiserId;
                final int i3 = txPower;
                handler.post(new Runnable() {
                    public void run() {
                        if (i != 0) {
                            advertisingSetCallback.onAdvertisingSetStarted(null, 0, i);
                            BluetoothLeAdvertiser.this.mCallbackWrappers.remove(advertisingSetCallback);
                            return;
                        }
                        AdvertisingSet advertisingSet = new AdvertisingSet(i2, BluetoothLeAdvertiser.this.mBluetoothManager);
                        BluetoothLeAdvertiser.this.mAdvertisingSets.put(Integer.valueOf(i2), advertisingSet);
                        advertisingSetCallback.onAdvertisingSetStarted(advertisingSet, i3, i);
                    }
                });
            }

            public void onOwnAddressRead(int advertiserId, int addressType, String address) {
                Handler handler = handler;
                final AdvertisingSetCallback advertisingSetCallback = callback;
                final int i = advertiserId;
                final int i2 = addressType;
                final String str = address;
                handler.post(new Runnable() {
                    public void run() {
                        advertisingSetCallback.onOwnAddressRead((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(i)), i2, str);
                    }
                });
            }

            public void onAdvertisingSetStopped(final int advertiserId) {
                Handler handler = handler;
                final AdvertisingSetCallback advertisingSetCallback = callback;
                handler.post(new Runnable() {
                    public void run() {
                        advertisingSetCallback.onAdvertisingSetStopped((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(advertiserId)));
                        BluetoothLeAdvertiser.this.mAdvertisingSets.remove(Integer.valueOf(advertiserId));
                        BluetoothLeAdvertiser.this.mCallbackWrappers.remove(advertisingSetCallback);
                    }
                });
            }

            public void onAdvertisingEnabled(int advertiserId, boolean enabled, int status) {
                Handler handler = handler;
                final AdvertisingSetCallback advertisingSetCallback = callback;
                final int i = advertiserId;
                final boolean z = enabled;
                final int i2 = status;
                handler.post(new Runnable() {
                    public void run() {
                        advertisingSetCallback.onAdvertisingEnabled((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(i)), z, i2);
                    }
                });
            }

            public void updateAdvertiseInterval(int interval) {
            }

            public void updateAdvertiseData(AdvertiseData data, boolean isScanResponse) {
            }

            public void onAdvertisingDataSet(final int advertiserId, final int status) {
                Handler handler = handler;
                final AdvertisingSetCallback advertisingSetCallback = callback;
                handler.post(new Runnable() {
                    public void run() {
                        advertisingSetCallback.onAdvertisingDataSet((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(advertiserId)), status);
                    }
                });
            }

            public void onScanResponseDataSet(final int advertiserId, final int status) {
                Handler handler = handler;
                final AdvertisingSetCallback advertisingSetCallback = callback;
                handler.post(new Runnable() {
                    public void run() {
                        advertisingSetCallback.onScanResponseDataSet((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(advertiserId)), status);
                    }
                });
            }

            public void onAdvertisingParametersUpdated(int advertiserId, int txPower, int status) {
                Handler handler = handler;
                final AdvertisingSetCallback advertisingSetCallback = callback;
                final int i = advertiserId;
                final int i2 = txPower;
                final int i3 = status;
                handler.post(new Runnable() {
                    public void run() {
                        advertisingSetCallback.onAdvertisingParametersUpdated((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(i)), i2, i3);
                    }
                });
            }

            public void onPeriodicAdvertisingParametersUpdated(final int advertiserId, final int status) {
                Handler handler = handler;
                final AdvertisingSetCallback advertisingSetCallback = callback;
                handler.post(new Runnable() {
                    public void run() {
                        advertisingSetCallback.onPeriodicAdvertisingParametersUpdated((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(advertiserId)), status);
                    }
                });
            }

            public void onPeriodicAdvertisingDataSet(final int advertiserId, final int status) {
                Handler handler = handler;
                final AdvertisingSetCallback advertisingSetCallback = callback;
                handler.post(new Runnable() {
                    public void run() {
                        advertisingSetCallback.onPeriodicAdvertisingDataSet((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(advertiserId)), status);
                    }
                });
            }

            public void onPeriodicAdvertisingEnabled(int advertiserId, boolean enable, int status) {
                Handler handler = handler;
                final AdvertisingSetCallback advertisingSetCallback = callback;
                final int i = advertiserId;
                final boolean z = enable;
                final int i2 = status;
                handler.post(new Runnable() {
                    public void run() {
                        advertisingSetCallback.onPeriodicAdvertisingEnabled((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(i)), z, i2);
                    }
                });
            }
        };
    }

    private void postStartFailure(final AdvertiseCallback callback, final int error) {
        this.mHandler.post(new Runnable() {
            public void run() {
                callback.onStartFailure(error);
            }
        });
    }

    private void postStartSuccess(final AdvertiseCallback callback, final AdvertiseSettings settings) {
        this.mHandler.post(new Runnable() {
            public void run() {
                callback.onStartSuccess(settings);
            }
        });
    }
}
