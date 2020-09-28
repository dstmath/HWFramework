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
        int duration;
        Log.i(TAG, "startAdvertising is called");
        synchronized (this.mLegacyAdvertisers) {
            try {
                BluetoothLeUtils.checkAdapterStateOn(this.mBluetoothAdapter);
                if (callback != null) {
                    boolean isConnectable = settings.isConnectable();
                    try {
                        int duration2 = 1;
                        if (totalBytes(advertiseData, isConnectable) <= 31) {
                            if (totalBytes(scanResponse, false) <= 31) {
                                if (this.mLegacyAdvertisers.containsKey(callback)) {
                                    postStartFailure(callback, 3);
                                    return;
                                }
                                AdvertisingSetParameters.Builder parameters = new AdvertisingSetParameters.Builder();
                                parameters.setLegacyMode(true);
                                parameters.setConnectable(isConnectable);
                                parameters.setScannable(true);
                                if (settings.getMode() == 0) {
                                    parameters.setInterval(1600);
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
                                } else if (settings.getTxPowerLevel() == 4) {
                                    parameters.setTxPowerLevel(8);
                                }
                                int timeoutMillis = settings.getTimeout();
                                if (timeoutMillis > 0) {
                                    if (timeoutMillis >= 10) {
                                        duration2 = timeoutMillis / 10;
                                    }
                                    duration = duration2;
                                } else {
                                    duration = 0;
                                }
                                AdvertisingSetCallback wrapped = wrapOldCallback(callback, settings);
                                this.mLegacyAdvertisers.put(callback, wrapped);
                                startAdvertisingSet(parameters.build(), advertiseData, scanResponse, null, null, duration, 0, wrapped);
                                return;
                            }
                        }
                        postStartFailure(callback, 1);
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                } else {
                    throw new IllegalArgumentException("callback cannot be null");
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public AdvertisingSetCallback wrapOldCallback(final AdvertiseCallback callback, final AdvertiseSettings settings) {
        return new AdvertisingSetCallback() {
            /* class android.bluetooth.le.BluetoothLeAdvertiser.AnonymousClass1 */

            @Override // android.bluetooth.le.AdvertisingSetCallback
            public void onAdvertisingSetStarted(AdvertisingSet advertisingSet, int txPower, int status) {
                if (status != 0) {
                    BluetoothLeAdvertiser.this.postStartFailure(callback, status);
                } else {
                    BluetoothLeAdvertiser.this.postStartSuccess(callback, settings);
                }
            }

            @Override // android.bluetooth.le.AdvertisingSetCallback
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
                try {
                    AdvertisingSetCallback wrapper = this.mLegacyAdvertisers.get(callback);
                    if (wrapper != null) {
                        stopAdvertisingSet(wrapper);
                        this.mLegacyAdvertisers.remove(callback);
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                throw new IllegalArgumentException("callback cannot be null");
            }
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
        if (callback != null) {
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
                    if (totalBytes(advertiseData, isConnectable) > maxData) {
                        throw new IllegalArgumentException("Advertising data too big");
                    } else if (totalBytes(scanResponse, false) > maxData) {
                        throw new IllegalArgumentException("Scan response data too big");
                    } else if (totalBytes(periodicData, false) <= maxData) {
                        boolean supportPeriodic = this.mBluetoothAdapter.isLePeriodicAdvertisingSupported();
                        if (periodicParameters != null) {
                            if (!supportPeriodic) {
                                throw new IllegalArgumentException("Controller does not support LE Periodic Advertising");
                            }
                        }
                    } else {
                        throw new IllegalArgumentException("Periodic advertising data too big");
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
            } else if (maxExtendedAdvertisingEvents != 0 && !this.mBluetoothAdapter.isLePeriodicAdvertisingSupported()) {
                throw new IllegalArgumentException("Can't use maxExtendedAdvertisingEvents with controller that don't support LE Extended Advertising");
            } else if (duration < 0 || duration > 65535) {
                throw new IllegalArgumentException("duration out of range: " + duration);
            } else {
                try {
                    IBluetoothGatt gatt = this.mBluetoothManager.getBluetoothGatt();
                    if (gatt == null) {
                        Log.e(TAG, "Bluetooth GATT is null");
                        postStartSetFailure(handler, callback, 4);
                        return;
                    }
                    IAdvertisingSetCallback wrapped = wrap(callback, handler);
                    if (this.mCallbackWrappers.putIfAbsent(callback, wrapped) == null) {
                        try {
                            gatt.startAdvertisingSet(parameters, advertiseData, scanResponse, periodicParameters, periodicData, duration, maxExtendedAdvertisingEvents, wrapped);
                        } catch (RemoteException e) {
                            Log.e(TAG, "Failed to start advertising set - ", e);
                            postStartSetFailure(handler, callback, 4);
                        }
                    } else {
                        throw new IllegalArgumentException("callback instance already associated with advertising");
                    }
                } catch (RemoteException e2) {
                    Log.e(TAG, "Failed to get Bluetooth GATT - ", e2);
                    postStartSetFailure(handler, callback, 4);
                }
            }
        } else {
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
            }
        } else {
            throw new IllegalArgumentException("callback cannot be null");
        }
    }

    public void cleanup() {
        synchronized (this.mLegacyAdvertisers) {
            this.mLegacyAdvertisers.clear();
        }
        this.mCallbackWrappers.clear();
        this.mAdvertisingSets.clear();
    }

    private int totalBytes(AdvertiseData data, boolean isFlagsIncluded) {
        int size = 0;
        if (data == null) {
            return 0;
        }
        if (isFlagsIncluded) {
            size = 3;
        }
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
            size += BluetoothUuid.uuidToBytes(uuid2).length + 2 + byteLength(data.getServiceData().get(uuid2));
        }
        for (int i = 0; i < data.getManufacturerSpecificData().size(); i++) {
            size += byteLength(data.getManufacturerSpecificData().valueAt(i)) + 4;
        }
        if (data.getIncludeTxPowerLevel()) {
            size += 3;
        }
        if (!data.getIncludeDeviceName() || this.mBluetoothAdapter.getName() == null) {
            return size;
        }
        return size + this.mBluetoothAdapter.getName().length() + 2;
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
            /* class android.bluetooth.le.BluetoothLeAdvertiser.AnonymousClass2 */

            @Override // android.bluetooth.le.IAdvertisingSetCallback
            public void onAdvertisingSetStarted(final int advertiserId, final int txPower, final int status) {
                handler.post(new Runnable() {
                    /* class android.bluetooth.le.BluetoothLeAdvertiser.AnonymousClass2.AnonymousClass1 */

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

            @Override // android.bluetooth.le.IAdvertisingSetCallback
            public void onOwnAddressRead(final int advertiserId, final int addressType, final String address) {
                handler.post(new Runnable() {
                    /* class android.bluetooth.le.BluetoothLeAdvertiser.AnonymousClass2.AnonymousClass2 */

                    public void run() {
                        callback.onOwnAddressRead((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(advertiserId)), addressType, address);
                    }
                });
            }

            @Override // android.bluetooth.le.IAdvertisingSetCallback
            public void onAdvertisingSetStopped(final int advertiserId) {
                handler.post(new Runnable() {
                    /* class android.bluetooth.le.BluetoothLeAdvertiser.AnonymousClass2.AnonymousClass3 */

                    public void run() {
                        callback.onAdvertisingSetStopped((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(advertiserId)));
                        BluetoothLeAdvertiser.this.mAdvertisingSets.remove(Integer.valueOf(advertiserId));
                        BluetoothLeAdvertiser.this.mCallbackWrappers.remove(callback);
                    }
                });
            }

            @Override // android.bluetooth.le.IAdvertisingSetCallback
            public void onAdvertisingEnabled(final int advertiserId, final boolean enabled, final int status) {
                handler.post(new Runnable() {
                    /* class android.bluetooth.le.BluetoothLeAdvertiser.AnonymousClass2.AnonymousClass4 */

                    public void run() {
                        callback.onAdvertisingEnabled((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(advertiserId)), enabled, status);
                    }
                });
            }

            @Override // android.bluetooth.le.IAdvertisingSetCallback
            public void onAdvertisingDataSet(final int advertiserId, final int status) {
                handler.post(new Runnable() {
                    /* class android.bluetooth.le.BluetoothLeAdvertiser.AnonymousClass2.AnonymousClass5 */

                    public void run() {
                        callback.onAdvertisingDataSet((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(advertiserId)), status);
                    }
                });
            }

            @Override // android.bluetooth.le.IAdvertisingSetCallback
            public void onScanResponseDataSet(final int advertiserId, final int status) {
                handler.post(new Runnable() {
                    /* class android.bluetooth.le.BluetoothLeAdvertiser.AnonymousClass2.AnonymousClass6 */

                    public void run() {
                        callback.onScanResponseDataSet((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(advertiserId)), status);
                    }
                });
            }

            @Override // android.bluetooth.le.IAdvertisingSetCallback
            public void onAdvertisingParametersUpdated(final int advertiserId, final int txPower, final int status) {
                handler.post(new Runnable() {
                    /* class android.bluetooth.le.BluetoothLeAdvertiser.AnonymousClass2.AnonymousClass7 */

                    public void run() {
                        callback.onAdvertisingParametersUpdated((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(advertiserId)), txPower, status);
                    }
                });
            }

            @Override // android.bluetooth.le.IAdvertisingSetCallback
            public void onPeriodicAdvertisingParametersUpdated(final int advertiserId, final int status) {
                handler.post(new Runnable() {
                    /* class android.bluetooth.le.BluetoothLeAdvertiser.AnonymousClass2.AnonymousClass8 */

                    public void run() {
                        callback.onPeriodicAdvertisingParametersUpdated((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(advertiserId)), status);
                    }
                });
            }

            @Override // android.bluetooth.le.IAdvertisingSetCallback
            public void onPeriodicAdvertisingDataSet(final int advertiserId, final int status) {
                handler.post(new Runnable() {
                    /* class android.bluetooth.le.BluetoothLeAdvertiser.AnonymousClass2.AnonymousClass9 */

                    public void run() {
                        callback.onPeriodicAdvertisingDataSet((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(advertiserId)), status);
                    }
                });
            }

            @Override // android.bluetooth.le.IAdvertisingSetCallback
            public void onPeriodicAdvertisingEnabled(final int advertiserId, final boolean enable, final int status) {
                handler.post(new Runnable() {
                    /* class android.bluetooth.le.BluetoothLeAdvertiser.AnonymousClass2.AnonymousClass10 */

                    public void run() {
                        callback.onPeriodicAdvertisingEnabled((AdvertisingSet) BluetoothLeAdvertiser.this.mAdvertisingSets.get(Integer.valueOf(advertiserId)), enable, status);
                    }
                });
            }
        };
    }

    private void postStartSetFailure(Handler handler, final AdvertisingSetCallback callback, final int error) {
        handler.post(new Runnable() {
            /* class android.bluetooth.le.BluetoothLeAdvertiser.AnonymousClass3 */

            public void run() {
                callback.onAdvertisingSetStarted(null, 0, error);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void postStartFailure(final AdvertiseCallback callback, final int error) {
        this.mHandler.post(new Runnable() {
            /* class android.bluetooth.le.BluetoothLeAdvertiser.AnonymousClass4 */

            public void run() {
                callback.onStartFailure(error);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void postStartSuccess(final AdvertiseCallback callback, final AdvertiseSettings settings) {
        this.mHandler.post(new Runnable() {
            /* class android.bluetooth.le.BluetoothLeAdvertiser.AnonymousClass5 */

            public void run() {
                callback.onStartSuccess(settings);
            }
        });
    }
}
