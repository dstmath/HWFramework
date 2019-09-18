package android.bluetooth.le;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothUuid;
import android.bluetooth.IBluetoothGatt;
import android.bluetooth.IBluetoothManager;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.IAdvertisingSetCallback;
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
    /* access modifiers changed from: private */
    public final Map<Integer, AdvertisingSet> mAdvertisingSets = Collections.synchronizedMap(new HashMap());
    private BluetoothAdapter mBluetoothAdapter;
    /* access modifiers changed from: private */
    public final IBluetoothManager mBluetoothManager;
    /* access modifiers changed from: private */
    public final Map<AdvertisingSetCallback, IAdvertisingSetCallback> mCallbackWrappers = Collections.synchronizedMap(new HashMap());
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
        AdvertiseCallback advertiseCallback = callback;
        Log.i(TAG, "startAdvertising is called");
        synchronized (this.mLegacyAdvertisers) {
            try {
                BluetoothLeUtils.checkAdapterStateOn(this.mBluetoothAdapter);
                if (advertiseCallback != null) {
                    boolean isConnectable = settings.isConnectable();
                    AdvertiseData advertiseData2 = advertiseData;
                    try {
                        int i = 1;
                        if (totalBytes(advertiseData2, isConnectable) <= 31) {
                            AdvertiseData advertiseData3 = scanResponse;
                            if (totalBytes(advertiseData3, false) <= 31) {
                                if (this.mLegacyAdvertisers.containsKey(advertiseCallback)) {
                                    postStartFailure(advertiseCallback, 3);
                                    return;
                                }
                                AdvertisingSetParameters.Builder parameters = new AdvertisingSetParameters.Builder();
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
                                    if (timeoutMillis >= 10) {
                                        i = timeoutMillis / 10;
                                    }
                                    duration = i;
                                }
                                int duration2 = duration;
                                AdvertisingSetCallback wrapped = wrapOldCallback(advertiseCallback, settings);
                                this.mLegacyAdvertisers.put(advertiseCallback, wrapped);
                                int i2 = timeoutMillis;
                                startAdvertisingSet(parameters.build(), advertiseData2, advertiseData3, null, null, duration2, 0, wrapped);
                                return;
                            }
                        } else {
                            AdvertiseData advertiseData4 = scanResponse;
                        }
                        postStartFailure(advertiseCallback, 1);
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                } else {
                    AdvertiseData advertiseData5 = advertiseData;
                    AdvertiseData advertiseData6 = scanResponse;
                    throw new IllegalArgumentException("callback cannot be null");
                }
            } catch (Throwable th2) {
                th = th2;
                AdvertiseData advertiseData7 = advertiseData;
                AdvertiseData advertiseData8 = scanResponse;
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public AdvertisingSetCallback wrapOldCallback(final AdvertiseCallback callback, final AdvertiseSettings settings) {
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
            if (callback != null) {
                AdvertisingSetCallback wrapper = this.mLegacyAdvertisers.remove(callback);
                if (wrapper != null) {
                    stopAdvertisingSet(wrapper);
                    this.mLegacyAdvertisers.remove(callback);
                    return;
                }
                return;
            }
            throw new IllegalArgumentException("callback cannot be null");
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
        AdvertiseData advertiseData2;
        AdvertiseData advertiseData3 = advertiseData;
        AdvertiseData advertiseData4 = scanResponse;
        int i = duration;
        int i2 = maxExtendedAdvertisingEvents;
        AdvertisingSetCallback advertisingSetCallback = callback;
        Handler handler2 = handler;
        BluetoothLeUtils.checkAdapterStateOn(this.mBluetoothAdapter);
        if (advertisingSetCallback != null) {
            boolean isConnectable = parameters.isConnectable();
            if (!parameters.isLegacy()) {
                boolean supportCodedPhy = this.mBluetoothAdapter.isLeCodedPhySupported();
                boolean support2MPhy = this.mBluetoothAdapter.isLe2MPhySupported();
                int pphy = parameters.getPrimaryPhy();
                int sphy = parameters.getSecondaryPhy();
                if (pphy == 3 && !supportCodedPhy) {
                    throw new IllegalArgumentException("Unsupported primary PHY selected");
                } else if ((sphy != 3 || supportCodedPhy) && (sphy != 2 || support2MPhy)) {
                    int maxData = this.mBluetoothAdapter.getLeMaximumAdvertisingDataLength();
                    if (totalBytes(advertiseData3, isConnectable) > maxData) {
                        boolean z = isConnectable;
                        Handler handler3 = handler2;
                        throw new IllegalArgumentException("Advertising data too big");
                    } else if (totalBytes(advertiseData4, false) <= maxData) {
                        advertiseData2 = periodicData;
                        if (totalBytes(advertiseData2, false) <= maxData) {
                            boolean supportPeriodic = this.mBluetoothAdapter.isLePeriodicAdvertisingSupported();
                            if (periodicParameters != null && !supportPeriodic) {
                                boolean z2 = supportCodedPhy;
                                throw new IllegalArgumentException("Controller does not support LE Periodic Advertising");
                            }
                        } else {
                            boolean z3 = supportCodedPhy;
                            boolean z4 = isConnectable;
                            Handler handler4 = handler2;
                            throw new IllegalArgumentException("Periodic advertising data too big");
                        }
                    } else {
                        boolean z5 = isConnectable;
                        Handler handler5 = handler2;
                        throw new IllegalArgumentException("Scan response data too big");
                    }
                } else {
                    throw new IllegalArgumentException("Unsupported secondary PHY selected");
                }
            } else if (totalBytes(advertiseData3, isConnectable) > 31) {
                throw new IllegalArgumentException("Legacy advertising data too big");
            } else if (totalBytes(advertiseData4, false) <= 31) {
                advertiseData2 = periodicData;
            } else {
                throw new IllegalArgumentException("Legacy scan response data too big");
            }
            if (i2 < 0 || i2 > 255) {
                Handler handler6 = handler2;
                throw new IllegalArgumentException("maxExtendedAdvertisingEvents out of range: " + i2);
            } else if (i2 != 0 && !this.mBluetoothAdapter.isLePeriodicAdvertisingSupported()) {
                throw new IllegalArgumentException("Can't use maxExtendedAdvertisingEvents with controller that don't support LE Extended Advertising");
            } else if (i < 0 || i > 65535) {
                Handler handler7 = handler2;
                throw new IllegalArgumentException("duration out of range: " + i);
            } else {
                try {
                    IBluetoothGatt gatt = this.mBluetoothManager.getBluetoothGatt();
                    IAdvertisingSetCallback wrapped = wrap(advertisingSetCallback, handler2);
                    if (this.mCallbackWrappers.putIfAbsent(advertisingSetCallback, wrapped) == null) {
                        AdvertiseData advertiseData5 = advertiseData3;
                        boolean z6 = isConnectable;
                        Handler handler8 = handler2;
                        try {
                            gatt.startAdvertisingSet(parameters, advertiseData5, advertiseData4, periodicParameters, advertiseData2, i, i2, wrapped);
                        } catch (RemoteException e) {
                            RemoteException remoteException = e;
                            Log.e(TAG, "Failed to start advertising set - ", e);
                            postStartSetFailure(handler8, advertisingSetCallback, 4);
                        }
                    } else {
                        boolean z7 = isConnectable;
                        Handler handler9 = handler2;
                        throw new IllegalArgumentException("callback instance already associated with advertising");
                    }
                } catch (RemoteException e2) {
                    boolean z8 = isConnectable;
                    Log.e(TAG, "Failed to get Bluetooth gatt - ", e2);
                    postStartSetFailure(handler2, advertisingSetCallback, 4);
                }
            }
        } else {
            Handler handler10 = handler2;
            throw new IllegalArgumentException("callback cannot be null");
        }
    }

    public void stopAdvertisingSet(AdvertisingSetCallback callback) {
        if (callback != null) {
            IAdvertisingSetCallback wrapped = this.mCallbackWrappers.remove(callback);
            if (wrapped != null) {
                try {
                    this.mBluetoothManager.getBluetoothGatt().stopAdvertisingSet(wrapped);
                } catch (RemoteException e) {
                    Log.e(TAG, "Failed to stop advertising - ", e);
                }
                return;
            }
            return;
        }
        throw new IllegalArgumentException("callback cannot be null");
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
            size += 2 + BluetoothUuid.uuidToBytes(uuid2).length + byteLength(data.getServiceData().get(uuid2));
        }
        for (int i = 0; i < data.getManufacturerSpecificData().size(); i++) {
            size += 4 + byteLength(data.getManufacturerSpecificData().valueAt(i));
        }
        if (data.getIncludeTxPowerLevel() != 0) {
            size += 3;
        }
        if (data.getIncludeDeviceName() && this.mBluetoothAdapter.getName() != null) {
            size += 2 + this.mBluetoothAdapter.getName().length();
        }
        return size;
    }

    private int byteLength(byte[] array) {
        if (array == null) {
            return 0;
        }
        return array.length;
    }

    /* access modifiers changed from: package-private */
    public IAdvertisingSetCallback wrap(final AdvertisingSetCallback callback, final Handler handler) {
        return new IAdvertisingSetCallback.Stub() {
            public void onAdvertisingSetStarted(final int advertiserId, final int txPower, final int status) {
                handler.post(new Runnable() {
                    public void run() {
                        if (status != 0) {
                            callback.onAdvertisingSetStarted(null, 0, status);
                            BluetoothLeAdvertiser.this.mCallbackWrappers.remove(callback);
                            return;
                        }
                        AdvertisingSet advertisingSet = new AdvertisingSet(advertiserId, BluetoothLeAdvertiser.this.mBluetoothManager);
                        BluetoothLeAdvertiser.this.mAdvertisingSets.put(Integer.valueOf(advertiserId), advertisingSet);
                        callback.onAdvertisingSetStarted(advertisingSet, txPower, status);
                    }
                });
            }

            public void onOwnAddressRead(final int advertiserId, final int addressType, final String address) {
                handler.post(new Runnable() {
                    public void run() {
                        callback.onOwnAddressRead((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(advertiserId)), addressType, address);
                    }
                });
            }

            public void onAdvertisingSetStopped(final int advertiserId) {
                handler.post(new Runnable() {
                    public void run() {
                        callback.onAdvertisingSetStopped((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(advertiserId)));
                        BluetoothLeAdvertiser.this.mAdvertisingSets.remove(Integer.valueOf(advertiserId));
                        BluetoothLeAdvertiser.this.mCallbackWrappers.remove(callback);
                    }
                });
            }

            public void onAdvertisingEnabled(final int advertiserId, final boolean enabled, final int status) {
                handler.post(new Runnable() {
                    public void run() {
                        callback.onAdvertisingEnabled((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(advertiserId)), enabled, status);
                    }
                });
            }

            public void updateAdvertiseInterval(int interval) {
            }

            public void updateAdvertiseData(AdvertiseData data, boolean isScanResponse) {
            }

            public void onAdvertisingDataSet(final int advertiserId, final int status) {
                handler.post(new Runnable() {
                    public void run() {
                        callback.onAdvertisingDataSet((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(advertiserId)), status);
                    }
                });
            }

            public void onScanResponseDataSet(final int advertiserId, final int status) {
                handler.post(new Runnable() {
                    public void run() {
                        callback.onScanResponseDataSet((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(advertiserId)), status);
                    }
                });
            }

            public void onAdvertisingParametersUpdated(final int advertiserId, final int txPower, final int status) {
                handler.post(new Runnable() {
                    public void run() {
                        callback.onAdvertisingParametersUpdated((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(advertiserId)), txPower, status);
                    }
                });
            }

            public void onPeriodicAdvertisingParametersUpdated(final int advertiserId, final int status) {
                handler.post(new Runnable() {
                    public void run() {
                        callback.onPeriodicAdvertisingParametersUpdated((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(advertiserId)), status);
                    }
                });
            }

            public void onPeriodicAdvertisingDataSet(final int advertiserId, final int status) {
                handler.post(new Runnable() {
                    public void run() {
                        callback.onPeriodicAdvertisingDataSet((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(advertiserId)), status);
                    }
                });
            }

            public void onPeriodicAdvertisingEnabled(final int advertiserId, final boolean enable, final int status) {
                handler.post(new Runnable() {
                    public void run() {
                        callback.onPeriodicAdvertisingEnabled((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(advertiserId)), enable, status);
                    }
                });
            }
        };
    }

    private void postStartSetFailure(Handler handler, final AdvertisingSetCallback callback, final int error) {
        handler.post(new Runnable() {
            public void run() {
                callback.onAdvertisingSetStarted(null, 0, error);
            }
        });
    }

    /* access modifiers changed from: private */
    public void postStartFailure(final AdvertiseCallback callback, final int error) {
        this.mHandler.post(new Runnable() {
            public void run() {
                callback.onStartFailure(error);
            }
        });
    }

    /* access modifiers changed from: private */
    public void postStartSuccess(final AdvertiseCallback callback, final AdvertiseSettings settings) {
        this.mHandler.post(new Runnable() {
            public void run() {
                callback.onStartSuccess(settings);
            }
        });
    }
}
