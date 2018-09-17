package android.bluetooth.le;

import android.app.ActivityThread;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.IBluetoothGatt;
import android.bluetooth.IBluetoothManager;
import android.bluetooth.le.IScannerCallback.Stub;
import android.bluetooth.le.ScanSettings.Builder;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.WorkSource;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BluetoothLeScanner {
    private static final boolean DBG = true;
    public static final String EXTRA_CALLBACK_TYPE = "android.bluetooth.le.extra.CALLBACK_TYPE";
    public static final String EXTRA_ERROR_CODE = "android.bluetooth.le.extra.ERROR_CODE";
    public static final String EXTRA_LIST_SCAN_RESULT = "android.bluetooth.le.extra.LIST_SCAN_RESULT";
    private static final String TAG = "BluetoothLeScanner";
    private static final boolean VDBG = false;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final IBluetoothManager mBluetoothManager;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Map<ScanCallback, BleScanCallbackWrapper> mLeScanClients = new HashMap();

    private class BleScanCallbackWrapper extends Stub {
        private static final int REGISTRATION_CALLBACK_TIMEOUT_MILLIS = 2000;
        private IBluetoothGatt mBluetoothGatt;
        private final List<ScanFilter> mFilters;
        private List<List<ResultStorageDescriptor>> mResultStorages;
        private final ScanCallback mScanCallback;
        private int mScannerId = 0;
        private ScanSettings mSettings;
        private final WorkSource mWorkSource;

        public BleScanCallbackWrapper(IBluetoothGatt bluetoothGatt, List<ScanFilter> filters, ScanSettings settings, WorkSource workSource, ScanCallback scanCallback, List<List<ResultStorageDescriptor>> resultStorages) {
            this.mBluetoothGatt = bluetoothGatt;
            this.mFilters = filters;
            this.mSettings = settings;
            this.mWorkSource = workSource;
            this.mScanCallback = scanCallback;
            this.mResultStorages = resultStorages;
        }

        /* JADX WARNING: Removed duplicated region for block: B:15:0x0025 A:{ExcHandler: java.lang.InterruptedException (r0_0 'e' java.lang.Exception), Splitter: B:7:0x0008} */
        /* JADX WARNING: Missing block: B:14:0x0024, code:
            return;
     */
        /* JADX WARNING: Missing block: B:15:0x0025, code:
            r0 = move-exception;
     */
        /* JADX WARNING: Missing block: B:17:?, code:
            android.util.Log.e(android.bluetooth.le.BluetoothLeScanner.TAG, "application registeration exception", r0);
            android.bluetooth.le.BluetoothLeScanner.-wrap0(r4.this$0, r4.mScanCallback, 3);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void startRegistration() {
            synchronized (this) {
                if (this.mScannerId == -1) {
                    return;
                }
                try {
                    this.mBluetoothGatt.registerScanner(this, this.mWorkSource);
                    wait(2000);
                } catch (Exception e) {
                }
                if (this.mScannerId > 0) {
                    BluetoothLeScanner.this.mLeScanClients.put(this.mScanCallback, this);
                } else {
                    if (this.mScannerId == 0) {
                        this.mScannerId = -1;
                    }
                    BluetoothLeScanner.this.postCallbackError(this.mScanCallback, 2);
                }
            }
        }

        public void stopLeScan() {
            synchronized (this) {
                if (this.mScannerId <= 0) {
                    Log.e(BluetoothLeScanner.TAG, "Error state, mLeHandle: " + this.mScannerId);
                    return;
                }
                try {
                    this.mBluetoothGatt.stopScan(this.mScannerId);
                    this.mBluetoothGatt.unregisterScanner(this.mScannerId);
                } catch (RemoteException e) {
                    Log.e(BluetoothLeScanner.TAG, "Failed to stop scan and unregister", e);
                }
                this.mScannerId = -1;
                return;
            }
        }

        public void updateLeScanParams(int window, int interval) {
            Log.e(BluetoothLeScanner.TAG, "updateLeScanParams win:" + window + " ivl:" + interval);
            synchronized (this) {
                if (this.mScannerId <= 0) {
                    Log.e(BluetoothLeScanner.TAG, "Error state, mLeHandle: " + this.mScannerId);
                    return;
                }
                try {
                    this.mBluetoothGatt.updateScanParams(this.mScannerId, false, window, interval);
                } catch (RemoteException e) {
                    Log.e(BluetoothLeScanner.TAG, "Failed to stop scan and unregister", e);
                }
            }
            return;
        }

        void flushPendingBatchResults() {
            synchronized (this) {
                if (this.mScannerId <= 0) {
                    Log.e(BluetoothLeScanner.TAG, "Error state, mLeHandle: " + this.mScannerId);
                    return;
                }
                try {
                    this.mBluetoothGatt.flushPendingBatchResults(this.mScannerId);
                } catch (RemoteException e) {
                    Log.e(BluetoothLeScanner.TAG, "Failed to get pending scan results", e);
                }
            }
        }

        public void onScannerRegistered(int status, int scannerId) {
            Log.d(BluetoothLeScanner.TAG, "onScannerRegistered() - status=" + status + " scannerId=" + scannerId + " mScannerId=" + this.mScannerId);
            synchronized (this) {
                if (status == 0) {
                    try {
                        if (this.mScannerId == -1) {
                            this.mBluetoothGatt.unregisterClient(scannerId);
                        } else {
                            this.mScannerId = scannerId;
                            this.mBluetoothGatt.startScan(this.mScannerId, this.mSettings, this.mFilters, this.mResultStorages, ActivityThread.currentOpPackageName());
                        }
                    } catch (RemoteException e) {
                        Log.e(BluetoothLeScanner.TAG, "fail to start le scan: " + e);
                        this.mScannerId = -1;
                    }
                } else {
                    this.mScannerId = -1;
                }
                notifyAll();
            }
            return;
        }

        public void onScanResult(final ScanResult scanResult) {
            synchronized (this) {
                if (this.mScannerId <= 0) {
                    return;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        BleScanCallbackWrapper.this.mScanCallback.onScanResult(1, scanResult);
                    }
                });
            }
        }

        public void onBatchScanResults(final List<ScanResult> results) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    BleScanCallbackWrapper.this.mScanCallback.onBatchScanResults(results);
                }
            });
        }

        public void onFoundOrLost(final boolean onFound, final ScanResult scanResult) {
            synchronized (this) {
                if (this.mScannerId <= 0) {
                    return;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        if (onFound) {
                            BleScanCallbackWrapper.this.mScanCallback.onScanResult(2, scanResult);
                        } else {
                            BleScanCallbackWrapper.this.mScanCallback.onScanResult(4, scanResult);
                        }
                    }
                });
            }
        }

        public void onScanManagerErrorCallback(int errorCode) {
            synchronized (this) {
                if (this.mScannerId <= 0) {
                    return;
                }
                BluetoothLeScanner.this.postCallbackError(this.mScanCallback, errorCode);
            }
        }
    }

    public BluetoothLeScanner(IBluetoothManager bluetoothManager) {
        this.mBluetoothManager = bluetoothManager;
    }

    public void startScan(ScanCallback callback) {
        startScan(null, new Builder().build(), callback);
    }

    public void startScan(List<ScanFilter> filters, ScanSettings settings, ScanCallback callback) {
        startScan(filters, settings, null, callback, null, null);
    }

    public int startScan(List<ScanFilter> filters, ScanSettings settings, PendingIntent callbackIntent) {
        return startScan(filters, settings != null ? settings : new Builder().build(), null, null, callbackIntent, null);
    }

    public void startScanFromSource(WorkSource workSource, ScanCallback callback) {
        startScanFromSource(null, new Builder().build(), workSource, callback);
    }

    public void startScanFromSource(List<ScanFilter> filters, ScanSettings settings, WorkSource workSource, ScanCallback callback) {
        startScan(filters, settings, workSource, callback, null, null);
    }

    /* JADX WARNING: Missing block: B:50:0x007d, code:
            return 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int startScan(List<ScanFilter> filters, ScanSettings settings, WorkSource workSource, ScanCallback callback, PendingIntent callbackIntent, List<List<ResultStorageDescriptor>> resultStorages) {
        BluetoothLeUtils.checkAdapterStateOn(this.mBluetoothAdapter);
        if (callback == null && callbackIntent == null) {
            throw new IllegalArgumentException("callback is null");
        } else if (settings == null) {
            throw new IllegalArgumentException("settings is null");
        } else {
            synchronized (this.mLeScanClients) {
                int postCallbackErrorOrReturn;
                IBluetoothGatt gatt;
                if (callback != null) {
                    if (this.mLeScanClients.containsKey(callback)) {
                        postCallbackErrorOrReturn = postCallbackErrorOrReturn(callback, 1);
                        return postCallbackErrorOrReturn;
                    }
                }
                try {
                    gatt = this.mBluetoothManager.getBluetoothGatt();
                } catch (RemoteException e) {
                    gatt = null;
                }
                if (gatt == null) {
                    postCallbackErrorOrReturn = postCallbackErrorOrReturn(callback, 3);
                    return postCallbackErrorOrReturn;
                } else if (!isSettingsConfigAllowedForScan(settings)) {
                    postCallbackErrorOrReturn = postCallbackErrorOrReturn(callback, 4);
                    return postCallbackErrorOrReturn;
                } else if (!isHardwareResourcesAvailableForScan(settings)) {
                    postCallbackErrorOrReturn = postCallbackErrorOrReturn(callback, 5);
                    return postCallbackErrorOrReturn;
                } else if (!isSettingsAndFilterComboAllowed(settings, filters)) {
                    postCallbackErrorOrReturn = postCallbackErrorOrReturn(callback, 4);
                    return postCallbackErrorOrReturn;
                } else if (callback != null) {
                    new BleScanCallbackWrapper(gatt, filters, settings, workSource, callback, resultStorages).startRegistration();
                } else {
                    try {
                        gatt.startScanForIntent(callbackIntent, settings, filters, ActivityThread.currentOpPackageName());
                    } catch (RemoteException e2) {
                        return 3;
                    }
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

    public void stopLeScanByPkg(String pkgName) {
        if (pkgName != null && !TextUtils.isEmpty(pkgName)) {
            Log.d(TAG, "stopLeScanByPkg() " + pkgName);
            try {
                this.mBluetoothManager.getBluetoothGatt().stopScanByPkg(pkgName);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to get stopLeScanByPkg, " + e.getMessage());
            }
        }
    }

    public void startLeScanByPkg(String pkgName) {
        Log.d(TAG, "startLeScanByPkg() " + pkgName);
        try {
            this.mBluetoothManager.getBluetoothGatt().startScanByPkg(pkgName);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to get startLeScanByPkg, " + e.getMessage());
        }
    }

    public void stopScan(PendingIntent callbackIntent) {
        BluetoothLeUtils.checkAdapterStateOn(this.mBluetoothAdapter);
        try {
            this.mBluetoothManager.getBluetoothGatt().stopScanForIntent(callbackIntent, ActivityThread.currentOpPackageName());
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to get stopScan, " + e.getMessage());
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
        startScan(scanFilters, settings, null, callback, null, scanStorages);
    }

    public void cleanup() {
        this.mLeScanClients.clear();
    }

    private int postCallbackErrorOrReturn(ScanCallback callback, int errorCode) {
        if (callback == null) {
            return errorCode;
        }
        postCallbackError(callback, errorCode);
        return 0;
    }

    private void postCallbackError(final ScanCallback callback, final int errorCode) {
        this.mHandler.post(new Runnable() {
            public void run() {
                callback.onScanFailed(errorCode);
            }
        });
    }

    private boolean isSettingsConfigAllowedForScan(ScanSettings settings) {
        if (this.mBluetoothAdapter.isOffloadedFilteringSupported()) {
            return true;
        }
        if (settings.getCallbackType() == 1 && settings.getReportDelayMillis() == 0) {
            return true;
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
        return true;
    }

    private boolean isHardwareResourcesAvailableForScan(ScanSettings settings) {
        boolean z = false;
        int callbackType = settings.getCallbackType();
        if ((callbackType & 2) == 0 && (callbackType & 4) == 0) {
            return true;
        }
        if (this.mBluetoothAdapter.isOffloadedFilteringSupported()) {
            z = this.mBluetoothAdapter.isHardwareTrackingFiltersAvailable();
        }
        return z;
    }
}
