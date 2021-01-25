package ohos.bluetooth.ble;

public interface BleCentralManagerCallback {
    void onScanCallback(BleScanResult bleScanResult);

    void onStartScanFailed(int i);
}
