package ohos.bluetooth.ble;

import java.util.List;

public interface BleCentralManagerCallback {
    void groupScanResultsEvent(List<BleScanResult> list);

    void scanFailedEvent(int i);

    void scanResultEvent(BleScanResult bleScanResult);
}
