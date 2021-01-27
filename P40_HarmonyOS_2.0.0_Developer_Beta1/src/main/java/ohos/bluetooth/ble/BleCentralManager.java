package ohos.bluetooth.ble;

import java.util.List;
import ohos.app.Context;
import ohos.bluetooth.BluetoothHostProxy;
import ohos.bluetooth.LogHelper;
import ohos.bluetooth.ble.ScannerCallbackWrapper;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class BleCentralManager {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "BleCentralManager");
    private BleCentralManagerCallback mBleCentralManagerCallback;
    private BleCentralManagerProxy mBleCentralManagerProxy;
    private final Context mContext;
    private int mDutyRatio;
    private List<BleScanFilter> mFilters;
    private int mMatchingMode;
    private int mScannerId;
    private long mTime;

    public BleCentralManager(Context context, BleCentralManagerCallback bleCentralManagerCallback) {
        this.mBleCentralManagerProxy = null;
        this.mBleCentralManagerCallback = null;
        this.mDutyRatio = 0;
        this.mMatchingMode = 1;
        this.mTime = 0;
        this.mScannerId = 0;
        this.mBleCentralManagerProxy = new BleCentralManagerProxy(BluetoothHostProxy.getInstace().getSaProfileProxy(11).orElse(null));
        this.mBleCentralManagerCallback = bleCentralManagerCallback;
        this.mContext = context;
    }

    public void startScan(List<BleScanFilter> list) {
        startScan(list, this.mDutyRatio, this.mMatchingMode, this.mTime);
    }

    public void startScan(List<BleScanFilter> list, int i, int i2) {
        this.mFilters = list;
        this.mDutyRatio = i;
        this.mMatchingMode = i2;
        this.mBleCentralManagerProxy.addScanner(this);
    }

    public void startScan(List<BleScanFilter> list, int i, int i2, long j) {
        this.mFilters = list;
        this.mDutyRatio = i;
        this.mMatchingMode = i2;
        if (j >= 0) {
            this.mTime = j;
            this.mBleCentralManagerProxy.addScanner(this);
            return;
        }
        throw new IllegalArgumentException("reportDelay must be > 0");
    }

    public void stopScan() {
        this.mBleCentralManagerProxy.stopScan(this.mScannerId);
        this.mBleCentralManagerProxy.removeScanner(this.mScannerId);
    }

    public List<BlePeripheralDevice> getDevicesByStates(int[] iArr) {
        HiLog.info(TAG, "getDevicesByStates", new Object[0]);
        return this.mBleCentralManagerProxy.getPeripheralDevicesByStates(iArr);
    }

    /* access modifiers changed from: package-private */
    public void onScannerAddFinish(int i, int i2) {
        HiLog.info(TAG, "onScannerAddFinish, result %{public}d, scanner %{public}d", new Object[]{Integer.valueOf(i), Integer.valueOf(i2)});
        if (i == 0 && i2 != -1) {
            this.mScannerId = i2;
            Context context = this.mContext;
            this.mBleCentralManagerProxy.doScan(i2, this.mFilters, new ScannerCallbackWrapper.ScanParameter(this.mDutyRatio, this.mMatchingMode, this.mTime), context != null ? context.getBundleName() : "");
        }
    }

    /* access modifiers changed from: package-private */
    public void onScanResult(BleScanResult bleScanResult) {
        HiLog.info(TAG, "onScanResult", new Object[0]);
        BleCentralManagerCallback bleCentralManagerCallback = this.mBleCentralManagerCallback;
        if (bleCentralManagerCallback == null) {
            HiLog.error(TAG, "onScanResult got null callback", new Object[0]);
        } else {
            bleCentralManagerCallback.scanResultEvent(bleScanResult);
        }
    }

    /* access modifiers changed from: package-private */
    public void scanFailedEvent(int i) {
        HiLog.info(TAG, "scanFailedEvent", new Object[0]);
        BleCentralManagerCallback bleCentralManagerCallback = this.mBleCentralManagerCallback;
        if (bleCentralManagerCallback == null) {
            HiLog.error(TAG, "scanFailedEvent got null callback", new Object[0]);
        } else {
            bleCentralManagerCallback.scanFailedEvent(i);
        }
    }

    /* access modifiers changed from: package-private */
    public void groupScanResultsEvent(List<BleScanResult> list) {
        HiLog.info(TAG, "groupScanResultsEvent", new Object[0]);
        BleCentralManagerCallback bleCentralManagerCallback = this.mBleCentralManagerCallback;
        if (bleCentralManagerCallback == null) {
            HiLog.error(TAG, "groupScanResultsEvent got null callback", new Object[0]);
        } else {
            bleCentralManagerCallback.groupScanResultsEvent(list);
        }
    }
}
