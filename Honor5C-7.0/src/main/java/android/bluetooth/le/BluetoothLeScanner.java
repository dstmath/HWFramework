package android.bluetooth.le;

import android.app.ActivityThread;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCallbackWrapper;
import android.bluetooth.IBluetoothGatt;
import android.bluetooth.IBluetoothManager;
import android.bluetooth.le.ScanSettings.Builder;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.os.WorkSource;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class BluetoothLeScanner {
    private static final boolean DBG = true;
    private static final String TAG = "BluetoothLeScanner";
    private static final boolean VDBG = false;
    private BluetoothAdapter mBluetoothAdapter;
    private final IBluetoothManager mBluetoothManager;
    private final Handler mHandler;
    private final Map<ScanCallback, BleScanCallbackWrapper> mLeScanClients;

    /* renamed from: android.bluetooth.le.BluetoothLeScanner.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ ScanCallback val$callback;
        final /* synthetic */ int val$errorCode;

        AnonymousClass1(ScanCallback val$callback, int val$errorCode) {
            this.val$callback = val$callback;
            this.val$errorCode = val$errorCode;
        }

        public void run() {
            this.val$callback.onScanFailed(this.val$errorCode);
        }
    }

    private class BleScanCallbackWrapper extends BluetoothGattCallbackWrapper {
        private static final int REGISTRATION_CALLBACK_TIMEOUT_MILLIS = 2000;
        private IBluetoothGatt mBluetoothGatt;
        private int mClientIf;
        private final List<ScanFilter> mFilters;
        private List<List<ResultStorageDescriptor>> mResultStorages;
        private final ScanCallback mScanCallback;
        private ScanSettings mSettings;
        private final WorkSource mWorkSource;

        /* renamed from: android.bluetooth.le.BluetoothLeScanner.BleScanCallbackWrapper.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ ScanResult val$scanResult;

            AnonymousClass1(ScanResult val$scanResult) {
                this.val$scanResult = val$scanResult;
            }

            public void run() {
                BleScanCallbackWrapper.this.mScanCallback.onScanResult(1, this.val$scanResult);
            }
        }

        /* renamed from: android.bluetooth.le.BluetoothLeScanner.BleScanCallbackWrapper.2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ List val$results;

            AnonymousClass2(List val$results) {
                this.val$results = val$results;
            }

            public void run() {
                BleScanCallbackWrapper.this.mScanCallback.onBatchScanResults(this.val$results);
            }
        }

        /* renamed from: android.bluetooth.le.BluetoothLeScanner.BleScanCallbackWrapper.3 */
        class AnonymousClass3 implements Runnable {
            final /* synthetic */ boolean val$onFound;
            final /* synthetic */ ScanResult val$scanResult;

            AnonymousClass3(boolean val$onFound, ScanResult val$scanResult) {
                this.val$onFound = val$onFound;
                this.val$scanResult = val$scanResult;
            }

            public void run() {
                if (this.val$onFound) {
                    BleScanCallbackWrapper.this.mScanCallback.onScanResult(2, this.val$scanResult);
                } else {
                    BleScanCallbackWrapper.this.mScanCallback.onScanResult(4, this.val$scanResult);
                }
            }
        }

        public BleScanCallbackWrapper(IBluetoothGatt bluetoothGatt, List<ScanFilter> filters, ScanSettings settings, WorkSource workSource, ScanCallback scanCallback, List<List<ResultStorageDescriptor>> resultStorages) {
            this.mBluetoothGatt = bluetoothGatt;
            this.mFilters = filters;
            this.mSettings = settings;
            this.mWorkSource = workSource;
            this.mScanCallback = scanCallback;
            this.mClientIf = 0;
            this.mResultStorages = resultStorages;
        }

        public void startRegisteration() {
            synchronized (this) {
                if (this.mClientIf == -1) {
                    return;
                }
                try {
                    this.mBluetoothGatt.registerClient(new ParcelUuid(UUID.randomUUID()), this);
                    wait(2000);
                } catch (Exception e) {
                    Log.e(BluetoothLeScanner.TAG, "application registeration exception", e);
                    BluetoothLeScanner.this.postCallbackError(this.mScanCallback, 3);
                }
                if (this.mClientIf > 0) {
                    BluetoothLeScanner.this.mLeScanClients.put(this.mScanCallback, this);
                } else {
                    if (this.mClientIf == 0) {
                        this.mClientIf = -1;
                    }
                    BluetoothLeScanner.this.postCallbackError(this.mScanCallback, 2);
                }
            }
        }

        public void stopLeScan() {
            synchronized (this) {
                if (this.mClientIf <= 0) {
                    Log.e(BluetoothLeScanner.TAG, "Error state, mLeHandle: " + this.mClientIf);
                    return;
                }
                try {
                    this.mBluetoothGatt.stopScan(this.mClientIf, false);
                    this.mBluetoothGatt.unregisterClient(this.mClientIf);
                } catch (RemoteException e) {
                    Log.e(BluetoothLeScanner.TAG, "Failed to stop scan and unregister", e);
                }
                this.mClientIf = -1;
            }
        }

        public void updateLeScanParams(int window, int interval) {
            Log.e(BluetoothLeScanner.TAG, "updateLeScanParams win:" + window + " ivl:" + interval);
            synchronized (this) {
                if (this.mClientIf <= 0) {
                    Log.e(BluetoothLeScanner.TAG, "Error state, mLeHandle: " + this.mClientIf);
                    return;
                }
                try {
                    this.mBluetoothGatt.updateScanParams(this.mClientIf, false, window, interval);
                } catch (RemoteException e) {
                    Log.e(BluetoothLeScanner.TAG, "Failed to stop scan and unregister", e);
                }
            }
        }

        void flushPendingBatchResults() {
            synchronized (this) {
                if (this.mClientIf <= 0) {
                    Log.e(BluetoothLeScanner.TAG, "Error state, mLeHandle: " + this.mClientIf);
                    return;
                }
                try {
                    this.mBluetoothGatt.flushPendingBatchResults(this.mClientIf, false);
                } catch (RemoteException e) {
                    Log.e(BluetoothLeScanner.TAG, "Failed to get pending scan results", e);
                }
            }
        }

        public void onClientRegistered(int status, int clientIf) {
            Log.d(BluetoothLeScanner.TAG, "onClientRegistered() - status=" + status + " clientIf=" + clientIf + " mClientIf=" + this.mClientIf);
            synchronized (this) {
                if (status == 0) {
                    try {
                        if (this.mClientIf == -1) {
                            this.mBluetoothGatt.unregisterClient(clientIf);
                        } else {
                            this.mClientIf = clientIf;
                            this.mBluetoothGatt.startScan(this.mClientIf, false, this.mSettings, this.mFilters, this.mWorkSource, this.mResultStorages, ActivityThread.currentOpPackageName());
                        }
                    } catch (RemoteException e) {
                        Log.e(BluetoothLeScanner.TAG, "fail to start le scan: " + e);
                        this.mClientIf = -1;
                    }
                } else {
                    this.mClientIf = -1;
                }
                notifyAll();
            }
        }

        public void onScanResult(ScanResult scanResult) {
            synchronized (this) {
                if (this.mClientIf <= 0) {
                    return;
                }
                new Handler(Looper.getMainLooper()).post(new AnonymousClass1(scanResult));
            }
        }

        public void onBatchScanResults(List<ScanResult> results) {
            new Handler(Looper.getMainLooper()).post(new AnonymousClass2(results));
        }

        public void onFoundOrLost(boolean onFound, ScanResult scanResult) {
            synchronized (this) {
                if (this.mClientIf <= 0) {
                    return;
                }
                new Handler(Looper.getMainLooper()).post(new AnonymousClass3(onFound, scanResult));
            }
        }

        public void onScanManagerErrorCallback(int errorCode) {
            synchronized (this) {
                if (this.mClientIf <= 0) {
                    return;
                }
                BluetoothLeScanner.this.postCallbackError(this.mScanCallback, errorCode);
            }
        }
    }

    public BluetoothLeScanner(IBluetoothManager bluetoothManager) {
        this.mBluetoothManager = bluetoothManager;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mLeScanClients = new HashMap();
    }

    public void startScan(ScanCallback callback) {
        startScan(null, new Builder().build(), callback);
    }

    public void startScan(List<ScanFilter> filters, ScanSettings settings, ScanCallback callback) {
        startScan(filters, settings, null, callback, null);
    }

    public void startScanFromSource(WorkSource workSource, ScanCallback callback) {
        startScanFromSource(null, new Builder().build(), workSource, callback);
    }

    public void startScanFromSource(List<ScanFilter> filters, ScanSettings settings, WorkSource workSource, ScanCallback callback) {
        startScan(filters, settings, workSource, callback, null);
    }

    private void startScan(List<ScanFilter> filters, ScanSettings settings, WorkSource workSource, ScanCallback callback, List<List<ResultStorageDescriptor>> resultStorages) {
        BluetoothLeUtils.checkAdapterStateOn(this.mBluetoothAdapter);
        if (callback == null) {
            throw new IllegalArgumentException("callback is null");
        } else if (settings == null) {
            throw new IllegalArgumentException("settings is null");
        } else {
            synchronized (this.mLeScanClients) {
                if (this.mLeScanClients.containsKey(callback)) {
                    postCallbackError(callback, 1);
                    return;
                }
                IBluetoothGatt bluetoothGatt;
                try {
                    bluetoothGatt = this.mBluetoothManager.getBluetoothGatt();
                } catch (RemoteException e) {
                    bluetoothGatt = null;
                }
                if (bluetoothGatt == null) {
                    postCallbackError(callback, 3);
                    return;
                } else if (!isSettingsConfigAllowedForScan(settings)) {
                    postCallbackError(callback, 4);
                    return;
                } else if (!isHardwareResourcesAvailableForScan(settings)) {
                    postCallbackError(callback, 5);
                    return;
                } else if (isSettingsAndFilterComboAllowed(settings, filters)) {
                    new BleScanCallbackWrapper(bluetoothGatt, filters, settings, workSource, callback, resultStorages).startRegisteration();
                    return;
                } else {
                    postCallbackError(callback, 4);
                    return;
                }
            }
        }
    }

    public void stopScan(ScanCallback callback) {
        BluetoothLeUtils.checkAdapterStateOn(this.mBluetoothAdapter);
        synchronized (this.mLeScanClients) {
            BleScanCallbackWrapper wrapper = (BleScanCallbackWrapper) this.mLeScanClients.remove(callback);
            if (wrapper == null) {
                Log.d(TAG, "could not find callback wrapper");
                return;
            }
            wrapper.stopLeScan();
        }
    }

    public void updateScanParams(ScanCallback callback, int window, int interval) {
        Log.i(TAG, "updateScanParams");
        BluetoothLeUtils.checkAdapterStateOn(this.mBluetoothAdapter);
        synchronized (this.mLeScanClients) {
            BleScanCallbackWrapper wrapper = (BleScanCallbackWrapper) this.mLeScanClients.get(callback);
            if (wrapper == null) {
                Log.d(TAG, "could not find callback wrapper");
                return;
            }
            wrapper.updateLeScanParams(window, interval);
        }
    }

    public void flushPendingScanResults(ScanCallback callback) {
        BluetoothLeUtils.checkAdapterStateOn(this.mBluetoothAdapter);
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null!");
        }
        synchronized (this.mLeScanClients) {
            BleScanCallbackWrapper wrapper = (BleScanCallbackWrapper) this.mLeScanClients.get(callback);
            if (wrapper == null) {
                return;
            }
            wrapper.flushPendingBatchResults();
        }
    }

    public void startTruncatedScan(List<TruncatedFilter> truncatedFilters, ScanSettings settings, ScanCallback callback) {
        int filterSize = truncatedFilters.size();
        List<ScanFilter> scanFilters = new ArrayList(filterSize);
        List<List<ResultStorageDescriptor>> scanStorages = new ArrayList(filterSize);
        for (TruncatedFilter filter : truncatedFilters) {
            scanFilters.add(filter.getFilter());
            scanStorages.add(filter.getStorageDescriptors());
        }
        startScan(scanFilters, settings, null, callback, scanStorages);
    }

    public void cleanup() {
        this.mLeScanClients.clear();
    }

    private void postCallbackError(ScanCallback callback, int errorCode) {
        this.mHandler.post(new AnonymousClass1(callback, errorCode));
    }

    private boolean isSettingsConfigAllowedForScan(ScanSettings settings) {
        if (this.mBluetoothAdapter.isOffloadedFilteringSupported()) {
            return DBG;
        }
        if (settings.getCallbackType() == 1 && settings.getReportDelayMillis() == 0) {
            return DBG;
        }
        return false;
    }

    private boolean isSettingsAndFilterComboAllowed(ScanSettings settings, List<ScanFilter> filterList) {
        if ((settings.getCallbackType() & 6) != 0) {
            if (filterList == null) {
                return false;
            }
            for (ScanFilter filter : filterList) {
                if (filter.isAllFieldsEmpty()) {
                    return false;
                }
            }
        }
        return DBG;
    }

    private boolean isHardwareResourcesAvailableForScan(ScanSettings settings) {
        boolean z = false;
        int callbackType = settings.getCallbackType();
        if ((callbackType & 2) == 0 && (callbackType & 4) == 0) {
            return DBG;
        }
        if (this.mBluetoothAdapter.isOffloadedFilteringSupported()) {
            z = this.mBluetoothAdapter.isHardwareTrackingFiltersAvailable();
        }
        return z;
    }
}
