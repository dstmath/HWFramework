package ohos.bluetooth.ble;

import java.util.List;
import ohos.bluetooth.BluetoothHostProxy;
import ohos.bluetooth.LogHelper;
import ohos.bluetooth.ble.ScannerCallbackWrapper;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class BleCentralManager {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "BleCentralManager");
    private BleCentralManagerCallback mBleCentralManagerCallback;
    private BleCentralManagerProxy mBleCentralManagerProxy;
    private int mDutyRatio;
    private List<BleScanFilter> mFilters;
    private int mMatchingMode;
    private int mScannerId;

    public BleCentralManager(BleCentralManagerCallback bleCentralManagerCallback) {
        this.mBleCentralManagerProxy = null;
        this.mBleCentralManagerCallback = null;
        this.mDutyRatio = 0;
        this.mMatchingMode = 1;
        this.mScannerId = 0;
        this.mBleCentralManagerProxy = new BleCentralManagerProxy(BluetoothHostProxy.getInstace().getSaProfileProxy(11).orElse(null));
        this.mBleCentralManagerCallback = bleCentralManagerCallback;
    }

    public void startScan(List<BleScanFilter> list) {
        startScan(list, this.mDutyRatio, this.mMatchingMode);
    }

    public void startScan(List<BleScanFilter> list, int i, int i2) {
        this.mFilters = list;
        this.mDutyRatio = i;
        this.mMatchingMode = i2;
        this.mBleCentralManagerProxy.addScanner(this);
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
            this.mBleCentralManagerProxy.doScan(i2, this.mFilters, new ScannerCallbackWrapper.ScanParameter(this.mDutyRatio, this.mMatchingMode));
        }
    }

    /* access modifiers changed from: package-private */
    public void onScanResult(BleScanResult bleScanResult) {
        HiLog.info(TAG, "onScanResult", new Object[0]);
        BleCentralManagerCallback bleCentralManagerCallback = this.mBleCentralManagerCallback;
        if (bleCentralManagerCallback == null) {
            HiLog.error(TAG, "onScanResult got null callback", new Object[0]);
        } else {
            bleCentralManagerCallback.onScanCallback(bleScanResult);
        }
    }

    /* access modifiers changed from: package-private */
    public void onStartScanFailed(int i) {
        HiLog.info(TAG, "onStartScanFailed", new Object[0]);
        BleCentralManagerCallback bleCentralManagerCallback = this.mBleCentralManagerCallback;
        if (bleCentralManagerCallback == null) {
            HiLog.error(TAG, "onStartScanFailed got null callback", new Object[0]);
        } else {
            bleCentralManagerCallback.onStartScanFailed(i);
        }
    }
}
